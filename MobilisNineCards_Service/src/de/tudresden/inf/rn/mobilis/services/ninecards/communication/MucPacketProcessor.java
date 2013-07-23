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

import java.io.StringReader;

import org.jivesoftware.smack.packet.Message;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.tudresden.inf.rn.mobilis.services.ninecards.Game.State;
import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.Player;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.MessageWrapper;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayerLeavingMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.StartGameMessage;

public class MucPacketProcessor {
	
	/**	The 9Cards service instance. */
	private NineCardsService mServiceInstance;
	/** The XML Pull Parser used for the fromXML() methods. */
	private XmlPullParser xmlParser;
	
	/**
	 * 
	 * @param connection
	 */
	public MucPacketProcessor(NineCardsService serviceInstance) throws Exception {
		this.mServiceInstance = serviceInstance;
		
		XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
		parserFactory.setNamespaceAware(true);
		xmlParser = parserFactory.newPullParser();
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void processPacket(Message message) throws Exception {
		// feed XML parser with message body
		xmlParser.setInput(new StringReader(message.getBody()));
		
		// try to convert the message body back to a Message Wrapper object
		MessageWrapper mesgWrapper = new MessageWrapper();
		mesgWrapper.fromXML(xmlParser);

		// we are only interested in system messages, not normal user chat messages
		if(mesgWrapper.getIsSystemMessage() == false)
			return;
		
		// try message according to its type
		String mesgType = mesgWrapper.getMessageType();
		if(mesgType.equals(MucConnection.TYPE_STARTGAME))
			onStartGame(message);
		else if(mesgType.equals(MucConnection.TYPE_PLAYCARD))
			onPlayCard(mesgWrapper);
		else if(mesgType.equals(MucConnection.TYPE_PLAYERLEAVING))
			onPlayerLeaving(message);
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onStartGame(Message message) {
		// check if player is allowed to start the game
		Player player = mServiceInstance.getGame().getPlayer(message.getFrom());
		if((player != null) && (player.isCreator())) {
			// close game for joining
			mServiceInstance.getGame().setGameOpen(false);
			
			// embed start message into message wrapper
			StartGameMessage startMesg = new StartGameMessage();
			MessageWrapper wrapper = new MessageWrapper(true, startMesg.toXML(), MucConnection.TYPE_STARTGAME);
			// include message wrapper into message which can be sent to muc
			Message finalMesg = new Message();
			finalMesg.setBody(wrapper.toXML());
			mServiceInstance.getMucConnection().sendMessagetoMuc(finalMesg);
			
			// set game state and prepare first round
			mServiceInstance.getGame().setGameState(State.PLAY);
			mServiceInstance.getGame().startNewRound();
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onPlayCard(MessageWrapper wrapper) throws Exception {
		// reconstruct PlayCardMessage
		xmlParser.setInput(new StringReader(wrapper.getMessageString()));
		PlayCardMessage playCardMesg = new PlayCardMessage();
		playCardMesg.fromXML(xmlParser);
		
		Player player = mServiceInstance.getGame().getPlayer(playCardMesg.getPlayersJID());
		if(player != null) {
			// check if player already played a card this round
			if(!(player.getChosenCard() == -1)) {
				// check if player already used this card
				if(player.getUsedCards().contains(playCardMesg.getCardID())) {
					// mark this card as used and set player to alreadyChose
					player.getUsedCards().add(playCardMesg.getCardID());
					player.setChosenCard(playCardMesg.getCardID());
					
					// notify other players that this player chose a card
					PlayCardMessage plCrdMesg = new PlayCardMessage(player.getName(), player.getJid(), 0);
					MessageWrapper wrppr = new MessageWrapper(true, plCrdMesg.toXML(), MucConnection.TYPE_PLAYCARD);
					Message finalMesg = new Message();
					finalMesg.setBody(wrppr.toXML());
					mServiceInstance.getMucConnection().sendMessagetoMuc(finalMesg);
				}
			}
		}
		
		// check if round is finished
		if(mServiceInstance.getGame().checkRoundOver()) {
			// increment winner's number of wins
			mServiceInstance.getGame().getRoundWinner().incrementRoundsWon();
			
			// check if end of game is reached. if true, also shutdown service
			if(mServiceInstance.getGame().getRound() == mServiceInstance.getSettings().getRounds()) {
				mServiceInstance.getMucConnection().sendRoundCompleteMessage(true);
				mServiceInstance.shutdown();
			}
			// else start next round
			else {
				mServiceInstance.getMucConnection().sendRoundCompleteMessage(false);
				mServiceInstance.getGame().startNewRound();
			}
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onPlayerLeaving(Message message) {
		Player player = mServiceInstance.getGame().getPlayer(message.getFrom());
		if(player != null) {
			// remove player from game and chat
			mServiceInstance.getGame().removePlayerByJid(message.getFrom());
			
			// notify other players
			PlayerLeavingMessage leaveMesg = new PlayerLeavingMessage(message.getFrom());
			MessageWrapper wrapper = new MessageWrapper(true, leaveMesg.toXML(), MucConnection.TYPE_PLAYERLEAVING);
			Message finalMesg = new Message();
			finalMesg.setBody(wrapper.toXML());
			mServiceInstance.getMucConnection().sendMessagetoMuc(finalMesg);
		}
	}
}
