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
import java.util.Random;
import java.util.logging.Logger;

import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.PlayerInfo;

/**
 * The game class is used to store information about the current game.
 * 
 * @author Matthias Köngeter
 *
 */
public class Game
{
	
	/** The Ninecards/Mobilis Service instance. */
	private NineCardsService mServiceInstance;
	
	/** The current round of the game. */
	private int round;
	/** The game players with IDs in MUC room as keys and player objects as values. */
	private HashMap<String, Player> gamePlayers;
	/** The winner of a round, needed for repeated calls of "getRoundWinner()". */
	private Player winnerOfRound;
	/** The winner of the game, needed for repeated calls of "getGameWinner()". */
	private Player winnerOfGame;
	
	/** The current state of the game. */
	private State state;
	/** The possible states of the game. */
	public static enum State {
		UNINITIALIZED, READY, PLAYING
	}

	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(Game.class.getCanonicalName());
	

	
	/**
	 * The Constructor for initializing a new Game object.
	 * 
	 * @param serviceInstance the instance of the mobilis ninecards service
	 */
	public Game(NineCardsService serviceInstance)
	{
		this.mServiceInstance = serviceInstance;

		this.round = 0;
		this.gamePlayers = new HashMap<String, Player>();
		this.winnerOfRound = null;
		this.winnerOfGame = null;
        
		this.state = State.UNINITIALIZED;
	}
	
	
	/**
	 * Called when starting a new round.
	 */
	public void startNewRound()
	{
		LOGGER.info("Starting round " + round+1 + "!");
		
		for(Player player : gamePlayers.values())
			player.setChosenCard(-1);
		
		winnerOfRound = null;
		round++;
	}
	
	
	/**
	 * Checks if all players chose a card.
	 * 
	 * @return true if all players chose a card, false if not.
	 */
	public boolean checkRoundOver()
	{
		boolean over = true;
		for(Player player : gamePlayers.values())
			over = over && (player.getChosenCard() != -1);
		
		return over;
	}
	
	
	/**
	 * Returns the player that played the highest card in this round
	 * or chooses one by random if more than one player played the same
	 * highest card.
	 * 
	 * @return the player who won the round
	 */
	public Player getRoundWinner()
	{
		if(winnerOfRound == null) {
			
			List<Player> potentialWinners = new ArrayList<Player>();
			for (Player plr : gamePlayers.values()) {
				
				// if list of potential winners is empty, add current player
				if (potentialWinners.size() == 0)
					potentialWinners.add(plr);

				// else if current player played same card as potential winners, add him to them
				else if (plr.getChosenCard() == potentialWinners.get(0).getChosenCard())
					potentialWinners.add(plr);

				// else if current player played higher card than potential winners, remove them and add him
				else if (plr.getChosenCard() > potentialWinners.get(0).getChosenCard()) {
					potentialWinners.clear();
					potentialWinners.add(plr);
				}
			}

			// return one of the potential winners by random
			winnerOfRound = potentialWinners.get(new Random().nextInt(potentialWinners.size()));
		}
		
		return winnerOfRound;
	}
	
	
	/**
	 * Returns the player that has reached the highest score.
	 * If there are more than one players with the same highest score,
	 * one is chosen by random. If the end of the game is not reached yet,
	 * null will be returned.
	 * 
	 * @return the player who won the game
	 */
	public Player getGameWinner()
	{
		if(round < mServiceInstance.getSettings().getRounds())
			return null;
		
		if(winnerOfGame == null) {

			List<Player> potentialWinners = new ArrayList<Player>();
			for (Player plr : gamePlayers.values()) {
				
				// if list of potential winners is empty, add current player
				if (potentialWinners.size() == 0)
					potentialWinners.add(plr);
				
				// else if current player reached same score as potential winners, add him to them
				else if (plr.getScore() == potentialWinners.get(0).getScore())
					potentialWinners.add(plr);
				
				// else if current player has reached higher score than potential winners, remove them and add him
				else if (plr.getScore() > potentialWinners.get(0).getScore()) {
					potentialWinners.clear();
					potentialWinners.add(plr);
				}
			}
			
			// return one of the potential winners by random
			winnerOfGame = potentialWinners.get(new Random().nextInt(potentialWinners.size()));
		}
		
		return winnerOfGame;
	}
	
	
	/**
	 * Returns a list containing a PlayerInfo Object for each player.
	 * 
	 * @return
	 */
	public List<PlayerInfo> getPlayerInfos()
	{
		List<PlayerInfo> infoList = new ArrayList<PlayerInfo>();
		
		// create PlayerInfo for each player
		for(Player plr : gamePlayers.values()) {
			
			// put information into new PlayerInfo
			PlayerInfo info = new PlayerInfo();
			info.setId(plr.getID());
			info.setScore(plr.getScore());
			info.setUsedcards(plr.getUsedCards());
			
			// add PlayerInfo to list
			infoList.add(info);
		}
		
		return infoList;
	}

	
	/**
	 * Adds a new player to the players list.
	 * 
	 * @param player the player to be added
	 */
	public void addPlayer(Player player)
	{
		this.gamePlayers.put(player.getID(), player);
		LOGGER.info("Added player " + player.getID());
	}
	
	
	/**
	 * Returns the player corresponding to the specified ID.
	 * 
	 * @param id the ID of the player (example: room@conference.jabber.org/nick)
	 * @return Player the player that matches the JabberID
	 */
	public Player getPlayer(String id)
	{
		return gamePlayers.get(id);
	}


	/**
	 * Returns a map containing all players of the game.
	 * The player's ID is used as key, while the player object is the value.
	 * 
	 * @return the list of players
	 */
	public HashMap<String, Player> getPlayers()
	{
		return gamePlayers;
	}
	
	
	/**
	 * Removes the player corresponding to the specified ID. Also removes him from the chat.
	 * If there's no player left in the game, the service will shut down.
	 * 
	 * @param id the ID of the player to be kicked (example: room@conference.jabber.org/nick)
	 * @param reason the reason to be logged or displayed when removing him from the chat
	 * @return the player object which was removed or null, if no player was found.
	 */
	public Player removePlayer(String id, String reason)
	{
		Player player = gamePlayers.remove(id);
		LOGGER.info("Removed player " + id);
		
		// shut down if there are no more players left
		if(gamePlayers.values().size() == 0) {
			LOGGER.info("no players left, shutting down service");
			mServiceInstance.shutdown();
		}
		
		return player;
	}
	
	
	/**
	 * Sets the current game state to the specified one.
	 * 
	 * @param newState the new game state.
	 */
	public void setGameState(State newState)
	{
		state = newState;
		LOGGER.info("Game State set to " + newState);
	}
	
	
	/**
	 * Returns the current game state.
	 * 
	 * @return the current game state
	 */
	public State getGameState()
	{
		return state;
	}

	
	/**
	 * Returns the current round.
	 * 
	 * @return the current round
	 */
	public int getRound()
	{
		return round;
	}
}
