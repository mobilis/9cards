package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import java.util.Iterator;
import java.util.logging.Logger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.Occupant;
import org.jivesoftware.smackx.muc.ParticipantStatusListener;

import de.tudresden.inf.rn.mobilis.services.ninecards.Game;
import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.Player;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GameOverMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class MucConnection implements PacketListener, MessageListener
{
	/** The Mobilis Service 9Cards Instance */
	private NineCardsService mServiceInstance;
	/** The class which processes chat packets. */
	private MucPacketProcessor packetProcessor;
	/** The Multi User Chat Instance */
	private MultiUserChat muc;
	/** The password for re-entering the muc room after it was locked. */
	private String mucPw;

	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());
	
	
	/**
	 * Standard Constructor, calls createMultiUserChat() and adds a listener for new users.
	 * @param serviceInstance
	 * @throws Exception
	 */
	public MucConnection(NineCardsService serviceInstance) throws Exception
	{
		this.mServiceInstance = serviceInstance;
		this.packetProcessor = new MucPacketProcessor(mServiceInstance);
	}
	
	
	/**
	 * Creates a new MUC room.
	 * 
	 * @throws XMPPException
	 */
	public void createMultiUserChat()
	{
		if(!mServiceInstance.getAgent().getConnection().isConnected())
			LOGGER.severe("Couldn't create MUC (no connection)!");
		
		if(this.muc != null) {
			LOGGER.warning("Couldn't create MUC because it already existed!");
			return;
		}

		try {
			muc = new MultiUserChat(mServiceInstance.getAgent().getConnection(), mServiceInstance.getSettings().getChatID());
			muc.create("9Cards-Service");

			Form cnfgForm = muc.getConfigurationForm().createAnswerForm();
			for (Iterator<FormField> fields = cnfgForm.getFields(); fields.hasNext();) {
			    FormField field = fields.next();
			    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
			    	cnfgForm.setDefaultAnswer(field.getVariable());
			    }
			}
			
			muc.sendConfigurationForm(cnfgForm);
			
			muc.addParticipantStatusListener(mParticipantStatusListener);
			muc.addMessageListener(this);
			mServiceInstance.getAgent().getConnection().getChatManager().addChatListener(
					new ChatManagerListener() {
						@Override
						public void chatCreated(Chat chat, boolean createdLocally) {
							chat.addMessageListener(MucConnection.this);
						}
					});
			
			LOGGER.info("Chatroom created (ID: " + mServiceInstance.getSettings().getChatID());
			
		} catch (Exception e) {
			LOGGER.severe("Failed to create MUC! (" + e.getMessage() + ")");
			mServiceInstance.shutdown();
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void sendMessagetoMuc(XMPPInfo message)
	{
		String body = "";
		body += "<MobilisMessage type='" + message.getClass().getSimpleName() + "'>";
		body += message.toXML();
		body += "</MobilisMessage>";
		
		try {
			Message msg = new Message();
			msg.setBody(body);
			msg.setTo(muc.getRoom());
			msg.setType(Message.Type.groupchat);
			muc.sendMessage(msg);
		} catch (Exception e) {
			LOGGER.severe("failed to send message to muc! (" + e.getClass() + " - " + e.getMessage() + ")");
		}
	}
	
	
	/**
	 * Prevents new players from joining by setting a secret password for muc. This is necessary because for maxusers,
	 * Smack only values out of { 10, 20, 30, 50, 100, None } (see http://xmpp.org/extensions/xep-0045.html#roomconfig)
	 */
	public void lockMuc()
	{
		try {
			Form cnfgForm = muc.getConfigurationForm().createAnswerForm();
			
			for (Iterator<FormField> fields = cnfgForm.getFields(); fields.hasNext();) {
			    FormField field = fields.next();
			    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
			    	cnfgForm.setDefaultAnswer(field.getVariable());
			    }
			    
				cnfgForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
				cnfgForm.setAnswer("muc#roomconfig_roomsecret", getMucPw());

				muc.sendConfigurationForm(cnfgForm);
				LOGGER.info("MUC was locked with secret password");
			}
			
		} catch (Exception e) { LOGGER.severe("Failed to lock MUC (" + e.getMessage() + ")"); }
	}
	
	
	/**
	 * Returns the password for re-entering the muc room after it was locked.
	 * @return
	 */
	public String getMucPw()
	{
		if(mucPw == null)
			mucPw = "9Cards#" + System.currentTimeMillis();
		return mucPw;
	}
	
	
	/*
	 * Receives all groupchat messages which are sent inside the muc room (also own messages).
	 * 
	 * (non-Javadoc)
	 * @see org.jivesoftware.smack.PacketListener#processPacket(org.jivesoftware.smack.packet.Packet)
	 */
	@Override
	public void processPacket(Packet packet)
	{
		if(packet instanceof Message) {
			Message mesg = (Message) packet;
			
			if(mesg.getBody() != null) {
				try {
					LOGGER.info("processing incoming groupchat packet: " + mesg.getFrom() + " - " + mesg.getBody());
					packetProcessor.processPacket(mesg);
				} catch (Exception e) {
					LOGGER.severe("failed to process incoming chat package (" + e.getClass() + " - " + e.getMessage() + ")");
				}
			}
		}
	}


	/*
	 * Receives messages which are being sent via private chat.
	 * 
	 * (non-Javadoc)
	 * @see org.jivesoftware.smack.MessageListener#processMessage(org.jivesoftware.smack.Chat, org.jivesoftware.smack.packet.Message)
	 */
	@Override
	public void processMessage(Chat chat, Message mesg)
	{
		if(mesg.getBody() != null) {
			try {
				if(mesg.getFrom() == null)
					mesg.setFrom(chat.getParticipant());
				LOGGER.info("processing incoming private chat message: " + mesg.getFrom() + " - " + mesg.getBody());
				packetProcessor.processPacket(mesg);
			} catch (Exception e) {
				LOGGER.severe("failed to process incoming chat package (" + e.getClass() + " - " + e.getMessage() + ")");
			}
		}
	}
	
	
	/**
	 * Kicks a player from the chat.
	 * @param fullID The ID of the player (example: room@conference.jabber.org/nick)
	 */
	public void removePlayerFromChat(String fullID, String reason)
	{
		String nick = StringUtils.parseResource(fullID);
		try {
			muc.kickParticipant(nick, reason);
			LOGGER.info("kicked player " + nick + " from chat (reason: " + reason + ")");
		} catch (Exception e) {
			LOGGER.severe("failed to remove player " + nick + " from chat: " + e.getMessage());
		}
	}

	
	/**
	 * Closes the MultiUserChat.
	 * @throws XMPPException the XMPP exception
	 */
	public void closeMultiUserChat(String reason)
	{
		try {
			muc.destroy(reason, null);
			LOGGER.info("Muc room destroyed (reason: " + reason + ")");
		} catch (Exception e) {
			String excMessage = e instanceof XMPPException
					? e.getMessage() + " / " + ((XMPPException) e).getXMPPError()
					: e.getMessage();
			LOGGER.warning("Failed to destroy muc room (" + excMessage + ")");
		}
	}
	
	
	/**
	 * 
	 * @param fullID
	 * @return
	 */
	public boolean isAdmin(String fullID)
	{
		String nick = StringUtils.parseResource(fullID);
		boolean res = false;
		
		try {
			for(Affiliate aff : muc.getAdmins())
				if(aff.getNick().equals(nick))
					res = true;	
		} catch(Exception e) {
			LOGGER.severe("Failed to determine whether " + fullID + "is admin");
		}
		
		return res;
	}
	
	
	/**
	 * 
	 * @param fullID The ID of the player (example: room@conference.jabber.org/nick)
	 * @return the bare XMPP user ID of the user (e.g. "user@host.org")
	 */
	private String getJid(String fullID)
	{
		String nick = StringUtils.parseResource(fullID);
		String jid = null;
		
		try {
			for(Occupant occ : muc.getParticipants())
				if(occ.getNick().equals(nick))
					jid = occ.getJid();
		} catch (XMPPException e) {
			LOGGER.severe("Failed to get JID of player " + fullID + " (" + e.getMessage() + " / " + e.getXMPPError());
		}
		
		return jid;
	}
	
	
	/**
	 * 
	 */
	public void checkRoundOver()
	{
		// check if round is finished
		if(mServiceInstance.getGame().checkRoundOver()) {
			
			// if true, get winner and increment his score
			mServiceInstance.getGame().getRoundWinner().incrementRoundsWon();
			
			// check if end of game is reached
			if(mServiceInstance.getGame().getRound() == mServiceInstance.getSettings().getRounds()) {
				sendMessagetoMuc(
						new GameOverMessage(
								mServiceInstance.getGame().getGameWinner().getID(),
								mServiceInstance.getGame().getGameWinner().getRoundsWon(),
								mServiceInstance.getGame().getPlayerInfos()));
			}
			
			// else start next round
			else {
				sendMessagetoMuc(
						new RoundCompleteMessage(
								mServiceInstance.getGame().getRound(),
								mServiceInstance.getGame().getRoundWinner().getID(),
								mServiceInstance.getGame().getPlayerInfos()));
				mServiceInstance.getGame().startNewRound();
			}
		}
	}
	
	
	/**
	 * Handles players who leave the room on their own or are kicked.
	 */
	private ParticipantStatusListener mParticipantStatusListener = new ParticipantStatusListener()
	{
		@Override
		public void left(String participant)
		{
			Player player = mServiceInstance.getGame().removePlayer(participant, "player left");
			if(mServiceInstance.getGame().getPlayers().size() > 0 && (player != null && player.getChosenCard() == -1))
				checkRoundOver();
		}

		@Override
		public void kicked(String participant, String actor, String reason)
		{
			Player player = mServiceInstance.getGame().removePlayer(participant, "player was kicked by "
					+ actor.substring(0, actor.indexOf("@")) + " (reason: " + reason + ")");
			
			if(mServiceInstance.getGame().getPlayers().size() > 0 && (player != null && player.getChosenCard() == -1))
				checkRoundOver();
		}
		
		@Override
		public void banned(String participant, String actor, String reason)
		{
			Player player = mServiceInstance.getGame().removePlayer(participant, "player was banned by "
					+ actor.substring(0, actor.indexOf("@")) + " (reason: " + reason + ")");

			if(mServiceInstance.getGame().getPlayers().size() > 0 && (player != null && player.getChosenCard() == -1))
				checkRoundOver();
		}

		@Override
		public void joined(String participant)
		{
			// if game was not configured yet or has already begun, joining is not allowed
			if(mServiceInstance.getGame().getGameState() != Game.State.READY) {
				LOGGER.warning(participant + " tried to enter MUC in gamestate " + mServiceInstance.getGame().getGameState());
				removePlayerFromChat(participant, "joining only allowed in gamestate 'ready'");
			}
			
			else {
				// add player if he's not already joined
				if(!mServiceInstance.getGame().getPlayers().containsKey(participant)) {
					mServiceInstance.getGame().addPlayer(new Player(participant));
					
					// if he's the first one, he is the creator of the game and will be assigned admin affiliation
					if (mServiceInstance.getGame().getPlayers().size() == 1) {
						try { muc.grantAdmin(getJid(participant)); }
						catch (Exception e) { LOGGER.severe("Failed to assign admin affiliation (" + e.getMessage() + ")"); };
					}

					// if he's a regular player, he'll be assigned membership affiliation which provides less rights
					else {
						try { muc.grantMembership(getJid(participant)); }
						catch (Exception e) { LOGGER.severe("Failed to assign member affiliation (" + e.getMessage() + ")"); };
					}
					
					// if max. number of players is reached, close MUC to prevent further joining
					if(mServiceInstance.getGame().getPlayers().size() == mServiceInstance.getSettings().getMaxPlayers())
						lockMuc();
				}
			}
		}
		
		

		@Override
		public void nicknameChanged(String participant, String newNickname)
		{
			if(mServiceInstance.getGame().getPlayers().containsKey(participant)) {
				String newID = muc.getRoom() + "/" + newNickname;
				Player player = mServiceInstance.getGame().getPlayers().remove(participant);
				player.changeID(newID);
				mServiceInstance.getGame().getPlayers().put(newID, player);
				
				LOGGER.info("Participant " + participant + " changed his nickname to " + newNickname);
			}
		}

		
		@Override
		public void adminGranted(String participant) {}

		@Override
		public void adminRevoked(String participant) {}
		
		@Override
		public void membershipGranted(String participant) {}

		@Override
		public void membershipRevoked(String participant) {}

		@Override
		public void moderatorGranted(String participant) {}

		@Override
		public void moderatorRevoked(String participant) {}

		@Override
		public void ownershipGranted(String participant) {}

		@Override
		public void ownershipRevoked(String participant) {}

		@Override
		public void voiceGranted(String participant) {}

		@Override
		public void voiceRevoked(String participant) {}
		
	};
}
