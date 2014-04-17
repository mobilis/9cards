package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class PlayCardMessage extends XMPPBean {

	private Integer round = null;
	
	public Integer getRound() {
		return this.round;
	}
	
	public void setRound(Integer round) {
		this.round = round;
	}
	private Integer card = null;
	
	public Integer getCard() {
		return this.card;
	}
	
	public void setCard(Integer card) {
		this.card = card;
	}

	public PlayCardMessage(Integer round, Integer card) {
		this.round = round; 
		this.card = card; 
	}
	
	public PlayCardMessage() {
	}

	public static final String CHILD_ELEMENT = "PlayCardMessage";

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
	PlayCardMessage clone = new PlayCardMessage();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<round>");
		sb.append(this.round);
		sb.append("</round>");
		sb.append("<card>");
		sb.append(this.card);
		sb.append("</card>");
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
					} else if (tagName.equals("card")) {
						Integer value = new Integer(parser.nextText());
						this.card = value;
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