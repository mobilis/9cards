package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;import org.xmlpull.v1.XmlPullParser;import java.util.List;import java.util.ArrayList;

public class GetGameConfigurationResponse extends XMPPBean {

	private String muc = null;
	private int maxRounds = Integer.MIN_VALUE;
	private int maxPlayers = Integer.MIN_VALUE;


	public GetGameConfigurationResponse( String muc, int maxRounds, int maxPlayers ) {
		super();
		this.muc = muc;
		this.maxRounds = maxRounds;
		this.maxPlayers = maxPlayers;

		this.setType( XMPPBean.TYPE_RESULT );
	}

	public GetGameConfigurationResponse(){
		this.setType( XMPPBean.TYPE_RESULT );
	}


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
				else if (tagName.equals( "muc" ) ) {
					this.muc = parser.nextText();
				}
				else if (tagName.equals( "maxRounds" ) ) {
					this.maxRounds = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "maxPlayers" ) ) {
					this.maxPlayers = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals("error")) {
					parser = parseErrorAttributes(parser);
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

	public static final String CHILD_ELEMENT = "GetGameConfigurationResponse";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de/apps/9cards";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
		GetGameConfigurationResponse clone = new GetGameConfigurationResponse( muc, maxRounds, maxPlayers );
		this.cloneBasicAttributes( clone );

		return clone;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<muc>" )
			.append( this.muc )
			.append( "</muc>" );

		sb.append( "<maxRounds>" )
			.append( this.maxRounds )
			.append( "</maxRounds>" );

		sb.append( "<maxPlayers>" )
			.append( this.maxPlayers )
			.append( "</maxPlayers>" );

		sb = appendErrorPayload(sb);

		return sb.toString();
	}


	public String getMuc() {
		return this.muc;
	}

	public void setMuc( String muc ) {
		this.muc = muc;
	}

	public int getMaxRounds() {
		return this.maxRounds;
	}

	public void setMaxRounds( int maxRounds ) {
		this.maxRounds = maxRounds;
	}

	public int getMaxPlayers() {
		return this.maxPlayers;
	}

	public void setMaxPlayers( int maxPlayers ) {
		this.maxPlayers = maxPlayers;
	}

}