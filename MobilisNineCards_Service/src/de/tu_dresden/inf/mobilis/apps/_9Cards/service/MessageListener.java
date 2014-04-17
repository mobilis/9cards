package de.tu_dresden.inf.mobilis.apps._9Cards.service;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tu_dresden.inf.rn.mobilis.services.ninecards.communication.MucConnection;
import java.util.logging.Logger;

public abstract class AbstractServiceMessageListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());

@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
				} else if (proxyBean.isTypeOf(StartGameMessage.NAMESPACE,
						StartGameMessage.CHILD_ELEMENT)) {
					onStartGameMessage((StartGameMessage) proxyBean
							.parsePayload(new StartGameMessage()));
				} else if (proxyBean.isTypeOf(PlayCardMessage.NAMESPACE,
						PlayCardMessage.CHILD_ELEMENT)) {
					onPlayCardMessage((PlayCardMessage) proxyBean
							.parsePayload(new PlayCardMessage()));
				} else {
					throw new Exception("No responsible type for received proxyBean!");
				}
			}
		}
	}

	public abstract void onStartGameMessage(StartGameMessage inBean);
	
	public abstract void onPlayCardMessage(PlayCardMessage inBean);
	
}