package de.tu_dresden.inf.mobilis.apps._9Cards.model;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;
import org.xmlpull.v1.XmlPullParser;
import java.util.List;
import java.util.ArrayList;

public class PlayerInfo implements XMPPInfo {

	private String id = null;
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	private Integer score = null;
	
	public Integer getScore() {
		return this.score;
	}
	
	public void setScore(Integer score) {
		this.score = score;
	}
	private List<Integer> usedcards = new ArrayList<Integer>();
	
	public List<Integer> getUsedcards() {
		return this.usedcards;
	}
	
	public void setUsedcards(List<Integer> usedcards) {
		this.usedcards = usedcards;
	}

	public PlayerInfo(String id, Integer score, List<Integer> usedcards) {
		this.id = id; 
		this.score = score; 
		this.usedcards = usedcards; 
	}
	
	public PlayerInfo() {
	}

	public static final String CHILD_ELEMENT = "PlayerInfo";

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
	public String toXML() {
		return this.payloadToXML();
	}

	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<id>");
		sb.append(this.id);
		sb.append("</id>");
		sb.append("<score>");
		sb.append(this.score);
		sb.append("</score>");
		for (Integer el : this.usedcards) {
			sb.append("<usedcards>");
			sb.append(el);
			sb.append("</usedcards>");
		}
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
					} else if (tagName.equals("id")) {
						String value = parser.nextText();
						this.id = value;
					} else if (tagName.equals("score")) {
						Integer value = new Integer(parser.nextText());
						this.score = value;
					} else if (tagName.equals("usedcards")) {
						Integer value = new Integer(parser.nextText());
						if (null == this.usedcards)
							this.usedcards = new ArrayList<>();
						this.usedcards.add(value);
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