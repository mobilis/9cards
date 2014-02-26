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
package de.tudresden.inf.rn.mobilis.android.ninecards.game;

import java.io.StringReader;
import java.util.Iterator;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
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
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DefaultUserStatusListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.IQImplProvider;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.SendNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.ConfigureGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.GameOverMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.GameStartsMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.GetGameConfigurationRequest;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.GetGameConfigurationResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;

/**
 * The class responsible for handling the connection to the XMPP server
 * as well as for sending and receiving messages.
 * 
 * @author Matthias Köngeter
 *
 */
public class ServerConnection
{
	/** The connection to the XMPP Server. */
	private XMPPConnection mXmppConnection;
	/** The chat used for private messages to the ninecards service. */
	private Chat mPrivateChat;
	/** The multiuser chat room to send/receive messages to/from all users. */
	private MultiUserChat mPublicChat;
	/** The information needed for logging in on the XMPP server. */
	private String mServer, mUserJid, mUserPw;
	/** The nickname used by the ninecards service inside of the muc room. */
	private static final String serviceNick = "9Cards-Service";
	
	/** The handler needed for updating the GUI. */
	private Handler mUpdateUIHandler;

	/** The application service running in the background. */
	private BackgroundService mBgService;
	
	
	/**
	 * Constructor used for creating a new ServerConnection instance.
	 * 
	 * @param bgService the instance of the application service running in the background
	 */
	public ServerConnection(BackgroundService bgService)
	{
		this.mBgService = bgService;
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Creates a connection to the XMPP server and logging in. Also registers different filters for messages.
	 * 
	 * @param server the IP or URL of the XMPP server
	 * @param userJid the JID to be used for logging in
	 * @param userPw the password to be used for logging in
	 * @return true if connecting was successful, false if not.
	 */
	public boolean connectToXmppServer(String server, String userJid, String userPw)
	{	
		// this is to avoid NetworkOnMainThreadException in Android 3.0 and newer versions
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		// Create connection to XMPP server
		mServer = server;
		mUserJid = userJid;
		mUserPw = userPw;
		
		try {
			AndroidConnectionConfiguration config = new AndroidConnectionConfiguration(server, 5222);
			mXmppConnection = new XMPPConnection(config);
			mXmppConnection.connect();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Failed to connect to XMPP Server ("
				+ e.getClass().getSimpleName() + " / " + e.getMessage() + ")");
			mXmppConnection = null;
			return false;
		}

		// Login on XMPP server
		try {
			mXmppConnection.login(userJid, userPw, "android");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), "Failed to connect to XMPP Server ("
					+ e.getClass().getSimpleName() + " / " + e.getMessage() + ")");
			mXmppConnection.disconnect();
			mXmppConnection = null;
			return false;
		}
		
		// subfilter for packets of type Message or IQ (ignore Presence packets)
		PacketFilter subFilterTypes = new OrFilter(
				new PacketTypeFilter(Message.class),
				new PacketTypeFilter(IQ.class));
		
		// subfilter for sender (coordinator service running on mobilis server or service instance)
		PacketFilter subFilterFrom = new OrFilter(
				new FromContainsFilter(mBgService.getMobilisServerJID()),
				new FromContainsFilter("mobilisninecards"));
		
		// set filters
		PacketFilter filter = new AndFilter(
				subFilterTypes,
				subFilterFrom); 
		
		mXmppConnection.addPacketListener(mPacketListener, filter);
		
