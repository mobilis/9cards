package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class JoinGameResponse extends XMPPBean {

	private String ChatRoom = null;
	private String ChatPassword = null;


	public JoinGameResponse( String ChatRoom, String ChatPassword ) {
		super();
		this.ChatRoom = ChatRoom;
		this.ChatPassword = ChatPassword;

		this.setType( XMPPBean.TYPE_RESULT );
	}

	public JoinGameResponse(){
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
				else if (tagName.equals( "ChatRoom" ) ) {
					this.ChatRoom = parser.nextText();
				}
				else if (tagName.equals( "ChatPassword" ) ) {
					this.ChatPassword = parser.nextText();
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

	public static final String CHILD_ELEMENT = "JoinGameResponse";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "mobilisninecards:iq:joingame";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
		JoinGameResponse clone = new JoinGameResponse( ChatRoom, ChatPassword );
		this.cloneBasicAttributes( clone );

		return clone;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<ChatRoom>" )
			.append( this.ChatRoom )
			.append( "</ChatRoom>" );

		sb.append( "<ChatPassword>" )
			.append( this.ChatPassword )
			.append( "</ChatPassword>" );

		sb = appendErrorPayload(sb);

		return sb.toString();
	}


	public String getChatRoom() {
		return this.ChatRoom;
	}

	public void setChatRoom( String ChatRoom ) {
		this.ChatRoom = ChatRoom;
	}

	public String getChatPassword() {
		return this.ChatPassword;
	}

	public void setChatPassword( String ChatPassword ) {
		this.ChatPassword = ChatPassword;
	}

}