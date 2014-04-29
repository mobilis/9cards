

package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class ConfigureGameRequest extends XMPPBean {

	private Integer players = null;
	
	public Integer getPlayers() {
		return this.players;
	}
	
	public void setPlayers(Integer players) {
		this.players = players;
	}
	private Integer rounds = null;
	
	public Integer getRounds() {
		return this.rounds;
	}
	
	public void setRounds(Integer rounds) {
		this.rounds = rounds;
	}

	public ConfigureGameRequest(Integer players, Integer rounds) {
		this.players = players; 
		this.rounds = rounds; 
		this.setType(XMPPBean.TYPE_SET);
	}
	
	public ConfigureGameRequest() {
		this.setType(XMPPBean.TYPE_SET);
	}

	public static final String CHILD_ELEMENT = "ConfigureGameRequest";

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
	ConfigureGameRequest clone = new ConfigureGameRequest();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<players>");
		sb.append(this.players);
		sb.append("</players>");
		sb.append("<rounds>");
		sb.append(this.rounds);
		sb.append("</rounds>");
		
		sb = this.appendErrorPayload(sb);

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
					} else if (tagName.equals("players")) {
						Integer value = new Integer(parser.nextText());
						this.players = value;
					} else if (tagName.equals("rounds")) {
						Integer value = new Integer(parser.nextText());
						this.rounds = value;
					} else if (tagName.equals("error")) {
						parser = this.parseErrorAttributes(parser);
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