package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class Card implements XMPPInfo {

	private int Value = Integer.MIN_VALUE;
	private boolean AlreadyUsed = false;


	public Card( int Value, boolean AlreadyUsed ) {
		super();
		this.Value = Value;
		this.AlreadyUsed = AlreadyUsed;
	}

	public Card(){}



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
				else if (tagName.equals( "Value" ) ) {
					this.Value = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "AlreadyUsed" ) ) {
					this.AlreadyUsed = Boolean.parseBoolean( parser.nextText() );
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

	public static final String CHILD_ELEMENT = "Card";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:Card";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<Value>" )
			.append( this.Value )
			.append( "</Value>" );

		sb.append( "<AlreadyUsed>" )
			.append( this.AlreadyUsed )
			.append( "</AlreadyUsed>" );

		return sb.toString();
	}



	public int getValue() {
		return this.Value;
	}

	public void setValue( int Value ) {
		this.Value = Value;
	}

	public boolean getAlreadyUsed() {
		return this.AlreadyUsed;
	}

	public void setAlreadyUsed( boolean AlreadyUsed ) {
		this.AlreadyUsed = AlreadyUsed;
	}

}