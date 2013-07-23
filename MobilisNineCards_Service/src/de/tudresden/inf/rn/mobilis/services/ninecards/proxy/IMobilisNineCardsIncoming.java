package de.tudresden.inf.rn.mobilis.services.ninecards.proxy;

import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;


public interface IMobilisNineCardsIncoming {

	XMPPBean onConfigureGame( ConfigureGameRequest in );

	XMPPBean onJoinGame( JoinGameRequest in );

}