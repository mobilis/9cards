package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;import org.xmlpull.v1.XmlPullParser;import java.util.List;import java.util.ArrayList;

public class PlayerInfo implements XMPPInfo {

	private String id = null;
	private int score = Integer.MIN_VALUE;
	private List< Integer > usedcards = new ArrayList< Integer >();


	public PlayerInfo( String id, int score, List< Integer > usedcards ) {
		super();
		this.id = id;
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
				else if (tagName.equals( "id" ) ) {
					this.id = parser.nextText();
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

	public static final String NAMESPACE = "http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<id>" )
			.append( this.id )
			.append( "</id>" );

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



	public String getId() {
		return this.id;
	}

	public void setId( String id ) {
		this.id = id;
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