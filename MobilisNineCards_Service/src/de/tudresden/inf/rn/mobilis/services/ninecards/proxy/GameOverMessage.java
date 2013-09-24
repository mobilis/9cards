package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class GameOverMessage implements XMPPInfo {

	private String winner = null;
	private int score = Integer.MIN_VALUE;
	private List< PlayerInfo > PlayerInfos = new ArrayList< PlayerInfo >();


	public GameOverMessage( String winner, int score, List< PlayerInfo > PlayerInfos ) {
		super();
		this.winner = winner;
		this.score = score;
		for ( PlayerInfo entity : PlayerInfos ) {
			this.PlayerInfos.add( entity );
		}
	}

	public GameOverMessage(){}



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
				else if (tagName.equals( "winner" ) ) {
					this.winner = parser.nextText();
				}
				else if (tagName.equals( "score" ) ) {
					this.score = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( PlayerInfo.CHILD_ELEMENT ) ) {
					PlayerInfo entity = new PlayerInfo();

					entity.fromXML( parser );
					this.PlayerInfos.add( entity );
					
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

	public static final String CHILD_ELEMENT = "GameOverMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:GameOverMessage";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<winner>" )
			.append( this.winner )
			.append( "</winner>" );

		sb.append( "<score>" )
			.append( this.score )
			.append( "</score>" );

		for( PlayerInfo entry : PlayerInfos ) {
			sb.append( "<" + PlayerInfo.CHILD_ELEMENT + ">" );
			sb.append( entry.toXML() );
			sb.append( "</" + PlayerInfo.CHILD_ELEMENT + ">" );
		}

		return sb.toString();
	}



	public String getWinner() {
		return this.winner;
	}

	public void setWinner( String winner ) {
		this.winner = winner;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore( int score ) {
		this.score = score;
	}

	public List< PlayerInfo > getPlayerInfos() {
		return this.PlayerInfos;
	}

	public void setPlayerInfos( List< PlayerInfo > PlayerInfos ) {
		this.PlayerInfos = PlayerInfos;
	}

}