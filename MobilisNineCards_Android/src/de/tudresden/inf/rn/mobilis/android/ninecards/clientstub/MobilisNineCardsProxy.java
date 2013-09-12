package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import de.tudresden.inf.rn.mobilis.xmpp.beans.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class MobilisNineCardsProxy {

	private IMobilisNineCardsOutgoing _bindingStub;


	public MobilisNineCardsProxy( IMobilisNineCardsOutgoing bindingStub) {
		_bindingStub = bindingStub;
	}


	public IMobilisNineCardsOutgoing getBindingStub(){
		return _bindingStub;
	}


	public XMPPBean ConfigureGame( String toJid, String gamename, int players, int rounds, IXMPPCallback< ConfigureGameResponse > callback ) {
		if ( null == _bindingStub || null == callback )
			return null;

		ConfigureGameRequest out = new ConfigureGameRequest( gamename, players, rounds );
		out.setTo( toJid );

		_bindingStub.sendXMPPBean( out, callback );

		return out;
	}

}