		return true;
	}
	
	
	/**
	 * Registers custom message types which to enable the parsing of their tags. Only
	 * messages which are being received need to be registered.
	 */
	public void registerXmppExtensions()
	{
		IQImplProvider iqProvider;
		
		MobilisServiceDiscoveryBean discoveryBean = new MobilisServiceDiscoveryBean();
		iqProvider = new IQImplProvider(discoveryBean.getNamespace(), discoveryBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(discoveryBean.getChildElement(), discoveryBean.getNamespace(), iqProvider);

		CreateNewServiceInstanceBean createServiceBean = new CreateNewServiceInstanceBean();
		iqProvider = new IQImplProvider(createServiceBean.getNamespace(), createServiceBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(createServiceBean.getChildElement(), createServiceBean.getNamespace(), iqProvider);
		
		SendNewServiceInstanceBean serviceCreatedBean = new SendNewServiceInstanceBean();
		iqProvider = new IQImplProvider(serviceCreatedBean.getNamespace(), serviceCreatedBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(serviceCreatedBean.getChildElement(), serviceCreatedBean.getNamespace(), iqProvider);
		
		ConfigureGameResponse confResponseBean = new ConfigureGameResponse();
		iqProvider = new IQImplProvider(confResponseBean.getNamespace(), confResponseBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(confResponseBean.getChildElement(), confResponseBean.getNamespace(), iqProvider);
		
		GetGameConfigurationResponse getConfResponseBean = new GetGameConfigurationResponse();
		iqProvider = new IQImplProvider(getConfResponseBean.getNamespace(), getConfResponseBean.getChildElement());
		ProviderManager.getInstance().addIQProvider(getConfResponseBean.getChildElement(), getConfResponseBean.getNamespace(), iqProvider);
	}
	
	
	/**
	 * Initializes multiuser and private chat and creates a player object for each participant found in the muc room.
	 * 
	 * @param updateUIHandler the handler which is needed to update the UI from a non-UI thread
	 * @return true if initialization was successful, false if not
	 */
	public boolean initializeMucAndChat(Handler updateUIHandler)
	{		
		// return if connection has not been established yet
		if (!isConnected()) {
			if(!connectToXmppServer(mServer, mUserJid, mUserPw)) {
				Log.w(getClass().getSimpleName(), "Couldn't initialize MUC and chat (couldn't connect to XMPP server)");
				return false;
			}
		}
		
		mUpdateUIHandler = updateUIHandler;

		// initialise multi user chat
		if (mPublicChat == null) {

			String nick = mBgService.getUserNick();
			if (nick == null || nick.equals(""))
				nick = mBgService.getUserJID().substring(0, mBgService.getUserJID().indexOf("@"));

			mPublicChat = new MultiUserChat(mXmppConnection, mBgService.getMucId());
			mPublicChat.addMessageListener(mMucMessageListener);
			mPublicChat.addParticipantStatusListener(mParticipantStatusListener);
			mPublicChat.addUserStatusListener(new DefaultUserStatusListener() {
				@Override
				public void adminGranted() {
					super.adminGranted();
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_ENABLE_START_GAME_BUTTON);
				}
			});

			try {
				mPublicChat.join(nick);
			} catch (Exception e) {
				Log.w(getClass().getSimpleName(), "Failed to join Multiuser Chat (" + e.getMessage() + "). Maybe the game has already begun");
				return false;
			}
		}
		
		// initialise private MUC chat for messages to ninecards service
		/*if(mPrivateMucChat == null) {
			for(Iterator<String> it = mPublicChat.getOccupants(); it.hasNext();) {
				String id = it.next();
				if(id.toLowerCase().endsWith(serviceNick.toLowerCase())) {
					mPrivateMucChat = mPublicChat.createPrivateChat(id, mChatMessageListener);
					break;
				}
			}
		}*/

		// initialise private chat for messages to ninecards service
		String serviceJID = mBgService.getGameServiceJID();
		/*try {
			for(Affiliate a : mPublicChat.getOwners()) { 
				serviceJID = a.getJid();
				break;
			}
		} catch(Exception e) { System.out.println("Failed to get service JID (" + e.getMessage() + ")"); }*/

		mPrivateChat = mXmppConnection.getChatManager().createChat(serviceJID, mChatMessageListener);
		
		// also listen to messages of private chats which were initiated by the other side
		mXmppConnection.getChatManager().addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				if(!createdLocally)
					chat.addMessageListener(mChatMessageListener);
			}
		});

		// add own player and others who have already joined the public chat to players list.
		// it sometimes takes a while until own player appears in occupants list, that's why the while loop is needed
		while(!mBgService.getGame().getPlayers().containsKey(getMyChatID())) {
			
			for(Iterator<String> it = mPublicChat.getOccupants(); it.hasNext();) {
				String occupantID = it.next();
				
				if(!occupantID.toLowerCase().endsWith(serviceNick.toLowerCase())
						&& !mBgService.getGame().getPlayers().containsKey(occupantID)) {
					Player player = new Player(occupantID);
					mBgService.getGame().getPlayers().put(occupantID, player);
				}
			}
		}
		mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
		
		return true;
	}
	
	
	/**
	 * Checks if the application is connected to the XMPP server.
	 * 
	 * @return true if the aplication is connected, false if not
	 */
	public boolean isConnected()
	{
		return mXmppConnection != null && mXmppConnection.isConnected();
	}
	
	
	/**
	 * Leave the multiuser chat.
	 */
	public void leaveChat()
	{
		if(mPublicChat != null && isConnected())// && publicChat.isJoined())
			mPublicChat.leave();
		
		mPublicChat = null;
		mPrivateChat = null;
	}
	
	
	/**
	 * Disconnect from XMPP server.
	 */
	public void disconnectFromXmppServer()
	{
		if(isConnected()) {			
			Presence presence = new Presence(Presence.Type.unavailable);
			presence.setStatus("User disconnected from XMPP Server");
			mXmppConnection.disconnect(presence);
		}

		mXmppConnection = null;
	}

// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Sends a ServiceDiscoveryBean to the Mobilis Server (not to be confused with Ninecards Service).
	 * A MobilisServiceDiscoveryBean with null as namespace returns all provided services,
     * a proper service namespace lets the server send information about all instances of the specified service.
     * 
	 * @param namespace null for general information about running services, or a service namespace for information about service instances
	 */
	public void sendServiceDiscovery(String namespace)
	{
		if(!isConnected())
			connectToXmppServer(mServer, mUserJid, mUserPw);
		
		if(isConnected()) {
			MobilisServiceDiscoveryBean bean = new MobilisServiceDiscoveryBean(namespace, Integer.MIN_VALUE, false);
			
			bean.setType(XMPPBean.TYPE_GET);
			bean.setFrom(mBgService.getUserJID());
			bean.setTo(mBgService.getMobilisServerJID());
			
			mXmppConnection.sendPacket(new BeanIQAdapter(bean));
			Log.i(getClass().getSimpleName(), "MobilisServiceDiscoveryBean sent (" + (new BeanIQAdapter(bean)).toXML() + ")");
		}

		else Log.w(ServerConnection.class.getSimpleName(), "Couldn't send MobilisServiceDiscoveryBean (couldn't connect to server)");
	}
	
	
	/**
	 * Sends a CreateNewServiceInstanceBean to the Mobilis Coordinator service.
	 * 
	 * @param serviceNamespace the service namespace for which a new instance shall be created
	 * @param serviceName the name for the service instance, that is the name of the new game
	 * @param servicePassword the service password which can be set to prevent others from joining
	 */
	public void sendCreateNewServiceInstance(String serviceNamespace, String serviceName, String servicePassword)
	{
		if(!isConnected())
			connectToXmppServer(mServer, mUserJid, mUserPw);
		
		if(isConnected()) {
			CreateNewServiceInstanceBean bean = new CreateNewServiceInstanceBean(serviceNamespace, servicePassword);
			
			bean.setServiceName(serviceName);
			bean.setType(XMPPBean.TYPE_SET);
			bean.setFrom(mBgService.getUserJID());
			bean.setTo(mBgService.getMobilisServerJID());
			
			mXmppConnection.sendPacket(new BeanIQAdapter(bean));
			Log.i(getClass().getSimpleName(), "CreateNewServiceInstanceBean sent (" + (new BeanIQAdapter(bean)).toXML() + ")");
		}
		
		else Log.w(ServerConnection.class.getSimpleName(), "Couldn't send CreateNewServiceInstanceBean (couldn't connect to server)");
	}
	
	
	/**
	 * Sends a ConfigureGameRequest containing specific game settings.
	 * 
	 * @param players the maximum number of players
	 * @param rounds the number of rounds
	 */
	public void sendGameConfiguration(int players, int rounds)
	{
		if(!isConnected())
			connectToXmppServer(mServer, mUserJid, mUserPw);
		
		if(isConnected()) {
			ConfigureGameRequest bean = new ConfigureGameRequest(players, rounds);

			bean.setType(XMPPBean.TYPE_SET);
			bean.setFrom(mBgService.getUserJID());
			bean.setTo(mBgService.getGameServiceJID());
			
			mXmppConnection.sendPacket(new BeanIQAdapter(bean));
			Log.i(getClass().getSimpleName(), "ConfigureGameBean sent (" + (new BeanIQAdapter(bean)).toXML() + ")");
		}

		else Log.w(ServerConnection.class.getSimpleName(), "Couldn't send ConfigureGameRequest (couldn't connect to server)");
	}
	
	
	/**
	 * Sends a GetGameConfigurationRequest to the game service.
	 */
	public void sendGetGameConfiguration()
	{
		if(!isConnected())
			connectToXmppServer(mServer, mUserJid, mUserPw);
		
		if(isConnected()) {
			GetGameConfigurationRequest bean = new GetGameConfigurationRequest();
			
			bean.setType(XMPPBean.TYPE_SET);
			bean.setFrom(mBgService.getUserJID());
			bean.setTo(mBgService.getGameServiceJID());
			
			mXmppConnection.sendPacket(new BeanIQAdapter(bean));
			Log.i(getClass().getSimpleName(), "GetGameConfigurationRequest sent (" + (new BeanIQAdapter(bean)).toXML() + ")");
		}
	}
	
	
	/**
	 * Send an XMPPBean of type ERROR.
	 * 
	 * @param inBean the XMPPBean to reply to with an ERROR. The payload will be copied.
	 */
	public void sendXMPPBeanError(XMPPBean inBean)
	{
		XMPPBean resultBean = inBean.clone();
		resultBean.setTo(inBean.getFrom());
		resultBean.setFrom(mBgService.getUserJID());
		resultBean.setType(XMPPBean.TYPE_ERROR);
		
		mXmppConnection.sendPacket(new BeanIQAdapter(resultBean));
		Log.i(getClass().getSimpleName(), "ErrorBean sent (" + (new BeanIQAdapter(resultBean)).toXML() + ")");
	}

