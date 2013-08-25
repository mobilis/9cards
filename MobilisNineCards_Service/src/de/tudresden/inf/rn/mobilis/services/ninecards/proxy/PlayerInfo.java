package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

public class PlayerInfo implements XMPPInfo {

	private String jid = null;
	private int score = Integer.MIN_VALUE;
	private List< Integer > usedcards = new ArrayList< Integer >();


	public PlayerInfo( String jid, int score, List< Integer > usedcards ) {
		super();
		this.jid = jid;
		this.score = score;
		for ( int entity : usedcards ) {
			this.usedcards.add( entity );
		}
	}

	public PlayerInfo(){}



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
				else if (tagName.equals( "jid" ) ) {
					this.jid = parser.nextText();
				}
				else if (tagName.equals( "score" ) ) {
					this.score = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "usedcards" ) ) {
					usedcards.add( Integer.parseInt( parser.nextText() ) );
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

	public static final String CHILD_ELEMENT = "PlayerInfo";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService#type:PlayerInfo";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<jid>" )
			.append( this.jid )
			.append( "</jid>" );

		sb.append( "<score>" )
			.append( this.score )
			.append( "</score>" );

		for( int entry : usedcards ) {
			sb.append( "<usedcards>" );
			sb.append( entry );
			sb.append( "</usedcards>" );
		}

		return sb.toString();
	}



	public String getJid() {
		return this.jid;
	}

	public void setJid( String jid ) {
		this.jid = jid;
	}

	public int getScore() {
		return this.score;
	}

	public void setScore( int score ) {
		this.score = score;
	}

	public List< Integer > getUsedcards() {
		return this.usedcards;
	}

	public void setUsedcards( List< Integer > usedcards ) {
		this.usedcards = usedcards;
	}

}