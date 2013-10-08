/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science Computer
 * Networks Group: http://www.rn.inf.tu-dresden.de mobilis project:
 * https://github.com/mobilis
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.android.ninecards.game;

import java.util.HashMap;

/**
 * Class which represents a game instance. It stores name, number of rounds, current round and a list of players.
 * 
 * @author Matthias Köngeter
 *
 */
public class Game
{

	/** The game's name. */
	private String name;
	/** The current round of the game. */
	private int currentRound;
	/** The total number of rounds. */
	private int rounds;
	
	/** The game players */
	private HashMap<String, Player> gamePlayers;
	

	/**
	 * Constructor for creating a new game instance.
	 * 
	 * @param name the name of the game instance
	 */
	public Game(String name)
	{
		this.name = name;
		this.currentRound = 0;
		this.gamePlayers = new HashMap<String, Player>();
	}
	
	
	/**
	 * Returns a map of players with the players IDs as keys (example: room@conference.jabber.org/nick)
	 * and the corresponding player objects as values.
	 * 
	 * @return the game players
	 */
	public HashMap<String, Player> getPlayers()
	{
		return gamePlayers;
	}
	
	
	/**
	 * Changes the id of the player. Needed when he changes his nickname in the MUC room.
	 * 
	 * @param oldID the old player ID (example: room@conference.jabber.org/oldNick)
	 * @param newID the new player ID (example: room@conference.jabber.org/newNick)
	 */
	public void changePlayerID(String oldID, String newID)
	{
		Player player = gamePlayers.remove(oldID);
		player.changeID(newID);
		gamePlayers.put(newID, player);
	}
	
	
	/**
	 * Returns the name of the game instance.
	 * 
	 * @return the name of the game
	 */
	public String getName()
	{
		return name != null ? name : "name not set";
	}
	
	
	/**
	 * Sets the round value.
	 * 
	 * @param round the new round value
	 */
	public void setCurrentRound(int round)
	{
		this.currentRound = round;
	}
	

	/**
	 * Returns the current round value.
	 * 
	 * @return the current round value
	 */
	public int getCurrentRound()
	{
		return currentRound;
	}
	
	
	/**
	 * Sets the total number of rounds which are to be played.
	 * 
	 * @param rounds the total rounds value
	 */
	public void setRounds(int rounds)
	{
		this.rounds = rounds;
	}
	
	
	/**
	 * Returns the total number of rounds before this game is finished.
	 * 
	 * @return the total number of rounds
	 */
	public int getRounds()
	{
		return rounds;
	}
}
