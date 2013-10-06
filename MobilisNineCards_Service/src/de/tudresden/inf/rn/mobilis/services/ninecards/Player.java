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
 * The Class for representing a specific player.
 */
public class Player
{
	/** The player's id in the muc room (example: room@conference.jabber.org/nick) */
	private String fullID;

	/** The cards (values from 1-9) which the player already used */
	private List<Integer> usedCards;
	/** The number of rounds this player won during the game. */
	private int roundsWon;
	/** The card which was chosen for the current round; -1 if the player didn't chose yet. */
	private int chosenCard;
	
	
	/**
	 * Initializes a new 9Cards Player.
	 * 
	 * @param fullID the player's id in the muc room (example: room@conference.jabber.org/nick)
	 * @param isCreator true if this player is the owner of the game
	 */
	public Player(String fullID)
	{
		this.fullID = fullID;
		this.roundsWon = 0;
		this.chosenCard = -1;
		
		this.usedCards = new ArrayList<Integer>(9);
	}
	
	
	/**
	 * Chosen card should be set to -1 at the beginning of each round,
	 * and to the ID of the chosen card after the player chose one.
	 * @param chosenCard
	 */
	public void setChosenCard(int chosenCard)
	{
		this.chosenCard = chosenCard;
	}
	
	
	/**
	 * Returns the ID of the card which was chosen for this round, or -1 if there isn't any yet.
	 * @return
	 */
	public int getChosenCard()
	{
		return chosenCard;
	}
	
	
	/**
	 * Increments the number of rounds won by this player by 1.
	 */
	public void incrementRoundsWon()
	{
		roundsWon++;
	}
	
	
	/**
	 * Returns the number of rounds of this game which this player has already won.
	 * @return the number of rounds the player won
	 */
	public int getRoundsWon()
	{
		return roundsWon;
	}
	
	
	/**
	 * Returns a list containing the cards the player already used.
	 * @return a list containing the card values which were already used
	 */
	public List<Integer> getUsedCards()
	{
		return usedCards;
	}
	

	/**
	 * Gets the id of the player in the muc room (example: room@conference.jabber.org/nick)
	 * @return the id of the player
	 */
	public String getID()
	{
		return fullID;
	}
	
	
	/**
	 * Changes the id of the player in the muc room. Needed when he changes his nickname.
	 * @param the new full id (example: room@conference.jabber.org/nick)
	 */
	public void changeID(String newID)
	{
		this.fullID = newID;
	}
}
