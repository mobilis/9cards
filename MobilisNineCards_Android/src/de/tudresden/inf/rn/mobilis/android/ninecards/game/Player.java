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
package de.tudresden.inf.rn.mobilis.android.ninecards.game;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class for representing a specific player. It contains information about the player's ID, his score,
 * the cards which he already used and the card he chose for the current round.
 * 
 * @author Matthias Köngeter
 *
 */
public class Player
{
	
	/** The player's id in the MUC room (example: room@conference.jabber.org/nick). */
	private String id;

	/** The cards which were already used. */
	private List<Integer> usedCards;
	/** The number of rounds this player already won in the current game. */
	private int score;
	/** The card which was chosen for the current round. */
	private int chosenCard;
	
	
	/**
	 * Initializes a new 9Cards Player.
	 * 
	 * @param id the ID of the player in the MUC room (example: room@conference.jabber.org/nick)
	 */
	public Player(String id)
	{
		this.id = id;
		this.score = 0;
		this.chosenCard = -1;
		this.usedCards = new ArrayList<Integer>(9);
	}
	
	
	/**
	 * Sets the value for the card which was chosen for the current round. This value
	 * should be set to '-1' at the beginning of each round, and to the ID of the chosen
	 * card after the player chose one or to '0' if he chose one but the value remains unknown.
	 * 
	 * @param chosenCard the value of the chosen card
	 */
	public void setChosenCard(int chosenCard)
	{
		this.chosenCard = chosenCard;
	}
	
	
	/**
	 * Returns the ID of the card which was chosen for this round, or '-1' if there isn't any yet.
	 * If '0' is returned, the player chose a card but the value is kept secret by the ninecards service.
	 * 
	 * @return the value of the chosen card
	 */
	public int getChosenCard()
	{
		return chosenCard;
	}
	
	
	/**
	 * Returns the number of rounds which the player has already won in the current game.
	 * 
	 * @return the number of rounds the player has won
	 */
	public int getScore()
	{
		return score;
	}
	
	
	/**
	 * Sets the number of rounds which the player has already won in the current game.
	 * 
	 * @param score the number of rounds the player has won
	 */
	public void setScore(int score)
	{
		this.score = score;
	}
	
	
	/**
	 * Returns a list containing the values of the cards which the player already used in the current game.
	 * 
	 * @return the used cards
	 */
	public List<Integer> getUsedCards()
	{
		return usedCards;
	}
	
	
	/**
	 * Sets the list containing the values of the cards which the player already used in the current game.
	 * 
	 * @param usedCards the used cards
	 */
	public void setUsedCards(List<Integer> usedCards)
	{
		this.usedCards = usedCards;
	}
	
	
	/**
	 * Returns the ID of the player in the MUC room (example: room@conference.jabber.org/nick)
	 * 
	 * @return the ID of the player
	 */
	public String getID()
	{
		return id;
	}
	
	
	/**
	 * Changes the ID of the player in the MUC room. Needed when he changes his nickname.
	 * Should only be used by Game.changePlayerID(String oldID, String newID).
	 * 
	 * @param the new full ID (example: room@conference.jabber.org/nick)
	 */
	public void changeID(String newID)
	{
		this.id = newID;
	}
	
	
	/**
	 * Returns the cards which were already used by this player in the current game as a formatted string.
	 * If the player didn't use a card yet, '-' is returned, otherwise the first played card values appear
	 * at the right end of the string and the last played card values at the left end of the string. If a
	 * card was played whose value is kept secret, a '?' will be appended.
	 * 
	 * @return the used cards as a formatted string
	 */
	public String getUsedCardsAsString()
	{
		String str = "";
		
		for(int i=usedCards.size() -1; i>=0; i--)
			str += usedCards.get(i) + ", ";
		
		if(chosenCard > -1)
			str = chosenCard > 0 ? chosenCard + ", " + str : "?, " + str; 
		
		if(str.length() > 0)
			str = str.substring(0, str.lastIndexOf(","));
		
		else if(str.length() == 0)
			str = " - ";
		
		return str;
	}
}
