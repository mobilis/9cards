package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class CardPlayedMessage extends XMPPBean {

	private Integer round = null;
	
	public Integer getRound() {
		return this.round;
	}
	
	public void setRound(Integer round) {
		this.round = round;
	}
	private String player = null;
	
	public String getPlayer() {
		return this.player;
	}
	
	public void setPlayer(String player) {
		this.player = player;
	}

	public CardPlayedMessage(Integer round, String player) {
		this.round = round; 
		this.player = player; 
	}
	
	public CardPlayedMessage() {
	}

	public static final String CHILD_ELEMENT = "CardPlayedMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de/apps/9Cards";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
	CardPlayedMessage clone = new CardPlayedMessage();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<round>");
		sb.append(this.round);
		sb.append("</round>");
		sb.append("<player>");
		sb.append(this.player);
		sb.append("</player>");
		return sb.toString();
	}

	@Override
	public void fromXML(XmlPullParser parser) throws Exception {
		boolean done = false;
	
		do {
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG:
					String tagName = parser.getName();
			
					if (tagName.equals(getChildElement())) {
						parser.next();
					} else if (tagName.equals("round")) {
						Integer value = new Integer(parser.nextText());
						this.round = value;
					} else if (tagName.equals("player")) {
						String value = parser.nextText();
						this.player = value;
					} else
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
}