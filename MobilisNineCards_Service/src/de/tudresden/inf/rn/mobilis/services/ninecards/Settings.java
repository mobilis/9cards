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

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;

/**
 * This class contains global settings for the game.
 * 
 * @author Matthias Köngeter
 *
 */
public class Settings
{
	/** The game name. */
	private String gameName;
	/** The maximum amount of players. */
	private int maxPlayers;
	/** The amount of rounds for this game. */
	private int rounds;
	
	/** The chat ID. */
	private String chatID;
	
	
	/**
	 * Initializes the settings with predefined values.
	 * 
	 * @param agent the MobilisAgent which contains XMPP specific attributes.
	 */
	public Settings(MobilisAgent agent)
	{
		gameName = agent.getConnection().getServiceName();
		rounds = 9;
		maxPlayers = 9;
		
		// replace all '/' and ':' because they would produce an error in chat name
		chatID = (
				agent.getResource().replaceAll( "[/:]", "" )
				+ "@conference."
				+ agent.getConnection().getServiceName())
				.toLowerCase();
	}
	
	
	/**
	 * Returns the game name.
	 * 
	 * @return the name of the game
	 */
	public String getGameName()
	{
		return gameName;
	}
	
	
	/**
	 * Sets the game name.
	 * 
	 * @param gameName the new game name
	 */
	public void setGameName(String gameName)
	{
		if((gameName != null) && (gameName.length() > 0))
			this.gameName = gameName;
	}

	
	/**
	 * Sets the maximum number of players.
	 * 
	 * @param maxPlayer the new maximum number of players
	 */
	public void setMaxPlayers(int maxPlayers)
	{
		if(maxPlayers > 0 && maxPlayers <= 10)
			this.maxPlayers = maxPlayers;
	}

	
	/**
	 * Returns the maximum number of players.
	 * 
	 * @return the maximum number of players
	 */
	public int getMaxPlayers()
	{
		return maxPlayers;
	}

	
	/**
	 * Returns the chat ID.
	 * 
	 * @return the chat ID
	 */
	public String getChatID()
	{
		return chatID;
	}
	

	/**
	 * Sets the number of rounds to be played.
	 * 
	 * @param rounds the new number of rounds
	 */
	public void setRounds(int rounds)
	{
		if((rounds > 0) && (rounds <= 9))
				this.rounds = rounds;
	}


	/**
	 * Returns the number of rounds to be played.
	 *
	 * @return the number of rounds rounds
	 */
	public int getRounds()
	{
		return rounds;
	}
}