// -------------------------------------------------------------------------------------------------------------------------------

	/**
	 * For sending messages via private chat to the game service. Used for StartGameMessages and PlayCardMessages.
	 * 
	 * @param xmppInfo the message to be sent.
	 * @return true if sending was successful, false if not
	 */
	public boolean sendPrivateToService(XMPPInfo xmppInfo)
	{
		Message mesg = new Message();
		mesg.setBody("<" + xmppInfo.getChildElement() + " xmlns=\"http://mobilis.inf.tu-dresden.de/apps/9cards\">"
				+ xmppInfo.toXML()
				+ "</" + xmppInfo.getChildElement() + ">");
		
		try {
			mPrivateChat.sendMessage(mesg);
			Log.i(getClass().getSimpleName(), "Private Message sent: " + mesg.toXML());
		} catch (Exception e) {
			Log.w(getClass().getSimpleName(), "Failed to send start game message (" + e.getClass().toString() + ": " + e.getMessage() + ")");
			return false;
		}
		
		return true;
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Listens for packets from the Mobilis Server (not to be confused with Ninecards Service).
	 * Received packets will be converted to XMPPBeans and be passed to the current GameState for further processing.
	 */
	private PacketListener mPacketListener = new PacketListener() {

		@Override
		public void processPacket(Packet packet) {
			Log.i("ServerConnection.mPacketListener", "Packet received (" + packet.toXML() + ")");
			
			// needed for some Android versions (observed crash on 2.3.3 when missing)
			try { Looper.prepare(); }
			catch (Exception ignore) {}
			
			if(packet instanceof IQ) {
				IQ iq = (IQ) packet;
				XMPPBean bean = null;
				
				if(iq.getChildElementXML().toLowerCase().startsWith("<servicediscovery"))
					bean = new MobilisServiceDiscoveryBean();
				
				else if(iq.getChildElementXML().toLowerCase().startsWith("<createnewserviceinstance"))
					bean = new CreateNewServiceInstanceBean();
				
				else if(iq.getChildElementXML().toLowerCase().startsWith("<sendnewserviceinstance"))
					bean = new SendNewServiceInstanceBean();
				
				else if(iq.getChildElementXML().toLowerCase().startsWith("<configuregameresponse"))
					bean = new ConfigureGameResponse();
				
				else if(iq.getChildElementXML().toLowerCase().startsWith("<getgameconfigurationresponse"))
					bean = new GetGameConfigurationResponse();
				
				if(bean != null) {
					try {
						XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
						xmlParser.setInput(new StringReader(iq.getChildElementXML()));
						bean.fromXML(xmlParser);
					} catch (Exception e) {
						Log.e("ServerConnection.mPacketListener", "Failed to parse XML (" + e.getMessage() + ")");
						return;
					}

					mBgService.getGameState().processPacket(bean);
				}
				
				else Log.w("ServerConnection.mPacketListener", "Unhandled IQ type received! (" + iq.getChildElementXML() + ")");
			}
		}
	};
	
	
	/**
	 * Listens for private chat messages. Currently not needed.
	 */
	private MessageListener mChatMessageListener = new MessageListener()
	{
		@Override
		public void processMessage(Chat chat, Message message) {
			Log.i(getClass().getSimpleName(), "Received private chat message: " + message.getBody());
		}
	};
	
	
	/**
	 * Listens for "group chat" messages. Received messages will be converted to XMPPInfos and be passed
	 * to the current GameState for further processing.
	 */
	private PacketListener mMucMessageListener = new PacketListener()
	{
		@Override
		public void processPacket(Packet packet) {
			Log.i("ServerConnection.mMucMessageListener", "Received public MUC message: " + packet.toXML());
			
			if(packet instanceof Message) {
				Message mesg = (Message) packet;
				XMPPInfo info = null;
				
				if(mesg.getBody().toLowerCase().contains("gamestartsmessage"))
					info = new GameStartsMessage();
				if(mesg.getBody().toLowerCase().contains("cardplayedmessage"))
					info = new CardPlayedMessage();
				if(mesg.getBody().toLowerCase().contains("roundcompletemessage"))
					info = new RoundCompleteMessage();
				if(mesg.getBody().toLowerCase().contains("gameovermessage"))
					info = new GameOverMessage();
			
				if(info != null) {
					try {
						XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
						xmlParser.setInput(new StringReader(mesg.getBody()));
						info.fromXML(xmlParser);
					} catch (Exception e) {
						Log.e("ServerConnection.mMucMessageListener", "Failed to parse XML (" + e.getMessage() + ")");
						return;
					}

					mBgService.getGameState().processChatMessage(info);
				}
				
				else Log.w("ServerConnection.mMucMessageListener", "Unhandled Message type received! (" + mesg.getBody() + ")");
			}
		}
	}; 
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Listens for joining or leaving of players and updates the user list in the GUI.
	 */
	private ParticipantStatusListener mParticipantStatusListener = new ParticipantStatusListener()
	{
		@Override
		public void joined(String participant) {
			if(!participant.toLowerCase().endsWith(serviceNick.toLowerCase()) && !mBgService.getGame().getPlayers().containsKey(participant)) {
				mBgService.getGame().getPlayers().put(participant, new Player(participant));
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void left(String participant) {
			if(mBgService.getGame().getPlayers().containsKey(participant)) {
				mBgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void kicked(String participant, String actor, String reason) {
			if(mBgService.getGame().getPlayers().containsKey(participant)) {
				mBgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void banned(String participant, String actor, String reason) {
			if(mBgService.getGame().getPlayers().containsKey(participant)) {
				mBgService.getGame().getPlayers().remove(participant);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			}
		}
		
		@Override
		public void nicknameChanged(String participant, String newNickname) {
			if(mBgService.getGame().getPlayers().containsKey(participant)) {
				String newID = mPublicChat.getRoom() + "/" + newNickname;
				mBgService.getGame().changePlayerID(participant, newID);
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
				Log.i("ServerConnection", "Player " + participant + " changed nick to " + newNickname);
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
	 * Returns the own users ID in the multiuser chat room.
	 * 
	 * @return the user's ID (example: room@conference.jabber.org/nick)
	 */
	public String getMyChatID()
	{
		if(mPublicChat != null)
			return mPublicChat.getRoom() + "/" + mPublicChat.getNickname();
	
		return null;
	}
	
	
	/**
	 * Changes the nickname of the own player inside of the multiuser chat room as well as in the game's players list.
	 * Currently not used.
	 * 
	 * @param newNick the new nickname for the own player
	 */
	public void changeMyNick(String newNick)
	{
		try {
			String oldID = getMyChatID();
			mPublicChat.changeNickname(newNick);
			mBgService.getGame().changePlayerID(oldID, getMyChatID());
			mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
		} catch (XMPPException e) {
			Log.e(this.getClass().getSimpleName(), "Failed to change own nick (" + e.getMessage() + " / " + e.getXMPPError() + ")");
		}
	}
	
	
	/**
	 * Determines whether the own user has administrator affiliation in the multiuser chat room.
	 * Only works reliably if the user has permission to request a list of all administrators of the muc room.
	 * 
	 * @return true if the user is administrator in the muc room, false if not or if he doesn't have rights to request a list of administrators
	 */
	public boolean isPlayerAdmin()
	{
		boolean res = false;
		
		try {
			for(Affiliate aff : mPublicChat.getAdmins()) {
				if(aff.getNick().equals(mPublicChat.getNickname()))
					res = true;
			}
		} catch (XMPPException e) {
			Log.w("ServerConnection", "Failed to check if user is admin (" + e.getMessage() + " / " + e.getXMPPError());
		}

		return res;
	}
}
