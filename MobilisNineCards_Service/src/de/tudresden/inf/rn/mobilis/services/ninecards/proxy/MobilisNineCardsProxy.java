package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
public class MobilisNineCardsProxy {

	private IMobilisNineCardsOutgoing _bindingStub;


	public MobilisNineCardsProxy( IMobilisNineCardsOutgoing bindingStub) {
		_bindingStub = bindingStub;
	}


	public IMobilisNineCardsOutgoing getBindingStub(){
		return _bindingStub;
	}


	public XMPPBean ConfigureGame( String toJid, String packetId ) {
		if ( null == _bindingStub )
			return null;

		ConfigureGameResponse out = new ConfigureGameResponse(  );
		out.setTo( toJid );
		out.setId( packetId );

		_bindingStub.sendXMPPBean( out );

		return out;
	}

}