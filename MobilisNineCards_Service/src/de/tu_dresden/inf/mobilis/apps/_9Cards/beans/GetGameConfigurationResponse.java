

package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class GetGameConfigurationResponse extends XMPPBean {

	private String muc = null;
	
	public String getMuc() {
		return this.muc;
	}
	
	public void setMuc(String muc) {
		this.muc = muc;
	}
	private Integer maxRounds = null;
	
	public Integer getMaxRounds() {
		return this.maxRounds;
	}
	
	public void setMaxRounds(Integer maxRounds) {
		this.maxRounds = maxRounds;
	}
	private Integer maxPlayers = null;
	
	public Integer getMaxPlayers() {
		return this.maxPlayers;
	}
	
	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public GetGameConfigurationResponse(String muc, Integer maxRounds, Integer maxPlayers) {
		this.muc = muc; 
		this.maxRounds = maxRounds; 
		this.maxPlayers = maxPlayers; 
		this.setType(XMPPBean.TYPE_RESULT);
	}
	
	public GetGameConfigurationResponse() {
		this.setType(XMPPBean.TYPE_RESULT);
	}

	public static final String CHILD_ELEMENT = "GetGameConfigurationResponse";

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
	GetGameConfigurationResponse clone = new GetGameConfigurationResponse();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<muc>");
		sb.append(this.muc);
		sb.append("</muc>");
		sb.append("<maxRounds>");
		sb.append(this.maxRounds);
		sb.append("</maxRounds>");
		sb.append("<maxPlayers>");
		sb.append(this.maxPlayers);
		sb.append("</maxPlayers>");
		
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
					} else if (tagName.equals("muc")) {
						String value = parser.nextText();
						this.muc = value;
					} else if (tagName.equals("maxRounds")) {
						Integer value = new Integer(parser.nextText());
						this.maxRounds = value;
					} else if (tagName.equals("maxPlayers")) {
						Integer value = new Integer(parser.nextText());
						this.maxPlayers = value;
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