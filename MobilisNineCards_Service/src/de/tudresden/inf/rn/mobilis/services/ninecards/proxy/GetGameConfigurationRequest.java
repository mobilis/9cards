package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;import org.xmlpull.v1.XmlPullParser;import java.util.List;import java.util.ArrayList;

public class GetGameConfigurationRequest extends XMPPBean {

	public GetGameConfigurationRequest(){
		this.setType( XMPPBean.TYPE_SET );
	}


	@Override
	public void fromXML( XmlPullParser parser ) throws Exception {}

	public static final String CHILD_ELEMENT = "GetGameConfigurationRequest";

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
		GetGameConfigurationRequest clone = new GetGameConfigurationRequest(  );
		this.cloneBasicAttributes( clone );

		return clone;
	}

	@Override
	public String payloadToXML() { return ""; }


	public GetGameConfigurationRequest buildGameConfigFault(String detailedErrorText){
		GetGameConfigurationRequest fault = ( GetGameConfigurationRequest )this.clone();

		fault.setTo( this.getFrom() );
    	fault.setId(this.getId());
		fault.setType( XMPPBean.TYPE_ERROR );
		fault.errorType = "cancel";
		fault.errorCondition = "not-allowed";
		fault.errorText = "Not allowed in current State.";

		if(null != detailedErrorText && detailedErrorText.length() > 0)
			fault.errorText += " Detail: " + detailedErrorText;
		
		return fault;
	}





}