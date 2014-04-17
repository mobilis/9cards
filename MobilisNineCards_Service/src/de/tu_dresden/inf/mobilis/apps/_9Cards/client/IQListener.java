package de.tu_dresden.inf.mobilis.apps._9Cards.client;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tu_dresden.inf.rn.mobilis.services.ninecards.communication.MucConnection;
import java.util.logging.Logger;

public abstract class AbstractClientIQListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());

@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
				} else if (proxyBean.isTypeOf(GetGameConfigurationResponse.NAMESPACE,
						GetGameConfigurationResponse.CHILD_ELEMENT)) {
					_onGetGameConfigurationResponse((GetGameConfigurationResponse) proxyBean
							.parsePayload(new GetGameConfigurationResponse()));
				} else if (proxyBean.isTypeOf(ConfigureGameResponse.NAMESPACE,
						ConfigureGameResponse.CHILD_ELEMENT)) {
					_onConfigureGameResponse((ConfigureGameResponse) proxyBean
							.parsePayload(new ConfigureGameResponse()));
				} else {
					throw new Exception("No responsible type for received proxyBean!");
				}
			}
		}
	}

	public abstract void onGetGameConfigurationResponse(GetGameConfigurationResponse inBean);
	
	public abstract void onConfigureGameResponse(ConfigureGameResponse inBean);
	
}