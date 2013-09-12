package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

import android.os.Handler;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.GameOverMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Player;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;

/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: https://github.com/mobilis
 ******************************************************************************/
public class ServerConnection
{
	/** The connection to the xmpp Server */
	private XMPPConnection xmppConnection;
	/** The chat used for private messages to the ninecards service. */
	private Chat privateChat;
	/** The multiuser chat room to send or receive messages to/from all users. */
	private MultiUserChat publicChat;
	
	/** The handler needed for updating the GUI. */
	private Handler mUpdateUIHandler;

	/** The application service running in the background. */
	private BackgroundService bgService;
	
	
	/**
	 * 
	 * @param appContext
	 */
	public ServerConnection(BackgroundService bgService)
	{
		this.bgService = bgService;
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * 
	 * @param server
	 * @return
	 */
	public boolean connectToXmppServer(String server, String userJid, String userPw)
	{
		// Create connection to XMPP server
		ConnectionConfiguration config = new ConnectionConfiguration(server, 5222);
		xmppConnection = new XMPPConnection(config);
		
		try {
			xmppConnection.connect();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Failed to connect to XMPP Server (" + e.getMessage() + ")");
			xmppConnection = null;
			return false;
		}

		// Login on XMPP server
		try {
			xmppConnection.login(userJid, userPw, "android");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Failed to login on XMPP Server (" + e.getMessage() + ")");
			xmppConnection.disconnect();
			xmppConnection = null;
			return false;
		}
		
		// Listen for packets from Mobilis server
		PacketFilter packetFilter = new AndFilter(
				new PacketTypeFilter(Message.class),
				new FromContainsFilter(server));
		
		xmppConnection.addPacketListener(mPacketListener, packetFilter);
		
		return true;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean initializeMucAndChat(Handler updateUIHandler)
	{		
		// return if connectiion was not established yet
		if (xmppConnection == null || !xmppConnection.isConnected()) {
			Log.e(getClass().getSimpleName(), "Can't create chat - not connected to XMPP Server!");
			return false;
		}
		
		mUpdateUIHandler = updateUIHandler;
		String gameJid = bgService.getGameServiceJid();
		String roomName = gameJid.substring(gameJid.indexOf("/") + 1);
		String roomId = roomName + "@conference." + gameJid.substring(gameJid.indexOf("@") + 1, gameJid.indexOf("/"));

		// initialise multi user chat
		if (publicChat == null) {

			String nick;
			if (bgService.getUserNick() == null || bgService.getUserNick().equals(""))
				nick = bgService.getUserJid().substring(bgService.getUserJid().indexOf("/") + 1);
			else nick = bgService.getUserNick();

			publicChat = new MultiUserChat(xmppConnection, roomId);
			publicChat.addMessageListener(mMucMessageListener);
			publicChat.addParticipantStatusListener(mParticipantStatusListener);
			publicChat.addUserStatusListener(new DefaultUserStatusListener() {
				@Override
				public void adminGranted() {
					super.adminGranted();
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_ENABLE_START_GAME_BUTTON);
				}
			});

			try {
				publicChat.join(nick);
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "Failed to join Multiuser Chat (" + e.getMessage() + ")");
				return false;
			}
		}
		
		// initialise chat with service (for private messages)
		if(privateChat == null)
			publicChat.createPrivateChat(roomId + "9Cards-Service", mChatMessageListener);
		
		// also listen to messages of private chats which were initiated by the other side
		xmppConnection.getChatManager().addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if(!createdLocally)
					chat.addMessageListener(mChatMessageListener);
			}
		});
		
		return true;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean isConnected()
	{
		return xmppConnection != null && xmppConnection.isConnected();
	}
	
	
	/**
	 * 
	 */
	public void leavePublicChat()
	{
		if(publicChat != null && publicChat.isJoined())
			publicChat.leave();
		
		publicChat = null;
	}
	
	
	/**
	 * 
	 */
	public void disconnectFromXmppServer()
	{
		Presence presence = new Presence(Presence.Type.unavailable);
		presence.setStatus("User disconnected from XMPP Server");
		xmppConnection.sendPacket(presence);
		
		leavePublicChat();
		xmppConnection.disconnect();

		privateChat = null;
		xmppConnection = null;
	}

// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Listens for packets from the Mobilis sever (not to be confused with game service!)
	 */
	private PacketListener mPacketListener = new PacketListener() {
		
		@Override
		public void processPacket(Packet packet) {
			Log.i(getClass().getSimpleName(), "Received packet: " + packet.toXML());

			if(packet instanceof BeanIQAdapter) {
				XMPPBean bean = ((BeanIQAdapter) packet).getBean();
				
				if(bean instanceof MobilisServiceDiscoveryBean) {
					Log.i(getClass().getSimpleName(), "MobilisServiceDiscoveryBean received! (" + bean.toXML() + ")");
					bgService.getGameState().processPacket(bean);
				}
				
				else if(bean instanceof CreateNewServiceInstanceBean) {
					Log.i(getClass().getSimpleName(), "CreateNewServiceInstanceBean received! (" + bean.toXML() + ")");
					bgService.getGameState().processPacket(bean);
				}
			}
			
			else Log.w(getClass().getSimpleName(), "Unhandled packet received (" + packet.toXML() + ")");
		}
	};
	
	
	/**
	 * Listens for private chat messages.
	 */
	private MessageListener mChatMessageListener = new MessageListener()
	{
		@Override
		public void processMessage(Chat chat, Message message) {
			Log.i(getClass().getSimpleName(), "Received private chat message: " + message.getBody());
			
			// TODO
			// evtl auf ConfigureGameResponses lauschen, läuft aber derzeit ohne
		}
	};
	
	
	/**
	 * Listens for "group chat" messages.
	 */
	private PacketListener mMucMessageListener = new PacketListener()
	{
		@Override
		public void processPacket(Packet packet) {
			Log.i(getClass().getSimpleName(), "Received public MUC message: " + packet.toXML());
			
			// TODO
			// das alles mal mit xmlparser machen (hatte exceptions bei 2 aufeinanderfolgenden sich öffnenden tags gegeben)
			if(packet.toXML().contains(CardPlayedMessage.class.getSimpleName())) {
				String player = packet.toXML().substring(packet.toXML().indexOf("<player>") + "<player>".length(), packet.toXML().indexOf("</player>"));
				int round = Integer.parseInt(packet.toXML().substring(packet.toXML().indexOf("<round>") + "<round>".length(), packet.toXML().indexOf("</round>")));
				bgService.getGameState().processChatMessage(new CardPlayedMessage(round, player));
			}
			
			else if(packet.toXML().contains(RoundCompleteMessage.class.getSimpleName())) {
				String startTag = "<MobilisMessage type=" + RoundCompleteMessage.class.getSimpleName() + ">";
				String endTag = "</MobilisMessage>";
				String full = packet.toXML();
				String content = full.substring(full.indexOf(startTag) + startTag.length(), full.indexOf(endTag));
				
				String roundString = content.substring(content.indexOf("<round>") + "<round>".length(), content.indexOf("</round>"));
				String winnerString = content.substring(content.indexOf("<winner>") + "<winner>".length(), content.indexOf("</winner>"));
				
				List<PlayerInfo> list = new ArrayList<PlayerInfo>();
				String plrInfos = content.substring(content.indexOf("<PlayerInfo>"), content.lastIndexOf("</PlayerInfo>") + "</PlayerInfo>".length());
				String[] infos = plrInfos.split("</PlayerInfo>");
				for(int i=0; i<infos.length; i++) {
					PlayerInfo info = new PlayerInfo();
					String jid = infos[i].substring(infos[i].indexOf("<jid>") + "<jid>".length(), infos[i].indexOf("</jid>"));
					String score = infos[i].substring(infos[i].indexOf("<score>") + "<score>".length(), infos[i].indexOf("</score>"));
					info.setJid(jid);
					info.setScore(Integer.parseInt(score));
					
					List<Integer> usedCards = new ArrayList<Integer>();
					String crdInfos = infos[i].substring(infos[i].indexOf("<usedcards>"), infos[i].lastIndexOf("</usedcards>") + "</usedcards>".length());
					String[] crds = crdInfos.split("</usedcards>");

					for(int j=0; j<crds.length; j++) {
						String value = crds[j].substring(crds[j].indexOf("<usedcards>") + "<usedcards>".length());
						usedCards.add(Integer.parseInt(value));
					}
					
					info.setUsedcards(usedCards);
					list.add(info);
				}
				
				RoundCompleteMessage rcMessage = new RoundCompleteMessage();
				rcMessage.setRound(Integer.parseInt(roundString));
				rcMessage.setWinner(winnerString);
				rcMessage.setPlayerInfos(list);
				
				bgService.getGameState().processChatMessage(rcMessage);
			}
			
			else if(packet.toXML().contains(GameOverMessage.class.getSimpleName())) {
				String winner = packet.toXML().substring(packet.toXML().indexOf("<winner>") + "<winner>".length(), packet.toXML().indexOf("</winner>"));
				int score = Integer.parseInt(packet.toXML().substring(packet.toXML().indexOf("<score>") + "<score>".length(), packet.toXML().indexOf("</score>")));
				bgService.getGameState().processChatMessage(new GameOverMessage(winner, score));
			}
		}
	}; 
	
	
	/**
	 * Listens for joining or leaving of players and updates the user list in the GUI.
	 */
	private ParticipantStatusListener mParticipantStatusListener = new ParticipantStatusListener()
	{
		@Override
		public void joined(String participant) {
			if(!participant.toLowerCase().contains("service") && !bgService.getGame().getPlayers().containsKey(participant)) {
				bgService.getGame().getPlayers().put(participant, new Player(participant));
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void left(String participant) {
			if(bgService.getGame().getPlayers().containsKey(participant)) {
				bgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void kicked(String participant, String actor, String reason) {
			if(bgService.getGame().getPlayers().containsKey(participant)) {
				bgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void banned(String participant, String actor, String reason) {
			if(bgService.getGame().getPlayers().containsKey(participant)) {
				bgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void nicknameChanged(String participant, String newNickname) {
			if(bgService.getGame().getPlayers().containsKey(participant)) {
				bgService.getGame().getPlayers().get(participant).changeNickname(newNickname);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		public void voiceRevoked(String arg0) {}
		public void voiceGranted(String arg0) {}
		public void ownershipRevoked(String arg0) {}
		public void ownershipGranted(String arg0) {}
		public void moderatorRevoked(String arg0) {}
		public void moderatorGranted(String arg0) {}
		public void membershipRevoked(String arg0) {}
		public void membershipGranted(String arg0) {}
		public void adminRevoked(String arg0) {}
		public void adminGranted(String arg0) {}
	};

// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Sends a ServiceDiscoveryBean to the Mobilis Server (not to the Ninecards Service!)
	 * A MobilisServiceDiscoveryBean with null as namespace returns all services,
     * a proper service namespace lets the server send information about all instances of the specified service.
	 * @param namespace
	 */
	public void sendServiceDiscovery(String namespace)
	{
		MobilisServiceDiscoveryBean bean = new MobilisServiceDiscoveryBean(namespace, Integer.MIN_VALUE, false);
		bean.setType(XMPPBean.TYPE_GET);
		bean.setFrom(bgService.getUserJid());
		bean.setTo(bgService.getMobilisServerJID());
		
		xmppConnection.sendPacket(new BeanIQAdapter(bean));
		Log.v(getClass().getSimpleName(), "MobilisServiceDiscoveryBean sent");
	}
	
	
	/**
	 * Sends a CreateNewServiceInstanceBean to the Mobilis Coordinator service.
	 * 
	 * IQ parameters:
	 * Type: SET
	 * From: player's JID
	 * To: Mobilis Coordinator service
	 *
	 * @param serviceNamespace the service namespace for which a new instance shall be created
	 * @param serviceName the service name
	 * @param servicePassword the service password
	 */
	public void sendCreateNewServiceInstance(String serviceNamespace, String serviceName, String servicePassword)
	{
		CreateNewServiceInstanceBean bean = new CreateNewServiceInstanceBean(serviceNamespace, servicePassword);
		bean.setServiceName(serviceName);

		bean.setType(XMPPBean.TYPE_SET);
		bean.setFrom(bgService.getUserJid());
		bean.setTo(bgService.getMobilisServerJID());
		
		xmppConnection.sendPacket(new BeanIQAdapter(bean));
		Log.v("IQProxy", "CreateNewServiceInstanceBean sent");
	}
	
	
	/**
	 * 
	 * @param gamename
	 * @param players
	 * @param rounds
	 */
	public void sendGameConfiguration(String gamename, int players, int rounds)
	{
		ConfigureGameRequest bean = new ConfigureGameRequest(gamename, players, rounds);

		bean.setType(XMPPBean.TYPE_SET);
		bean.setFrom(bgService.getUserJid());
		bean.setTo(bgService.getGameServiceJid());
		
		xmppConnection.sendPacket(new BeanIQAdapter(bean));
		Log.v("IQProxy", "ConfigureGameBean sent");
	}
	
	
	/**
	 * Send an XMPPBean of type ERROR.
	 * @param inBean the XMPPBean to reply with an ERROR. The payload will be copied.
	 */
	public void sendXMPPBeanError(XMPPBean inBean){
		XMPPBean resultBean = inBean.clone();
		resultBean.setTo(inBean.getFrom());
		resultBean.setFrom(bgService.getUserJid());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		
		xmppConnection.sendPacket(new BeanIQAdapter(resultBean));
		Log.v(getClass().getSimpleName(), "Errorbean sent");
	}

// -------------------------------------------------------------------------------------------------------------------------------

	/**
	 * For sending StartGameMessage and PlayCardMessage via private chat to the game service
	 */
	public boolean sendPrivateToService(XMPPInfo xmppInfo)
	{
		Message mesg = new Message();
		
		mesg.setBody("<MobilisMessage type=" + xmppInfo.getClass().getSimpleName() + ">"
				+ xmppInfo.toXML()
				+ "</MobilisMessage>");
		
		try {
			privateChat.sendMessage(mesg);
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Failed to send start game message (" + e.getMessage() + ")");
			return false;
		}
		
		return true;
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * 
	 * @return
	 */
	public String getMyChatNick()
	{
		if(publicChat != null)
			return publicChat.getRoom() + "/" + publicChat.getNickname();
		
		return null;
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
		
	/**
	 * Just for testing purposes
	 //TODO remove
	 */
	public void printRoster()
	{
		Roster roster = xmppConnection.getRoster();
		Collection<RosterEntry> entries = roster.getEntries();
		for(RosterEntry entry : entries)
			System.out.println(entry);
		
		//roster.getPresence("user"); gibt angeblich Presence zurück oder null wenn user nicht on ist
		//man muss die presences auch subscribed haben, ist meist der fall wenn nutzer im roster sind
		
		// presence: entweder on oder off, wenn on dann oft zusatzinfos wie was man gerade macht und so
		
		//roster aber wohl nicht so geeignet, man muss bestätigen dass man hinzugefügt werden will, ist dann permanent
		//sind konkret presence subscription requests, kann man auch automatisch alle annehmen oder ablehnen
		// Roster.setSubscriptionMode(Roster.SubscriptionMode)
		// a PacketListener should be registered that listens for Presence packets that have a type of Presence.Type.subscribe
		
		// RosterListener für Presence- und Rosteränderungen
		//roster.addRosterListener(new RosterListener() {
	}
}
