package de.tu_dresden.inf.mobilis.apps._9Cards.beans;

import java.util.List;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

import de.tu_dresden.inf.mobilis.apps._9Cards.model.PlayerInfo;	

public class RoundCompleteMessage extends XMPPBean {

	private Integer round = null;
	
	public Integer getRound() {
		return this.round;
	}
	
	public void setRound(Integer round) {
		this.round = round;
	}
	private String winner = null;
	
	public String getWinner() {
		return this.winner;
	}
	
	public void setWinner(String winner) {
		this.winner = winner;
	}
	private List<PlayerInfo> playerInfos = new ArrayList<PlayerInfo>();
	
	public List<PlayerInfo> getPlayerInfos() {
		return this.playerInfos;
	}
	
	public void setPlayerInfos(List<PlayerInfo> playerInfos) {
		this.playerInfos = playerInfos;
	}

	public RoundCompleteMessage(Integer round, String winner, List<PlayerInfo> playerInfos) {
		this.round = round; 
		this.winner = winner; 
		this.playerInfos = playerInfos; 
	}
	
	public RoundCompleteMessage() {
	}

	public static final String CHILD_ELEMENT = "RoundCompleteMessage";

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
	RoundCompleteMessage clone = new RoundCompleteMessage();
		this.cloneBasicAttributes(clone);
		return clone;
	}
	
	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();
		sb.append("<round>");
		sb.append(this.round);
		sb.append("</round>");
		sb.append("<winner>");
		sb.append(this.winner);
		sb.append("</winner>");
		for (PlayerInfo el : this.playerInfos) {
			sb.append("<playerInfos>");
			sb.append(el.payloadToXML());
			sb.append("</playerInfos>");
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
					} else if (tagName.equals("round")) {
						Integer value = new Integer(parser.nextText());
						this.round = value;
					} else if (tagName.equals("winner")) {
						String value = parser.nextText();
						this.winner = value;
					} else if (tagName.equals("playerInfos")) {
						PlayerInfo value = new PlayerInfo();
						value.fromXML(parser);
						if (null == this.playerInfos)
							this.playerInfos = new ArrayList<PlayerInfo>();
						this.playerInfos.add(value);
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