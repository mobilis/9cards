package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayCardMessage implements XMPPInfo {

	private String PlayersName = null;
	private String PlayersJID = null;
	private int CardID = Integer.MIN_VALUE;


	public PlayCardMessage( String PlayersName, String PlayersJID, int CardID ) {
		super();
		this.PlayersName = PlayersName;
		this.PlayersJID = PlayersJID;
		this.CardID = CardID;
	}

	public PlayCardMessage(){}



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
				else if (tagName.equals( "CardID" ) ) {
					this.CardID = Integer.parseInt( parser.nextText() );
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

	public static final String CHILD_ELEMENT = "PlayCardMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:PlayCardMessage";

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

		sb.append( "<CardID>" )
			.append( this.CardID )
			.append( "</CardID>" );

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

	public int getCardID() {
		return this.CardID;
	}

	public void setCardID( int CardID ) {
		this.CardID = CardID;
	}

}