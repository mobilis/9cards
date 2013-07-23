package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;


public class MessageWrapper implements XMPPInfo {

	private boolean IsSystemMessage = false;
	private String MessageString = null;
	private String MessageType = null;


	public MessageWrapper( boolean IsSystemMessage, String MessageString, String MessageType ) {
		super();
		this.IsSystemMessage = IsSystemMessage;
		this.MessageString = MessageString;
		this.MessageType = MessageType;
	}

	public MessageWrapper(){}



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
				else if (tagName.equals( "IsSystemMessage" ) ) {
					this.IsSystemMessage = Boolean.parseBoolean( parser.nextText() );
				}
				else if (tagName.equals( "MessageString" ) ) {
					this.MessageString = parser.nextText();
				}
				else if (tagName.equals( "MessageType" ) ) {
					this.MessageType = parser.nextText();
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

	public static final String CHILD_ELEMENT = "MessageWrapper";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:MessageWrapper";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<IsSystemMessage>" )
			.append( this.IsSystemMessage )
			.append( "</IsSystemMessage>" );

		sb.append( "<MessageString>" )
			.append( this.MessageString )
			.append( "</MessageString>" );

		sb.append( "<MessageType>" )
			.append( this.MessageType )
			.append( "</MessageType>" );

		return sb.toString();
	}



	public boolean getIsSystemMessage() {
		return this.IsSystemMessage;
	}

	public void setIsSystemMessage( boolean IsSystemMessage ) {
		this.IsSystemMessage = IsSystemMessage;
	}

	public String getMessageString() {
		return this.MessageString;
	}

	public void setMessageString( String MessageString ) {
		this.MessageString = MessageString;
	}

	public String getMessageType() {
		return this.MessageType;
	}

	public void setMessageType( String MessageType ) {
		this.MessageType = MessageType;
	}

}