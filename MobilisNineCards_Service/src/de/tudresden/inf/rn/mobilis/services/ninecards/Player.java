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
import java.util.List;

/**
 * The player class is used to store information about a specific player. 
 * 
 * @author Matthias Köngeter
 *
 */
public class Player
{
	
	/**
	 * The player's jid in the muc room (example:
	 * room@conference.jabber.org/nick).
	 */
	private String mucJID;

	/** The player's jid (example: nick@jabber.org/resource). */
	private String privateJID;

	/** The cards (values from 1-9) which the player already used in the current game. */
	private List<Integer> usedCards;
	/** The number of rounds this player won in the current game. */
	private int score;
	/** The card which was chosen for the current round; -1 if the player didn't choose one yet. */
	private int chosenCard;
	
	
	/**
	 * The constructor for initializing a new player object.
	 * 
	 * @param mucJID
	 *            the player's id in the muc room (example:
	 *            room@conference.jabber.org/nick)
	 */
	public Player(String mucJID, String privateJID)
	{
		this.mucJID = mucJID;
		this.privateJID = privateJID;
		this.score = 0;
		this.chosenCard = -1;
		
		this.usedCards = new ArrayList<Integer>(9);
	}
	
	
	/**
	 * The chosen card value should be set to -1 at the beginning of each round,
	 * and to the value of the chosen card after the player chose one.
	 * 
	 * @param chosenCard the value of the chosen card
	 */
	public void setChosenCard(int chosenCard)
	{
		this.chosenCard = chosenCard;
	}
	
	
	/**
	 * Returns the value of the card which was chosen for this round, or -1 if there isn't any yet.
	 * 
	 * @return
	 */
	public int getChosenCard()
	{
		return chosenCard;
	}
	
	
	/**
	 * Increments the number of rounds won by this player by 1.
	 */
	public void incrementScore()
	{
		score++;
	}
	
	
	/**
	 * Returns the number of rounds of this game which this player has already won.
	 * 
	 * @return the number of rounds the player won
	 */
	public int getScore()
	{
		return score;
	}
	
	
	/**
	 * Returns a list containing the card values the player already used in the current game.
	 * 
	 * @return a list containing the card values which were already used
	 */
	public List<Integer> getUsedCards()
	{
		return usedCards;
	}
	

	/**
	 * Returns the JID of the player in the multiuser chat room (example:
	 * room@conference.jabber.org/nick).
	 * 
	 * @return the muc JID of the player
	 */
	public String getMucJID()
	{
		return mucJID;
	}
	
	
	/**
	 * Changes the ID of the player in the multiuser chat room. Needed when he changes his nickname.
	 * 
	 * @param the new full ID (example: room@conference.jabber.org/nick)
	 */
	public void changeID(String newID)
	{
		this.mucJID = newID;
	}

	/**
	 * Returns the private JID of the player (example:
	 * nick@jabber.org/resource).
	 * 
	 * @return the private JID of the player
	 */
	public String getPrivateJID() {
		return privateJID;
	}
}
