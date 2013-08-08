package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayerInfosMessage implements XMPPInfo {

	private List< PlayerInfo > Players = new ArrayList< PlayerInfo >();


	public PlayerInfosMessage( List< PlayerInfo > Players ) {
		super();
		for ( PlayerInfo entity : Players ) {
			this.Players.add( entity );
		}
	}

	public PlayerInfosMessage(){}



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
				else if (tagName.equals( PlayerInfo.CHILD_ELEMENT ) ) {
					PlayerInfo entity = new PlayerInfo();

					entity.fromXML( parser );
					this.Players.add( entity );
					
					parser.next();
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

	public static final String CHILD_ELEMENT = "PlayerInfosMessage";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:PlayerInfosMessage";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		for( PlayerInfo entry : Players ) {
			sb.append( "<" + PlayerInfo.CHILD_ELEMENT + ">" );
			sb.append( entry.toXML() );
			sb.append( "</" + PlayerInfo.CHILD_ELEMENT + ">" );
		}

		return sb.toString();
	}



	public List< PlayerInfo > getPlayers() {
		return this.Players;
	}

	public void setPlayers( List< PlayerInfo > Players ) {
		this.Players = Players;
	}

}