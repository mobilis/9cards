package de.tudresden.inf.rn.mobilis.android.ninecards.message;

import org.xmlpull.v1.XmlPullParser;

import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;

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


}