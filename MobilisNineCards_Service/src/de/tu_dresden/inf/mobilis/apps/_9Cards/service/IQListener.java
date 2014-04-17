package de.tu_dresden.inf.mobilis.apps._9Cards.service;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.server.BeanIQAdapter;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import de.tu_dresden.inf.rn.mobilis.services.ninecards.communication.MucConnection;
import java.util.logging.Logger;

public abstract class AbstractServiceIQListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(MucConnection.class.getCanonicalName());

@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
				if (proxyBean.isTypeOf(GetGameConfigurationRequest.NAMESPACE,
						GetGameConfigurationRequest.CHILD_ELEMENT)) {
					_onGetGameConfigurationRequest((GetGameConfigurationRequest) proxyBean
							.parsePayload(new GetGameConfigurationRequest()));
				} else if (proxyBean.isTypeOf(ConfigureGameRequest.NAMESPACE,
						ConfigureGameRequest.CHILD_ELEMENT)) {
					_onConfigureGameRequest((ConfigureGameRequest) proxyBean
							.parsePayload(new ConfigureGameRequest()));
				} else {
					throw new Exception("No responsible type for received proxyBean!");
				}
			}
		}
	}

	public final void _onGetGameConfigurationRequest(GetGameConfigurationRequest inBean) {
		GetGameConfigurationResponse out = new GetGameConfigurationResponse();
		out.setId(inBean.getId());
		out.setTo(inBean.getFrom());
		this.onGetGameConfigurationRequest(inBean, out);
	}
	
	public abstract void onGetGameConfigurationRequest(GetGameConfigurationRequest inBean, GetGameConfigurationResponse outBean);
	
	public final void _onConfigureGameRequest(ConfigureGameRequest inBean) {
		ConfigureGameResponse out = new ConfigureGameResponse();
		out.setId(inBean.getId());
		out.setTo(inBean.getFrom());
		this.onConfigureGameRequest(inBean, out);
	}
	
	public abstract void onConfigureGameRequest(ConfigureGameRequest inBean, ConfigureGameResponse outBean);
	
}