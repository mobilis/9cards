package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;import java.util.List;import java.util.ArrayList;public class MobilisNineCardsProxy {

	private IMobilisNineCardsOutgoing _bindingStub;


	public MobilisNineCardsProxy( IMobilisNineCardsOutgoing bindingStub) {
		_bindingStub = bindingStub;
	}


	public IMobilisNineCardsOutgoing getBindingStub(){
		return _bindingStub;
	}


	public XMPPBean ConfigureGame( String toJid, String packetId, String muc ) {
		if ( null == _bindingStub )
			return null;

		ConfigureGameResponse out = new ConfigureGameResponse( muc );
		out.setTo( toJid );
		out.setId( packetId );

		_bindingStub.sendXMPPBean( out );

		return out;
	}

	public XMPPBean GetGameConfiguration( String toJid, String packetId, String muc, int maxRounds, int maxPlayers ) {
		if ( null == _bindingStub )
			return null;

		GetGameConfigurationResponse out = new GetGameConfigurationResponse( muc, maxRounds, maxPlayers );
		out.setTo( toJid );
		out.setId( packetId );

		_bindingStub.sendXMPPBean( out );

		return out;
	}

}