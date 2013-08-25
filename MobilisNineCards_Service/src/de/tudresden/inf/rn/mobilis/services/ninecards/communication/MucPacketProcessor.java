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

import org.jivesoftware.smack.packet.Message;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import de.tudresden.inf.rn.mobilis.services.ninecards.Game.State;
import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.Player;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GameOverMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.RoundCompleteMessage;
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
		String startPrefix = "<mobilismessage type=" + StartGameMessage.class.getSimpleName().toLowerCase() + ">";
		String cardPrefix = "<mobilismessage type=" + PlayCardMessage.class.getSimpleName().toLowerCase() + ">";
		
		// check type of message and handle it accordingly
		if(message.getBody().toLowerCase().startsWith(startPrefix))
			onStartGame(message);
		else if(message.getBody().toLowerCase().startsWith(cardPrefix))
			onPlayCard(message);
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onStartGame(Message message) {
		
		// only allowed in gamestate ready
		if(mServiceInstance.getGame().getGameState() == State.READY) {
			
			// check if player is allowed to start the game
			Player player = mServiceInstance.getGame().getPlayer(message.getFrom());
			
			//TODO soll dann später nur über die admin-affiliation laufen, erst testen ob der participantlistener die jid der neuen spieler kennt
			if(mServiceInstance.getMucConnection().isAdmin(message.getFrom())
					|| ((player != null) && (player.isCreator()))) {

				// close game for joining
				mServiceInstance.getMucConnection().lockMuc();
				
				// set game state and prepare first round
				mServiceInstance.getGame().setGameState(State.PLAYING);
				mServiceInstance.getGame().startNewRound();
				
				// inform all players about start of game
				mServiceInstance.getMucConnection().sendMessagetoMuc(
						new StartGameMessage(mServiceInstance.getSettings().getRounds()));
			}
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onPlayCard(Message message) throws Exception {
		
		// reconstruct PlayCardMessage
		String prefix = "<mobilismessage type=" + PlayCardMessage.class.getSimpleName().toLowerCase() + ">";
		String content = message.getBody().toLowerCase().substring(
				message.getBody().indexOf(prefix) + prefix.length(),
				message.getBody().indexOf("</mobilismessage>"));

		PlayCardMessage playCardMesg = new PlayCardMessage();
		//xmlParser.setInput(new StringReader(content));
		//playCardMesg.fromXML(xmlParser);
		
String round = content.substring(content.indexOf("<round>" + "<round>".length(), content.indexOf("</round>")));
String card = content.substring(content.indexOf("<card>" + "<card>".length(), content.indexOf("</card>")));
playCardMesg.setRound(Integer.parseInt(round));
playCardMesg.setCard(Integer.parseInt(card));		

		// act depending on message
		Player player = mServiceInstance.getGame().getPlayer(message.getFrom());
		if(player != null) {
			// check if player already played a card this round
			if(player.getChosenCard() == -1) {
				// check if player already used this card
				if(!player.getUsedCards().contains(playCardMesg.getCard())) {
					// mark this card as used and set player to alreadyChose
					player.getUsedCards().add(playCardMesg.getCard());
					player.setChosenCard(playCardMesg.getCard());
					// inform other players that this player chose some card
					mServiceInstance.getMucConnection().sendMessagetoMuc(
							new CardPlayedMessage(mServiceInstance.getGame().getRound(), player.getJid()));
				}
			}
		}

		// check if round is finished
		if(mServiceInstance.getGame().checkRoundOver()) {
			
			// increment winner's number of wins
			mServiceInstance.getGame().getRoundWinner().incrementRoundsWon();
			
			// check if end of game is reached. if true, also shutdown service
			if(mServiceInstance.getGame().getRound() == mServiceInstance.getSettings().getRounds()) {
				mServiceInstance.getMucConnection().sendMessagetoMuc(
						new GameOverMessage(
								mServiceInstance.getGame().getGameWinner().getJid(),
								mServiceInstance.getGame().getGameWinner().getRoundsWon()));
				mServiceInstance.shutdown();
			}
			
			// else start next round
			else {
				mServiceInstance.getMucConnection().sendMessagetoMuc(
						new RoundCompleteMessage(
								mServiceInstance.getGame().getRound(),
								mServiceInstance.getGame().getRoundWinner().getJid(),
								mServiceInstance.getGame().getPlayerInfos()));
				mServiceInstance.getGame().startNewRound();
			}
		}
	}
}
