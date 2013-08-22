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

import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.Card;


/**
 * The Class for representing a specific player.
 */
public class Player {
	
	/** The player's jid. */
	private String jid;
	
	/** The player's name. */
	private String name;

	/** The cards which were already used. */
	private List<Integer> usedCards;
	
	/** The number of rounds this player won during the game. */
	private int roundsWon;
	
	/** The card which was chosen for the current round; -1 if the player didn't chose yet. */
	private int chosenCard;
	
	
	/**
	 * Initializes a new 9Cards Player.
	 * 
	 * @param jid the jid of the player
	 * @param name the name of the player
	 */
	public Player(String jid, String name) {
		
		this.jid = jid;
		this.name = name;
		
		this.roundsWon = 0;
		this.chosenCard = -1;
		this.usedCards = new ArrayList<Integer>(9);
		
		// assure that the name is set
		if(this.name == null || this.name.length() == 0)
			this.name = jid.substring(0, jid.indexOf("@"));
	}
	
	
	/**
	 * Chosen card should be set to -1 at the beginning of each round,
	 * and to the ID of the chosen card after the player chose one.
	 * @param chosenCard
	 */
	public void setChosenCard(int chosenCard) {
		this.chosenCard = chosenCard;
	}
	
	
	/**
	 * Returns the ID of the card which was chosen for this round, or -1 if there isn't any yet.
	 * @return
	 */
	public int getChosenCard() {
		return chosenCard;
	}
	
	
	/**
	 * Increments the number of rounds won by this player by 1.
	 */
	public void incrementRoundsWon() {
		roundsWon++;
	}
	
	
	/**
	 * Returns the number of rounds of this game which this player has already won.
	 * @return the number of rounds the player won
	 */
	public int getRoundsWon() {
		return roundsWon;
	}
	
	
	public void setRoundsWon(int roundsWon) {
		this.roundsWon = roundsWon;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public List<Integer> getUsedCards() {
		return usedCards;
	}
	
	
	public void setUsedCards(List<Card> cards) {
		usedCards.clear();
		for(Card card : cards)
			if(card.getAlreadyUsed())
				usedCards.add(card.getValue());
	}
	
	
	public String getUsedCardsAsString(boolean ownPlayer) {
		String str = "";
		
		for(int i=usedCards.size() -1; i>=0; i--)
			str += usedCards.get(i) + ", ";
		
		if(chosenCard > -1)
			str = ownPlayer ? chosenCard + ", " + str : "?, " + str; 
		
		if(str.length() > 0)
			str = str.substring(0, str.lastIndexOf(","));
		
		else if(str.length() == 0)
			str = " - ";
		
		return str;
	}
	

	/**
	 * Gets the jid of the player.
	 * @return the jid of the player
	 */
	public String getJid() {
		return jid;
	}
	
	
	/**
	 * Returns the player's nickname.
	 * @return
	 */
	public String getName() {
		return name;
	}
}
