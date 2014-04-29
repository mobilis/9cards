package de.tu_dresden.inf.mobilis.apps._9Cards.service;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.mxj.BeanIQAdapter;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.PlayCardMessage;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.StartGameMessage;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.util.logging.Logger;

public abstract class AbstractMessageListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(AbstractMessageListener.class.getCanonicalName());

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
				if (proxyBean.isTypeOf(PlayCardMessage.NAMESPACE,
						PlayCardMessage.CHILD_ELEMENT)) {
					onPlayCardMessage((PlayCardMessage) proxyBean
							.parsePayload(new PlayCardMessage()));
				} else if (proxyBean.isTypeOf(StartGameMessage.NAMESPACE,
						StartGameMessage.CHILD_ELEMENT)) {
					onStartGameMessage((StartGameMessage) proxyBean
							.parsePayload(new StartGameMessage()));
				} else {
					LOGGER.warning("No responsible type for received proxyBean!");
				}
			}
		}
	}

	public abstract void onPlayCardMessage(PlayCardMessage inBean);
	
	public abstract void onStartGameMessage(StartGameMessage inBean);
	
}