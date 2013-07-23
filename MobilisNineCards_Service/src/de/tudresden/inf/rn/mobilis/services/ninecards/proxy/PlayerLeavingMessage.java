package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayerLeavingMessage implements XMPPInfo {

	private String LeavingJID = null;


	public PlayerLeavingMessage( String LeavingJID ) {
		super();
		this.LeavingJID = LeavingJID;
	}

	public PlayerLeavingMessage(){}



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
				else if (tagName.equals( "LeavingJID" ) ) {
					this.LeavingJID = parser.nextText();
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

	public static final String CHILD_ELEMENT = "PlayerLeavingMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:PlayerLeavingMessage";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<LeavingJID>" )
			.append( this.LeavingJID )
			.append( "</LeavingJID>" );

		return sb.toString();
	}



	public String getLeavingJID() {
		return this.LeavingJID;
	}

	public void setLeavingJID( String LeavingJID ) {
		this.LeavingJID = LeavingJID;
	}

}