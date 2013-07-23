package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public class ConfigureGameRequest extends XMPPBean {

	private String GameName = null;
	private int MaxPlayers = Integer.MIN_VALUE;
	private int NumberOfRounds = Integer.MIN_VALUE;


	public ConfigureGameRequest( String GameName, int MaxPlayers, int NumberOfRounds ) {
		super();
		this.GameName = GameName;
		this.MaxPlayers = MaxPlayers;
		this.NumberOfRounds = NumberOfRounds;

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
				else if (tagName.equals( "GameName" ) ) {
					this.GameName = parser.nextText();
				}
				else if (tagName.equals( "MaxPlayers" ) ) {
					this.MaxPlayers = Integer.parseInt( parser.nextText() );
				}
				else if (tagName.equals( "NumberOfRounds" ) ) {
					this.NumberOfRounds = Integer.parseInt( parser.nextText() );
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
		ConfigureGameRequest clone = new ConfigureGameRequest( GameName, MaxPlayers, NumberOfRounds );
		this.cloneBasicAttributes( clone );

		return clone;
	}

	@Override
	public String payloadToXML() {
		StringBuilder sb = new StringBuilder();

		sb.append( "<GameName>" )
			.append( this.GameName )
			.append( "</GameName>" );

		sb.append( "<MaxPlayers>" )
			.append( this.MaxPlayers )
			.append( "</MaxPlayers>" );

		sb.append( "<NumberOfRounds>" )
			.append( this.NumberOfRounds )
			.append( "</NumberOfRounds>" );

		sb = appendErrorPayload(sb);

		return sb.toString();
	}


	public ConfigureGameRequest buildInputDataFault(String detailedErrorText){
		ConfigureGameRequest fault = ( ConfigureGameRequest )this.clone();

		fault.setTo( this.getFrom() );
    	fault.setId(this.getId());
		fault.setType( XMPPBean.TYPE_ERROR );
		fault.errorType = "modify";
		fault.errorCondition = "not-acceptable";
		fault.errorText = "Unaccepted data input.";

		if(null != detailedErrorText && detailedErrorText.length() > 0)
			fault.errorText += " Detail: " + detailedErrorText;
		
		return fault;
	}





	public String getGameName() {
		return this.GameName;
	}

	public void setGameName( String GameName ) {
		this.GameName = GameName;
	}

	public int getMaxPlayers() {
		return this.MaxPlayers;
	}

	public void setMaxPlayers( int MaxPlayers ) {
		this.MaxPlayers = MaxPlayers;
	}

	public int getNumberOfRounds() {
		return this.NumberOfRounds;
	}

	public void setNumberOfRounds( int NumberOfRounds ) {
		this.NumberOfRounds = NumberOfRounds;
	}

}