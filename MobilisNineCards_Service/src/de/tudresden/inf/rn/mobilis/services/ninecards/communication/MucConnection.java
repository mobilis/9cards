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
package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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

/**
 * This class is responsible for managing multiuser and private chat communication.
 * 
 * @author Matthias Köngeter
 *
 */
public class MucConnection implements PacketListener, MessageListener
{

	/** The NineCards game service instance. */
	private NineCardsService mServiceInstance;
	/** The MucPacketProcessor object which is responsible for processing chat messages. */
	private MucPacketProcessor mPacketProcessor;
	/** The multiuser chat Instance */
	private MultiUserChat muc;
	/** The password for re-entering the muc room after it has been locked. */
	private String mucPw;

	private Map<String,String> addressMapper;
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());
	
	
	/**
	 * The constructor for creating a new instance of MucConnection.
	 * 
	 * @param serviceInstance the ninecards game service instance
	 */
	public MucConnection(NineCardsService serviceInstance)
	{
		this.mServiceInstance = serviceInstance;
		this.mPacketProcessor = new MucPacketProcessor(mServiceInstance);
	}
	
	
	/**
	 * Creates a new multiuser chat room.
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
			
			addressMapper = new HashMap<String, String>();
		} catch (Exception e) {
			LOGGER.severe("Failed to create MUC! (" + e.getMessage() + ")");
			mServiceInstance.shutdown();
		}
	}
	
	
	/**
	 * Sends a groupchat message to the multiuser chat room.
	 * 
	 * @param message the message to be sent
	 */
	public void sendMessagetoMuc(XMPPInfo message)
	{
		try {
			Message msg = new Message();
			msg.setBody(message.toXML());
			msg.setTo(muc.getRoom());
			msg.setType(Message.Type.groupchat);
			muc.sendMessage(msg);
		} catch (Exception e) {
			LOGGER.severe("failed to send message to muc! (" + e.getClass() + " - " + e.getMessage() + ")");
		}
	}
	
	
	/**
	 * Prevents new players from joining by setting a secret password for muc. This is necessary because for the maximum number of users,
	 * Smack only allows values out of { 10, 20, 30, 50, 100, None } (see http://xmpp.org/extensions/xep-0045.html#roomconfig).
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
	 * Returns the password for the multiuser chat room after it was locked.
	 * @return
	 */
	public String getMucPw()
	{
		if(mucPw == null)
			mucPw = "9Cards#" + System.currentTimeMillis();
		return mucPw;
	}
	
	public MultiUserChat getMuc()
	{
		return muc;
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
					mPacketProcessor.processPacket(mesg);
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
				mPacketProcessor.processPacket(mesg);
			} catch (Exception e) {
				LOGGER.severe("failed to process incoming chat package (" + e.getClass() + " - " + e.getMessage() + ")");
			}
		}
	}
	
	
	/**
	 * Removes a player from the chat.
	 * 
	 * @param id the ID of the player (example: room@conference.jabber.org/nick)
	 * @param reason the reason for kicking the player
	 */
	public void removePlayerFromChat(String id, String reason)
	{
		String nick = StringUtils.parseResource(id);
		try {
			muc.kickParticipant(nick, reason);
			LOGGER.info("kicked player " + nick + " from chat (reason: " + reason + ")");
		} catch (Exception e) {
			LOGGER.severe("failed to remove player " + nick + " from chat: " + e.getMessage());
		}
	}

	
	/**
	 * Closes the multiuser chat.
	 * 
	 * @param reason the reason for closing the chat
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
	 * Determines whether the player corresponding to the specified ID has admin affiliation in the muc room.
	 * 
	 * @param adminBareJID the id of the player
	 * @return true if player is admin, else if not
	 */
	public boolean isAdmin(String adminBareJID)
	{
		return mServiceInstance.getSettings().getAdminBareJID().equalsIgnoreCase(StringUtils.parseBareAddress(adminBareJID));
	}
	
	
	/**
	 * Returns the full JID of the user corresponding to the specified ID.
	 * 
	 * @param id the ID of the player (example: room@conference.jabber.org/nick)
	 * @return the bare XMPP user ID of the user (e.g. user@host.org)
	 */
	private String getJID(String id)
	{
		String nick = StringUtils.parseResource(id);
		String jid = null;
		
		try {
			for(Occupant occ : muc.getParticipants())
				if(occ.getNick().equals(nick))
					jid = occ.getJid();
		} catch (XMPPException e) {
			LOGGER.severe("Failed to get JID of player " + id + " (" + e.getMessage() + " / " + e.getXMPPError());
		}
		
		return jid;
	}
	
	
	/**
	 * Checks if all players have chosen a card, and if true, sends a RoundCompleteMessage to all players before
	 * starting a new round. If the end of the game is reached, a GameOverMessage is sent instead.
	 */
	public void checkRoundOver()
	{
		// check if round is finished
		if(mServiceInstance.getGame().checkRoundOver()) {
			
			// if true, get winner and increment his score
			mServiceInstance.getGame().getRoundWinner().incrementScore();
			
			// check if end of game is reached
			if(mServiceInstance.getGame().getRound() == mServiceInstance.getSettings().getRounds()) {
				sendMessagetoMuc(
						new GameOverMessage(
								mServiceInstance.getGame().getGameWinner().getID(),
								mServiceInstance.getGame().getGameWinner().getScore(),
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
	 * Handles players who leave the room on their own or are being kicked.
	 */
	private ParticipantStatusListener mParticipantStatusListener = new ParticipantStatusListener()
	{
		@Override
		public void left(String participant)
		{
			Player player = mServiceInstance.getGame().removePlayer(addressMapper.get(participant), "player left");
			if (player != null) {
				removePlayerFromChat(participant, "player left");
			}
			if(mServiceInstance.getGame().getPlayers().size() > 0 && (player != null && player.getChosenCard() == -1))
				checkRoundOver();
		}

		@Override
		public void kicked(String participant, String actor, String reason)
		{
			Player player = mServiceInstance.getGame().removePlayer(addressMapper.get(participant), "player was kicked by "
					+ actor.substring(0, actor.indexOf("@")) + " (reason: " + reason + ")");
			
			if(mServiceInstance.getGame().getPlayers().size() > 0 && (player != null && player.getChosenCard() == -1))
				checkRoundOver();
		}
		
		@Override
		public void banned(String participant, String actor, String reason)
		{
			Player player = mServiceInstance.getGame().removePlayer(addressMapper.get(participant), "player was banned by "
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
				if(!mServiceInstance.getGame().getPlayers().containsKey(getJID(participant))) {
					mServiceInstance.getGame().addPlayer(new Player(getJID(participant)));
					addressMapper.put(participant, getJID(participant));
					
					// check if the joined user is the admin who triggered the ConfigureGameMessage
					if (mServiceInstance.getSettings().getAdminBareJID().equalsIgnoreCase(StringUtils.parseBareAddress(getJID(participant)))) {
						try { muc.grantAdmin(getJID(participant)); }
						catch (Exception e) { LOGGER.severe("Failed to assign admin affiliation (" + e.getMessage() + ")"); };
					}

					// if he's a regular player, he'll be assigned membership affiliation which provides less rights
					else {
						try { muc.grantMembership(getJID(participant)); }
						catch (Exception e) { LOGGER.severe("Failed to assign member affiliation (" + e.getMessage() + ")"); };
					}
					
					// if the maximum number of players is reached, close the multiuser chat room to prevent further joining
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
