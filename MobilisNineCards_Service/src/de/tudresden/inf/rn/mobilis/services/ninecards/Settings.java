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
 * The Class Settings contains global setting for the game.
 */
public class Settings {
	
	/** The game name. */
	private String gameName;
	/** The amount of max players. */
	private int maxPlayers;
	/** The amount of min players. */
	private int minPlayers;
	/** The amount of rounds for this game. */
	private int rounds;
	
	/** The chat id. */
	private String chatID;
	/** The chat password. */
	private String chatPW;
	/** The service resource. */
	private String serviceResource;
	
	
	/**
	 * Initializes the Settings with predefined values.
	 *
	 * @param agent the MobilisAgent which contains XMPP specific attributes.
	 */
	public Settings(MobilisAgent agent){
		serviceResource = agent.getResource();
		initDefaultValues(agent.getConnection().getServiceName());
	}
	
	
	/**
	 * Inits the default values.
	 * @param serverIdent the server identity
	 */
	private void initDefaultValues(String serverIdent){
		gameName = serverIdent;

		rounds = 9;
		maxPlayers = 9;
		minPlayers = 1;
		
		// default chatroom data, replaces all '/' and ':' because they would produce an error in chat name
		chatID = serviceResource.replaceAll( "[/:]", "" ) + "@conference." + serverIdent;
		chatPW = "tnuhx";
	}
	
	/**
	 * Gets the game name.
	 * @return the game name
	 */
	public String getGameName() {
		return gameName;
	}
	
	/**
	 * Sets the game name.
	 * @param gameName the new game name
	 */
	public void setGameName(String gameName) {
		if((gameName != null) && (gameName.length() > 0))
			this.gameName = gameName;
	}

	/**
	 * Sets the max players.
	 *
	 * @param maxPlayer the new max players
	 */
	public void setMaxPlayers(int maxPlayer) {
		if((maxPlayer >= minPlayers) && (maxPlayer <= 9))
			this.maxPlayers = maxPlayer;
	}

	/**
	 * Gets the max players.
	 *
	 * @return the max players
	 */
	public int getMaxPlayers() {
		return maxPlayers;
	}

	/**
	 * Sets the min players.
	 *
	 * @param minPlayer the new min players
	 */
	public void setMinPlayers(int minPlayer) {
		if((minPlayer > 0) && (minPlayer <= 9))
			this.minPlayers = minPlayer;
	}

	/**
	 * Gets the min players.
	 *
	 * @return the min players
	 */
	public int getMinPlayers() {
		return minPlayers;
	}
	
	/**
	 * Sets the chat id.
	 * @param chatID the new chat id
	 */
	public void setChatID(String chatID) {
		this.chatID = chatID;
	}
	
	/**
	 * Gets the chat id.
	 * @return the chat id
	 */
	public String getChatID() {
		return chatID;
	}
	
	/**
	 * Sets the chat password.
	 * @param chatPW the new chat password
	 */
	public void setChatPW(String chatPW) {
		this.chatPW = chatPW;
	}

	/**
	 * Gets the chat password.
	 * @return the chat password
	 */
	public String getChatPW() {
		return chatPW;
	}

	/**
	 * Sets the rounds.
	 * @param rounds the new rounds
	 */
	public void setRounds(int rounds) {
		if((rounds > 0) && (rounds <= 9))
				this.rounds = rounds;
	}


	/**
	 * Gets the rounds.
	 *
	 * @return the rounds
	 */
	public int getRounds() {
		return rounds;
	}
	
	/**
	 * Gets the service resource.
	 * @return the service resource
	 */
	public String getServiceResource() {
		return serviceResource;
	}
}
