package de.tudresden.inf.rn.mobilis.android.ninecards.clientstub;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
public class MobilisNineCardsProxy {

	private IMobilisNineCardsOutgoing _bindingStub;


	public MobilisNineCardsProxy( IMobilisNineCardsOutgoing bindingStub) {
		_bindingStub = bindingStub;
	}


	public IMobilisNineCardsOutgoing getBindingStub(){
		return _bindingStub;
	}


	public XMPPBean ConfigureGame( String toJid, String GameName, int MaxPlayers, int NumberOfRounds, IXMPPCallback< ConfigureGameResponse > callback ) {
		if ( null == _bindingStub || null == callback )
			return null;

		ConfigureGameRequest out = new ConfigureGameRequest( GameName, MaxPlayers, NumberOfRounds );
		out.setTo( toJid );

		_bindingStub.sendXMPPBean( out, callback );

		return out;
	}

	public XMPPBean JoinGame( String toJid, IXMPPCallback< JoinGameResponse > callback ) {
		if ( null == _bindingStub || null == callback )
			return null;

		JoinGameRequest out = new JoinGameRequest(  );
		out.setTo( toJid );

		_bindingStub.sendXMPPBean( out, callback );

		return out;
	}

}