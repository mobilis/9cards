package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayerInfo implements XMPPInfo {

	private String PlayersName = null;
	private String PlayersJID = null;
	private int PlayersWins = Integer.MIN_VALUE;
	private List< Card > PlayersUsedCards = new ArrayList< Card >();


	public PlayerInfo( String PlayersName, String PlayersJID, int PlayersWins, List< Card > PlayersUsedCards ) {
		super();
		this.PlayersName = PlayersName;
		this.PlayersJID = PlayersJID;
		this.PlayersWins = PlayersWins;
		for ( Card entity : PlayersUsedCards ) {
			this.PlayersUsedCards.add( entity );
		}
	}

	public PlayerInfo(){}



	@Override
	public void fromXML( XmlPullParser parser ) throws Exception {
		boolean done = false;
			
		do {
			switch (parser.getEventType()) {
			case XmlPullParser.START_TAG:
				String tagName = parser.getName();
				
				if (tagName.equals(getChildElement())) {
					parser.next();
				}
				else if (tagName.equals( "PlayersName" ) ) {
					this.PlayersName = parser.nextText();
				}
				else if (tagName.equals( "PlayersJID" ) ) {
					this.PlayersJID = parser.nextText();
				}
				else if (tagName.equals( "PlayersWins" ) ) {
					this.PlayersWins = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( Card.CHILD_ELEMENT ) ) {
					Card entity = new Card();

					entity.fromXML( parser );
					this.PlayersUsedCards.add( entity );
					
					parser.next();
				}
				else
					parser.next();
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals(getChildElement()))
					done = true;
				else
					parser.next();
				break;
			case XmlPullParser.END_DOCUMENT:
				done = true;
				break;
			default:
				parser.next();
			}
		} while (!done);
	}

	public static final String CHILD_ELEMENT = "PlayerInfo";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:PlayerInfo";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<PlayersName>" )
			.append( this.PlayersName )
			.append( "</PlayersName>" );

		sb.append( "<PlayersJID>" )
			.append( this.PlayersJID )
			.append( "</PlayersJID>" );

		sb.append( "<PlayersWins>" )
			.append( this.PlayersWins )
			.append( "</PlayersWins>" );

		for( Card entry : PlayersUsedCards ) {
			sb.append( "<" + Card.CHILD_ELEMENT + ">" );
			sb.append( entry.toXML() );
			sb.append( "</" + Card.CHILD_ELEMENT + ">" );
		}

		return sb.toString();
	}



	public String getPlayersName() {
		return this.PlayersName;
	}

	public void setPlayersName( String PlayersName ) {
		this.PlayersName = PlayersName;
	}

	public String getPlayersJID() {
		return this.PlayersJID;
	}

	public void setPlayersJID( String PlayersJID ) {
		this.PlayersJID = PlayersJID;
	}

	public int getPlayersWins() {
		return this.PlayersWins;
	}

	public void setPlayersWins( int PlayersWins ) {
		this.PlayersWins = PlayersWins;
	}

	public List< Card > getPlayersUsedCards() {
		return this.PlayersUsedCards;
	}

	public void setPlayersUsedCards( List< Card > PlayersUsedCards ) {
		this.PlayersUsedCards = PlayersUsedCards;
	}

}