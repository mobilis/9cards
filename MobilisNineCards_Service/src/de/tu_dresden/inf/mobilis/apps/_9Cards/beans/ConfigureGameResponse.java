

package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class ConfigureGameResponse extends XMPPBean {

	private String muc = null;
	
	public String getMuc() {
		return this.muc;
	}
	
	public void setMuc(String muc) {
		this.muc = muc;
	}

	public ConfigureGameResponse(String muc) {
		this.muc = muc; 
		this.setType(XMPPBean.TYPE_RESULT);
	}
	
	public ConfigureGameResponse() {
		this.setType(XMPPBean.TYPE_RESULT);
	}

	public static final String CHILD_ELEMENT = "ConfigureGameResponse";

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
	ConfigureGameResponse clone = new ConfigureGameResponse();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<muc>");
		sb.append(this.muc);
		sb.append("</muc>");
		
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