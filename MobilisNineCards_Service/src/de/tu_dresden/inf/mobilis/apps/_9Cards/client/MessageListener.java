package de.tu_dresden.inf.mobilis.apps._9Cards.client;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.RoundCompleteMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GameStartsMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GameOverMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.CardPlayedMessage;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.util.logging.Logger;

public abstract class AbstractClientMessageListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(AbstractClientMessageListener.class.getCanonicalName());

@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
				if (proxyBean.isTypeOf(RoundCompleteMessage.NAMESPACE,
						RoundCompleteMessage.CHILD_ELEMENT)) {
					onRoundCompleteMessage((RoundCompleteMessage) proxyBean
							.parsePayload(new RoundCompleteMessage()));
				} else if (proxyBean.isTypeOf(GameStartsMessage.NAMESPACE,
						GameStartsMessage.CHILD_ELEMENT)) {
					onGameStartsMessage((GameStartsMessage) proxyBean
							.parsePayload(new GameStartsMessage()));
				} else if (proxyBean.isTypeOf(GameOverMessage.NAMESPACE,
						GameOverMessage.CHILD_ELEMENT)) {
					onGameOverMessage((GameOverMessage) proxyBean
							.parsePayload(new GameOverMessage()));
				} else if (proxyBean.isTypeOf(CardPlayedMessage.NAMESPACE,
						CardPlayedMessage.CHILD_ELEMENT)) {
					onCardPlayedMessage((CardPlayedMessage) proxyBean
							.parsePayload(new CardPlayedMessage()));
				} else {
					throw new Exception("No responsible type for received proxyBean!");
				}
			}
		}
	}

	public abstract void onRoundCompleteMessage(RoundCompleteMessage inBean);
	
	public abstract void onGameStartsMessage(GameStartsMessage inBean);
	
	public abstract void onGameOverMessage(GameOverMessage inBean);
	
	public abstract void onCardPlayedMessage(CardPlayedMessage inBean);
	
}