package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.android.ninecards.communication.XMPPInfo;


public class RoundCompleteMessage implements XMPPInfo {

	private int round = Integer.MIN_VALUE;
	private String winner = null;
	private List< PlayerInfo > PlayerInfos = new ArrayList< PlayerInfo >();


	public RoundCompleteMessage( int round, String winner, List< PlayerInfo > PlayerInfos ) {
		super();
		this.round = round;
		this.winner = winner;
		for ( PlayerInfo entity : PlayerInfos ) {
			this.PlayerInfos.add( entity );
		}
	}

	public RoundCompleteMessage(){}



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
				else if (tagName.equals( "round" ) ) {
					this.round = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "winner" ) ) {
					this.winner = parser.nextText();
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

	public static final String CHILD_ELEMENT = "RoundCompleteMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:RoundCompleteMessage";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<round>" )
			.append( this.round )
			.append( "</round>" );

		sb.append( "<winner>" )
			.append( this.winner )
			.append( "</winner>" );

		for( PlayerInfo entry : this.PlayerInfos ) {
			sb.append( "<" + PlayerInfo.CHILD_ELEMENT + ">" );
			sb.append( entry.toXML() );
			sb.append( "</" + PlayerInfo.CHILD_ELEMENT + ">" );
		}

		return sb.toString();
	}



	public int getRound() {
		return this.round;
	}

	public void setRound( int round ) {
		this.round = round;
	}

	public String getWinner() {
		return this.winner;
	}

	public void setWinner( String winner ) {
		this.winner = winner;
	}

	public List< PlayerInfo > getPlayerInfos() {
		return this.PlayerInfos;
	}

	public void setPlayerInfos( List< PlayerInfo > PlayerInfos ) {
		this.PlayerInfos = PlayerInfos;
	}

}