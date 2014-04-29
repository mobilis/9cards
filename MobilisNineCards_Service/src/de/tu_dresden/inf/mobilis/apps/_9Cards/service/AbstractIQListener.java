package de.tu_dresden.inf.mobilis.apps._9Cards.service;

import de.tudresden.inf.rn.mobilis.xmpp.beans.ProxyBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.mxj.BeanIQAdapter;
import de.tudresden.inf.rn.mobilis.xmpp.mxj.BeanProviderAdapter;

import de.tu_dresden.inf.mobilis.apps._9Cards.beans.ConfigureGameRequest;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.ConfigureGameResponse;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GetGameConfigurationRequest;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GetGameConfigurationResponse;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

import java.util.logging.Logger;

public abstract class AbstractIQListener implements PacketListener {

private final static Logger LOGGER = Logger.getLogger(AbstractIQListener.class.getCanonicalName());

	public AbstractIQListener() {
		this.registerBeanPrototypes();
	}
	
	public void registerBeanPrototypes() {
		(new BeanProviderAdapter(new ProxyBean(ConfigureGameRequest.NAMESPACE, ConfigureGameRequest.CHILD_ELEMENT))).addToProviderManager();
		(new BeanProviderAdapter(new ProxyBean(ConfigureGameResponse.NAMESPACE, ConfigureGameResponse.CHILD_ELEMENT))).addToProviderManager();
		(new BeanProviderAdapter(new ProxyBean(GetGameConfigurationRequest.NAMESPACE, GetGameConfigurationRequest.CHILD_ELEMENT))).addToProviderManager();
		(new BeanProviderAdapter(new ProxyBean(GetGameConfigurationResponse.NAMESPACE, GetGameConfigurationResponse.CHILD_ELEMENT))).addToProviderManager();
	}

	@Override
	public void processPacket(Packet packet) {
		if (packet instanceof BeanIQAdapter) {
			XMPPBean inBean = ((BeanIQAdapter) packet).getBean();

			LOGGER.info(inBean.toXML());

			if (inBean instanceof ProxyBean) {
				ProxyBean proxyBean = (ProxyBean) inBean;
					if (proxyBean.isTypeOf(ConfigureGameRequest.NAMESPACE,
						ConfigureGameRequest.CHILD_ELEMENT)) {
					_onConfigureGameRequest((ConfigureGameRequest) proxyBean
							.parsePayload(new ConfigureGameRequest()));
					} else if (proxyBean.isTypeOf(GetGameConfigurationRequest.NAMESPACE,
						GetGameConfigurationRequest.CHILD_ELEMENT)) {
					_onGetGameConfigurationRequest((GetGameConfigurationRequest) proxyBean
							.parsePayload(new GetGameConfigurationRequest()));
				} else {
					LOGGER.warning("No responsible type for received proxyBean!");
				}
			}
		}
	}

	private final void _onConfigureGameRequest(ConfigureGameRequest inBean) {
		ConfigureGameResponse out = new ConfigureGameResponse();
		out.setId(inBean.getId());
		out.setTo(inBean.getFrom());
		out.setFrom(inBean.getTo());
		out.setType(XMPPBean.TYPE_RESULT);
		this.sendIQBean(this.onConfigureGameRequest(inBean, out));
	}
	
	public abstract ConfigureGameResponse onConfigureGameRequest(ConfigureGameRequest inBean, ConfigureGameResponse outBean);
	
	private final void _onGetGameConfigurationRequest(GetGameConfigurationRequest inBean) {
		GetGameConfigurationResponse out = new GetGameConfigurationResponse();
		out.setId(inBean.getId());
		out.setTo(inBean.getFrom());
		out.setFrom(inBean.getTo());
		out.setType(XMPPBean.TYPE_RESULT);
		this.sendIQBean(this.onGetGameConfigurationRequest(inBean, out));
	}
	
	public abstract GetGameConfigurationResponse onGetGameConfigurationRequest(GetGameConfigurationRequest inBean, GetGameConfigurationResponse outBean);
	

	public abstract void sendIQBean(XMPPBean bean);

}