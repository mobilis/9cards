package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import java.util.Iterator;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.MessageWrapper;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.RoundCompleteMessage;

public class MucConnection implements PacketListener {
	
	/** The Mobilis Service 9Cards Instance */
	private NineCardsService mServiceInstance;
	/** The Multi User Chat Instance */
	private MultiUserChat muc;
	/** The class which processes chat packets. */
	private MucPacketProcessor packetProcessor;

	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());
	
	public static final String TYPE_STARTGAME = "StartGame";
	public static final String TYPE_ROUNDCOMPLETE = "RoundComplete";
	public static final String TYPE_PLAYCARD = "PlayCard";
	public static final String TYPE_PLAYERLEAVING = "PlayerLeaving";
	
	
	/**
	 * Standard Constructor, calls openMultiUserChat().
	 * @param serviceInstance
	 * @throws Exception
	 */
	public MucConnection(NineCardsService serviceInstance) throws Exception {
		this.mServiceInstance = serviceInstance;
		this.packetProcessor = new MucPacketProcessor(mServiceInstance);
		openMultiUserChat();
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void sendMessagetoMuc(Message message) {
		try {
			muc.sendMessage(message);
		} catch (Exception e) {
			LOGGER.severe("failed to send message to muc! (" + e.getClass() + " - " + e.getMessage() + ")");
		}
	}
	
	
	/**
	 * 
	 * @param wasLastRound
	 */
	public void sendRoundCompleteMessage(boolean wasLastRound) {
		RoundCompleteMessage roundMesg = new RoundCompleteMessage();
		
		roundMesg.setRoundID(mServiceInstance.getGame().getRound());
		roundMesg.setRoundWinnersName(mServiceInstance.getGame().getRoundWinner().getName());
		roundMesg.setRoundWinnersJID(mServiceInstance.getGame().getRoundWinner().getJid());
		roundMesg.setPlayerInfos(mServiceInstance.getGame().getPlayerInfos());
		roundMesg.setEndOfGame(wasLastRound);
		
		MessageWrapper wrapper = new MessageWrapper(true, roundMesg.toXML(), MucConnection.TYPE_ROUNDCOMPLETE);
		Message finalMesg = new Message();
		finalMesg.setBody(wrapper.toXML());
		sendMessagetoMuc(finalMesg);
	}
	

	@Override
	public void processPacket(Packet packet) {
		if(packet instanceof Message) {
			Message mesg = (Message) packet;
			
			if(mesg.getBody() != null) {
				try {
					LOGGER.info("processing incoming chat packet: " + mesg.getFrom() + " - " + mesg.getBody());
					packetProcessor.processPacket(mesg);
				} catch (Exception e) {
					LOGGER.severe("failed to process incoming chat package (" + e.getClass() + " - " + e.getMessage() + ")");
				}
			}
		}
	}
	
	
	/**
	 * Opens the MultiUserChat with initialized members.
	 * @throws XMPPException an XMPP exception
	 */
	public void openMultiUserChat() throws XMPPException {
		muc = new MultiUserChat(mServiceInstance.getAgent().getConnection(), mServiceInstance.getSettings().getChatID());
		muc.create("Server");
		
		Form oldForm = muc.getConfigurationForm();
		Form newForm = oldForm.createAnswerForm();
		
		for (Iterator<FormField> fields = oldForm.getFields(); fields.hasNext();) {
		    FormField field = (FormField) fields.next();
		    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
		    	newForm.setDefaultAnswer(field.getVariable());
		    }
		}
		
		newForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
		newForm.setAnswer("muc#roomconfig_roomsecret", mServiceInstance.getSettings().getChatPW());
		
		muc.sendConfigurationForm(newForm);
		
		LOGGER.info("Chat created (ID: " + mServiceInstance.getSettings().getChatID()
				+ ", Pw: " + mServiceInstance.getSettings().getChatPW() +")");

	}
	
	
	/**
	 * Kicks a player from the chat.
	 * @param jid The JabberID of the player.
	 */
	public void removePlayerFromChat(String jid){
		try {
			muc.kickParticipant(jid, "No reason");
		} catch (XMPPException e) {
			LOGGER.severe("failed to remove player " + jid + " from chat: " + e.getMessage());
		}
	}

	
	/**
	 * Closes the MultiUserChat.
	 * @throws XMPPException the XMPP exception
	 */
	public void closeMultiUserChat() throws XMPPException{
		if(muc.isJoined()) {
			muc.destroy("", "");
		}		
	}
}
