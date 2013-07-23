package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class RoundCompleteMessage implements XMPPInfo {

	private int RoundID = Integer.MIN_VALUE;
	private String RoundWinnersName = null;
	private String RoundWinnersJID = null;
	private List< PlayerInfo > PlayerInfos = new ArrayList< PlayerInfo >();
	private boolean EndOfGame = false;


	public RoundCompleteMessage( int RoundID, String RoundWinnersName, String RoundWinnersJID, List< PlayerInfo > PlayerInfos, boolean EndOfGame ) {
		super();
		this.RoundID = RoundID;
		this.RoundWinnersName = RoundWinnersName;
		this.RoundWinnersJID = RoundWinnersJID;
		for ( PlayerInfo entity : PlayerInfos ) {
			this.PlayerInfos.add( entity );
		}
		this.EndOfGame = EndOfGame;
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
				else if (tagName.equals( "RoundID" ) ) {
					this.RoundID = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "RoundWinnersName" ) ) {
					this.RoundWinnersName = parser.nextText();
				}
				else if (tagName.equals( "RoundWinnersJID" ) ) {
					this.RoundWinnersJID = parser.nextText();
				}
				else if (tagName.equals( PlayerInfo.CHILD_ELEMENT ) ) {
					PlayerInfo entity = new PlayerInfo();

					entity.fromXML( parser );
					this.PlayerInfos.add( entity );
					
					parser.next();
				}
				else if (tagName.equals( "EndOfGame" ) ) {
					this.EndOfGame = Boolean.parseBoolean( parser.nextText() );
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

		sb.append( "<RoundID>" )
			.append( this.RoundID )
			.append( "</RoundID>" );

		sb.append( "<RoundWinnersName>" )
			.append( this.RoundWinnersName )
			.append( "</RoundWinnersName>" );

		sb.append( "<RoundWinnersJID>" )
			.append( this.RoundWinnersJID )
			.append( "</RoundWinnersJID>" );

		for( PlayerInfo entry : this.PlayerInfos ) {
			sb.append( "<" + PlayerInfo.CHILD_ELEMENT + ">" );
			sb.append( entry.toXML() );
			sb.append( "</" + PlayerInfo.CHILD_ELEMENT + ">" );
		}

		sb.append( "<EndOfGame>" )
			.append( this.EndOfGame )
			.append( "</EndOfGame>" );

		return sb.toString();
	}



	public int getRoundID() {
		return this.RoundID;
	}

	public void setRoundID( int RoundID ) {
		this.RoundID = RoundID;
	}

	public String getRoundWinnersName() {
		return this.RoundWinnersName;
	}

	public void setRoundWinnersName( String RoundWinnersName ) {
		this.RoundWinnersName = RoundWinnersName;
	}

	public String getRoundWinnersJID() {
		return this.RoundWinnersJID;
	}

	public void setRoundWinnersJID( String RoundWinnersJID ) {
		this.RoundWinnersJID = RoundWinnersJID;
	}

	public List< PlayerInfo > getPlayerInfos() {
		return this.PlayerInfos;
	}

	public void setPlayerInfos( List< PlayerInfo > PlayerInfos ) {
		this.PlayerInfos = PlayerInfos;
	}

	public boolean getEndOfGame() {
		return this.EndOfGame;
	}

	public void setEndOfGame( boolean EndOfGame ) {
		this.EndOfGame = EndOfGame;
	}

}