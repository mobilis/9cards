package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;

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
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Handler;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.GameOverMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.StartGameMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Player;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;



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
		//xmppConnection = new XMPPConnection("mobilis-dev.inf.tu-dresden.de");
		
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
		
		// subfilter for packets of type Message or IQ (ignore Presence packets)
		PacketFilter subFilterTypes = new OrFilter(
				new PacketTypeFilter(Message.class),
				new PacketTypeFilter(IQ.class));
		
		// subfilter for sender (coordinator service running on mobilis server)
		PacketFilter subFilterFrom = new FromContainsFilter(bgService.getMobilisServerJID());
		
		// set filters
		PacketFilter filter = new AndFilter(
				subFilterTypes,
				subFilterFrom); 
		
		xmppConnection.addPacketListener(mPacketListener, filter);
		
		return true;
	}
	
	
	/**
	 * 
	 */
	public void registerXmppExtensions()
	{
		MobilisServiceDiscoveryBean discoveryBean = new MobilisServiceDiscoveryBean();
		IQImplProvider iqProv_1 = new IQImplProvider(discoveryBean.getNamespace(), discoveryBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(discoveryBean.getChildElement(), discoveryBean.getNamespace(), iqProv_1);

		CreateNewServiceInstanceBean createBean = new CreateNewServiceInstanceBean();
		IQImplProvider iqProv_2 = new IQImplProvider(createBean.getNamespace(), createBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(createBean.getChildElement(), createBean.getNamespace(), iqProv_2);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public boolean initializeMucAndChat(Handler updateUIHandler)
	{		
		// return if connection was not established yet
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
		if(privateChat == null) {
			for(Iterator<String> it = publicChat.getOccupants(); it.hasNext();) {
				String id = it.next();
				if(id.toLowerCase().endsWith("/9cards-service")) {
					privateChat = publicChat.createPrivateChat(id, mChatMessageListener);
					break;
				}
			}
		}
		
		// also listen to messages of private chats which were initiated by the other side
		xmppConnection.getChatManager().addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if(!createdLocally)
					chat.addMessageListener(mChatMessageListener);
			}
		});
		
		// also add a player object for current player to the game
		for(Iterator<String> it = publicChat.getOccupants(); it.hasNext();) {
			String myId = it.next();
			if(myId.endsWith(publicChat.getNickname())) {
				Player me = new Player(myId);
				bgService.getGame().getPlayers().put(me.getNickname(), me);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
				break;
			}
		}
		
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
	public void leaveChat()
	{
		if(publicChat != null)// && publicChat.isJoined())
			publicChat.leave();
		
		publicChat = null;
		privateChat = null;
	}
	
	
	/**
	 * 
	 */
	public void disconnectFromXmppServer()
	{
		if(isConnected()) {
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus("User disconnected from XMPP Server");
			xmppConnection.sendPacket(presence);
			
			leaveChat();
			xmppConnection.disconnect();
		}

		xmppConnection = null;
	}

// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Listens for packets from the Mobilis sever (not to be confused with game service!)
	 */
	private PacketListener mPacketListener = new PacketListener() {
		
		@Override
		public void processPacket(Packet packet) {
			Log.i("ServerConnection.mPacketListener", "Packet received (" + packet.toXML() + ")");
			
			if(packet instanceof IQ) {
				IQ iq = (IQ) packet;
				XMPPBean bean = null;
				
				if(iq.getChildElementXML().toLowerCase().startsWith("<servicediscovery"))
					bean = new MobilisServiceDiscoveryBean();
				
				else if(iq.getChildElementXML().toLowerCase().startsWith("<createnewserviceinstance"))
					bean = new CreateNewServiceInstanceBean();
				
				if(bean != null) {
					try {
						XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
						xmlParser.setInput(new StringReader(iq.getChildElementXML()));
						bean.fromXML(xmlParser);
						bgService.getGameState().processPacket(bean);
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), "Failed to parse XML (" + e.getMessage() + ")");
					}
				}
				
				else Log.w("ServerConnection.mPacketListener", "Unhandled IQ type received! (" + iq.getChildElementXML() + ")");
			}
			
			else Log.w(getClass().getSimpleName(), "Unhandled Packet type received (" + packet.toXML() + ")");
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
			
			// TODO evtl die ConfigureGameResponses beachten, werden vom service bereits gesendet
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
			
			if(packet instanceof Message) {
				Message mesg = (Message) packet;
				XMPPInfo info = null;
				
				if(mesg.getBody().toLowerCase().startsWith("<mobilismessage type=startgamemessage"))
					info = new StartGameMessage();
				if(mesg.getBody().toLowerCase().startsWith("<mobilismessage type=cardplayedmessage"))
					info = new CardPlayedMessage();
				if(mesg.getBody().toLowerCase().startsWith("<mobilismessage type=roundcompletemessage"))
					info = new RoundCompleteMessage();
				if(mesg.getBody().toLowerCase().startsWith("<mobilismessage type=gameovermessage"))
					info = new GameOverMessage();
			
				if(info != null) {
					try {
						XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
						String content = mesg.getBody().substring(mesg.getBody().indexOf(">") + 1, mesg.getBody().lastIndexOf("<"));
						xmlParser.setInput(new StringReader(content));
						info.fromXML(xmlParser);
						bgService.getGameState().processChatMessage(info);
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), "Failed to parse XML (" + e.getMessage() + ")");
					}
				}
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
		
		Log.i(getClass().getSimpleName(), "MobilisServiceDiscoveryBean sent (" + (new BeanIQAdapter(bean)).toXML() + ")");
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
		Log.i("IQProxy", "CreateNewServiceInstanceBean sent");
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
		Log.i("IQProxy", "ConfigureGameBean sent");
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
		Log.i(getClass().getSimpleName(), "Errorbean sent");
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
			Log.e(getClass().getSimpleName(), "Failed to send start game message (" + e.getClass().toString() + ": " + e.getMessage() + ")");
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
