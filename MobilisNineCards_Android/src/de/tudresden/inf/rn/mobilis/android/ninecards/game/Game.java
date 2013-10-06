package de.tudresden.inf.rn.mobilis.android.ninecards.game;

import java.util.HashMap;

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
public class Game
{

	/** The game's name. */
	private String name;
	
	/** The current round of the game. */
	private int round;
	
	/** The total number of rounds. */
	private int maxRounds;
	
	/** The game players (Nickname : Player) */
	private HashMap<String, Player> gamePlayers;
	

	/**
	 * 
	 */
	public Game(String name)
	{
		this.name = name;

		round = 0;
		gamePlayers = new HashMap<String, Player>();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, Player> getPlayers()
	{
		return gamePlayers;
	}	
	
	/**
	 * Changes the id of the player in the muc room. Needed when he changes his nickname.
	 * @param the new full id (example: room@conference.jabber.org/nick)
	 */
	public void changePlayerID(String oldID, String newID)
	{
		Player player = gamePlayers.remove(oldID);
		player.changePlayerID(newID);
		gamePlayers.put(newID, player);
	}
	
	/**
	 * 
	 * @return
	 */
	public String getName()
	{
		return name != null ? name : "name not set";
	}
	
	public void setRound(int round)
	{
		this.round = round;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getRound()
	{
		return round;
	}
	
	/**
	 * 
	 * @param maxRounds
	 */
	public void setMaxRounds(int maxRounds)
	{
		this.maxRounds = maxRounds;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getMaxRounds()
	{
		return maxRounds;
	}
}
