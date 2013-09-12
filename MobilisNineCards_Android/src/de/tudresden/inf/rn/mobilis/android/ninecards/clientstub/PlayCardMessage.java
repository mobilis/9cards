package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayCardMessage implements XMPPInfo {

	private int round = Integer.MIN_VALUE;
	private int card = Integer.MIN_VALUE;


	public PlayCardMessage( int round, int card ) {
		super();
		this.round = round;
		this.card = card;
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
				else if (tagName.equals( "round" ) ) {
					this.round = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "card" ) ) {
					this.card = Integer.parseInt( parser.nextText() );
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

		sb.append( "<round>" )
			.append( this.round )
			.append( "</round>" );

		sb.append( "<card>" )
			.append( this.card )
			.append( "</card>" );

		return sb.toString();
	}



	public int getRound() {
		return this.round;
	}

	public void setRound( int round ) {
		this.round = round;
	}

	public int getCard() {
		return this.card;
	}

	public void setCard( int card ) {
		this.card = card;
	}

}