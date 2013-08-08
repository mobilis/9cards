package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import java.util.Iterator;
import java.util.logging.Logger;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.MessageWrapper;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayerInfosMessage;
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
	
	public static final String TYPE_PLAYERINFOS = "PlayerInfos";
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
System.out.println("sende Nachricht an Muc: " + message.getBody());

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
	
	
	public void sendPlayerInfosMessage() {
		PlayerInfosMessage playersMsg = new PlayerInfosMessage();
		playersMsg.setPlayers(mServiceInstance.getGame().getPlayerInfos());
		
		MessageWrapper wrapper = new MessageWrapper(true, playersMsg.toXML(), MucConnection.TYPE_PLAYERINFOS);
		Message finalMesg = new Message();
		finalMesg.setBody(wrapper.toXML());
System.out.println("Sende PlayerInfosMessage: " + finalMesg.getBody());
		sendMessagetoMuc(finalMesg);		
	}
	

	@Override
	public void processPacket(Packet packet) {
System.out.println("MucConnection.processPacket(): " + packet.toXML());
System.out.println("packet instanceof Message: " + (packet instanceof Message));
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
	private void openMultiUserChat() throws XMPPException {
		
		if(!mServiceInstance.getAgent().getConnection().isConnected()) {
			LOGGER.severe("Couldn't open MUC (no connection)!");
			return;
		}

		if(muc == null)
			muc = new MultiUserChat(mServiceInstance.getAgent().getConnection(), mServiceInstance.getSettings().getChatID());

		muc.create("Server");

		org.jivesoftware.smackx.Form cnfgForm = muc.getConfigurationForm().createAnswerForm();
	
		for (Iterator<FormField> fields = cnfgForm.getFields(); fields.hasNext();) {
		    FormField field = (FormField) fields.next();
		    if (!FormField.TYPE_HIDDEN.equals(field.getType()) && field.getVariable() != null) {
		    	cnfgForm.setDefaultAnswer(field.getVariable());
		    }
		}

		cnfgForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
		cnfgForm.setAnswer("muc#roomconfig_roomsecret", mServiceInstance.getSettings().getChatPW());
		
		muc.sendConfigurationForm(cnfgForm);

/*
System.out.println("muc.getMembers().size(): " + muc.getMembers().size() + "; muc.getOccupantsCount(): " + muc.getOccupantsCount());
Iterator<String> i = muc.getOccupants();
while(i.hasNext())
	System.out.println(i.next());
System.out.println("MUC created. Room Name: " + muc.getRoom() + "; PW: " + mServiceInstance.getSettings().getChatPW());
//muc.addMessageListener(this);
//muc.join("Serverarsch", mServiceInstance.getSettings().getChatPW());

MultiUserChat muc2 = new MultiUserChat(mServiceInstance.getAgent().getConnection(), mServiceInstance.getSettings().getChatID());
muc2.join(mServiceInstance.getAgent().getConnection().getUser(), mServiceInstance.getSettings().getChatID());
muc.sendMessage("HUUUHUUUUUUUU");
*/
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
