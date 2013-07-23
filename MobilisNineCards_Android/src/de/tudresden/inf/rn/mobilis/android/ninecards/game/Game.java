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
public class Game {

	/** The game's name. */
	private String name;
	
	/** The current round of the game. */
	private int round;
	
	/** The game players (JID, player) */
	private HashMap<String, Player> gamePlayers;
	

	/**
	 * 
	 */
	public Game() {
		this.gamePlayers = new HashMap<String, Player>();
	}
	
	
	/**
	 * Returns the player corresponding to the JabberID that is used.
	 * @param jid The JabberID of the player.
	 * @return Player the player that matches the JabberID.
	 */
	public Player getPlayer(String jid) {
		return gamePlayers.get(jid);
	}
	
	public String getName() {
		return name != null ? name : "name not set";
	}
	
	
	public void setName(String name) {
		this.name = name;
	}
}
