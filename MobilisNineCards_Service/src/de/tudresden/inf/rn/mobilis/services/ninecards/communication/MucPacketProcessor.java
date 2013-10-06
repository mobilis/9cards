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
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GameOverMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.StartGameMessage;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class MucPacketProcessor
{
	/**	The 9Cards service instance. */
	private NineCardsService mServiceInstance;
	
	
	/**
	 * 
	 * @param connection
	 */
	public MucPacketProcessor(NineCardsService serviceInstance)
	{
		this.mServiceInstance = serviceInstance;
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void processPacket(Message message) throws Exception
	{
		XMPPInfo info = null;
		
		if(message.getBody().toLowerCase().contains("startgamemessage"))
			info = new StartGameMessage();
		if(message.getBody().toLowerCase().contains("playcardmessage"))
			info = new PlayCardMessage();
		
		if(info != null) {
			XmlPullParser xmlParser = XmlPullParserFactory.newInstance().newPullParser();
			xmlParser.setInput(new StringReader(message.getBody()));
			info.fromXML(xmlParser);
		}

		if(info instanceof StartGameMessage)
			onStartGame((StartGameMessage) info, message.getFrom());
		
		else if(info instanceof PlayCardMessage)
			onPlayCard((PlayCardMessage) info, message.getFrom());
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onStartGame(StartGameMessage message, String sender)
	{
		// only allowed in gamestate ready
		if(mServiceInstance.getGame().getGameState() == State.READY) {
			
			// check if player is allowed to start the game
			if(mServiceInstance.getMucConnection().isAdmin(sender)) {

				// close game for joining
				mServiceInstance.getMucConnection().lockMuc();
				
				// set game state and prepare first round
				mServiceInstance.getGame().setGameState(State.PLAYING);
				mServiceInstance.getGame().startNewRound();
				
				// inform all players about start of game
				mServiceInstance.getMucConnection().sendMessagetoMuc(
						new StartGameMessage(
								mServiceInstance.getSettings().getRounds(),
								mServiceInstance.getMucConnection().getMucPw()));
			}
		}
	}
	
	
	/**
	 * 
	 * @param message
	 */
	private void onPlayCard(PlayCardMessage message, String sender) throws Exception
	{
		Player player = mServiceInstance.getGame().getPlayer(sender);
		if(player != null) {
			
			// check if player already played a card this round
			if(player.getChosenCard() == -1) {
				
				// check if player already used this card
				if(!player.getUsedCards().contains(message.getCard())) {
					
					// add this card to the used ones and set player to alreadyChose
					player.getUsedCards().add(message.getCard());
					player.setChosenCard(message.getCard());
					
					// inform other players that this player chose some card
					mServiceInstance.getMucConnection().sendMessagetoMuc(
							new CardPlayedMessage(mServiceInstance.getGame().getRound(), player.getID()));
					
					// check if round is finished
					mServiceInstance.getMucConnection().checkRoundOver();
				}
			}
		}
	}
}
