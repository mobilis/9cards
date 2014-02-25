package de.tudresden.inf.rn.mobilis.android.ninecards.message;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;

public class GameStartsMessage implements XMPPInfo {

	private int rounds = Integer.MIN_VALUE;


	public GameStartsMessage( int rounds ) {
		super();
		this.rounds = rounds;
	}

	public GameStartsMessage(){}



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
				else if (tagName.equals( "rounds" ) ) {
					this.rounds = Integer.parseInt( parser.nextText() );
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

	public static final String CHILD_ELEMENT = "GameStartsMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:GameStartsMessage";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<rounds>" )
			.append( this.rounds )
			.append( "</rounds>" );

		return sb.toString();
	}



	public int getRounds() {
		return this.rounds;
	}

	public void setRounds( int rounds ) {
		this.rounds = rounds;
	}

}