package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.android.ninecards.communication.XMPPInfo;


public class CardPlayedMessage implements XMPPInfo {

	private int round = Integer.MIN_VALUE;
	private String player = null;


	public CardPlayedMessage( int round, String player ) {
		super();
		this.round = round;
		this.player = player;
	}

	public CardPlayedMessage(){}



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
				else if (tagName.equals( "player" ) ) {
					this.player = parser.nextText();
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

	public static final String CHILD_ELEMENT = "CardPlayedMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:CardPlayedMessage";

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

		sb.append( "<player>" )
			.append( this.player )
			.append( "</player>" );

		return sb.toString();
	}



	public int getRound() {
		return this.round;
	}

	public void setRound( int round ) {
		this.round = round;
	}

	public String getPlayer() {
		return this.player;
	}

	public void setPlayer( String player ) {
		this.player = player;
	}

}