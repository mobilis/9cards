package de.tudresden.inf.rn.mobilis.android.ninecards.message;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;

public class ConfigureGameRequest extends XMPPBean {

	private String gamename = null;
	private int players = Integer.MIN_VALUE;
	private int rounds = Integer.MIN_VALUE;


	public ConfigureGameRequest( String gamename, int players, int rounds ) {
		super();
		this.gamename = gamename;
		this.players = players;
		this.rounds = rounds;

		this.setType( XMPPBean.TYPE_SET );
	}

	public ConfigureGameRequest(){
		this.setType( XMPPBean.TYPE_SET );
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
				else if (tagName.equals( "gamename" ) ) {
					this.gamename = parser.nextText();
				}
				else if (tagName.equals( "players" ) ) {
					this.players = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "rounds" ) ) {
					this.rounds = Integer.parseInt( parser.nextText() );
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

	public static final String CHILD_ELEMENT = "ConfigureGameRequest";

	@Override
	public String getChildElement() {
		return CHILD_ELEMENT;
	}

	public static final String NAMESPACE = "mobilisninecards:iq:configuregame";

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public XMPPBean clone() {
		ConfigureGameRequest clone = new ConfigureGameRequest( gamename, players, rounds );
		this.cloneBasicAttributes( clone );

		return clone;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<gamename>" )
			.append( this.gamename )
			.append( "</gamename>" );

		sb.append( "<players>" )
			.append( this.players )
			.append( "</players>" );

		sb.append( "<rounds>" )
			.append( this.rounds )
			.append( "</rounds>" );

		sb = appendErrorPayload(sb);

		return sb.toString();
	}


	public String getGamename() {
		return this.gamename;
	}

	public void setGamename( String gamename ) {
		this.gamename = gamename;
	}

	public int getPlayers() {
		return this.players;
	}

	public void setPlayers( int players ) {
		this.players = players;
	}

	public int getRounds() {
		return this.rounds;
	}

	public void setRounds( int rounds ) {
		this.rounds = rounds;
	}

}