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
package de.tudresden.inf.rn.mobilis.services.ninecards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.Card;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayerInfo;

/**
 * The game class represents a whole game. 
 * It organizes players, cards and the state of the game.
 */

public class Game {
	
	/** The control. */
	private NineCardsService mServiceInstance;
	
	/** Whether players can already/still join or not. */
	private boolean gameOpen;
	/** The current round of the game. */
	private int round;
	/** The game players (JID, player) */
	private HashMap<String, Player> gamePlayers;
	/** The JID of the player who created the game. */
	private String creator;
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(Game.class.getCanonicalName());
	
	/** The state of the game. */
	private State state;
	/** The possible states of the game. */
	public static enum State {
		UNINITIALIZED, LOBBY, PLAY
	}
	

	
	/**
	 * Initializes the Game Component.
	 * @param serviceInstance NineCardsController, who administrates the whole life cycle
	 */
	public Game(NineCardsService serviceInstance) throws Exception {
		this.mServiceInstance = serviceInstance;

		this.gameOpen = false;
		this.round = 0;
		this.gamePlayers = new HashMap<String, Player>();
        
		this.state = State.UNINITIALIZED;
	}
	
	
	/**
	 * Called when starting a new round.
	 */
	public void startNewRound() {
		LOGGER.info("Starting round " + round+1 + "!");
		round++;
		
		for(Player player : gamePlayers.values())
			player.setChosenCard(-1);
	}
	
	
	/**
	 * Checks if all players chose a card.
	 * @return true if all players chose a card.
	 */
	public boolean checkRoundOver() {
		boolean over = true;
		for(Player player : gamePlayers.values())
			over = over && (player.getChosenCard() != -1);
		
		return over;
	}
	
	
	/**
	 * Returns the player that played the highest card in this round.
	 * @return
	 */
	public Player getRoundWinner() {
		Player winner = null;
		for(Player plr : gamePlayers.values()) {
			if((winner == null) || (plr.getChosenCard() > winner.getChosenCard())) {
				winner = plr;
			}
		}
		
		return winner;
	}
	
	
	/**
	 * Returns a list containing a PlayerInfo Object for each player.
	 * @return
	 */
	public List<PlayerInfo> getPlayerInfos() {
		List<PlayerInfo> infoList = new ArrayList<PlayerInfo>();
		
		// create PlayerInfo for each player
		for(Player plr : gamePlayers.values()) {
			// convert the list of used cards
			List<Card> usedCards = new ArrayList<Card>();
			for(Integer cardId : plr.getUsedCards())
				usedCards.add(new Card(cardId, true));
			
			// put information into new PlayerInfo
			PlayerInfo info = new PlayerInfo();
			info.setPlayersName(plr.getName());
			info.setPlayersJID(plr.getJid());
			info.setPlayersWins(plr.getRoundsWon());
			info.setPlayersUsedCards(usedCards);
			
			// add PlayerInfo to list
			infoList.add(info);
		}
		
		return infoList;
	}

	
	/**
	 * Adds a new player.
	 * @param player the player to be added
	 */
	public void addPlayer(Player player){
		this.gamePlayers.put(player.getJid(), player);
		LOGGER.info("Added player " + player.getJid());
	}
	
	
	/**
	 * Returns the player corresponding to the JabberID that is used.
	 * @param jid The JabberID of the player.
	 * @return Player the player that matches the JabberID.
	 */
	public Player getPlayer(String jid) {
		return gamePlayers.get(jid);
	}


	/**
	 * For getting a list containing players.
	 * @return The list of players.
	 */
	public HashMap<String, Player> getPlayers() {
		return gamePlayers;
	}
	
	/**
	 * Removes a player by using his jid. Also removes him from the chat.
	 * @param jid the jid of the player to be kicked
	 */
	public void removePlayerByJid(String jid){
		gamePlayers.remove(jid);
		mServiceInstance.getMucConnection().removePlayerFromChat(jid);
		LOGGER.info("Removed player " + jid);
		
		// shut down if there are no more players left
		if(gamePlayers.values().size() == 0) {
			LOGGER.info("no players left, shutting down service");
			mServiceInstance.shutdown();
		}
	}
	
	/**
	 * Sets the current game state to a new one.
	 * @param newState the next state for the state machine.
	 */
	public void setGameState(State newState) {
		state = newState;
		LOGGER.info("Game State set to " + newState);
	}
	
	/**
	 * Gets the game state.
	 * @return the current game state
	 */
	public State getGameState() {
		return state;
	}

	/**
	 * Gets the current round.
	 * @return the round
	 */
	public int getRound() {
		return round;
	}
	
	/**
	 * Sets the game to open or closed.
	 * @param gameOpen true if the game is open
	 */
	public void setGameOpen(boolean gameOpen) {
		this.gameOpen = gameOpen;
		LOGGER.info("Game set to open=" + gameOpen);
	}
	
	/**
	 * Checks if game is open.
	 * @return true, if game is open
	 */
	public boolean isGameOpen() {
		return gameOpen;
	}
	
	/**
	 * Sets the game creator.
	 * @param creator the JID of the creator 
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	/**
	 * Returns the player which created the game.
	 * @return the JID of the game creator
	 */
	public String getCreator() {
		return creator;
	}
}
