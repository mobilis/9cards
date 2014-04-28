package de.tu_dresden.inf.rn.mobilis.services.ninecards.communication;

import org.jivesoftware.smack.util.StringUtils;

import de.tu_dresden.inf.mobilis.apps._9Cards.beans.ConfigureGameRequest;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.ConfigureGameResponse;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GetGameConfigurationRequest;
import de.tu_dresden.inf.mobilis.apps._9Cards.beans.GetGameConfigurationResponse;
import de.tu_dresden.inf.mobilis.apps._9Cards.service.AbstractIQListener;
import de.tu_dresden.inf.rn.mobilis.services.ninecards.Game;
import de.tu_dresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.mxj.BeanIQAdapter;

public class IQListener extends AbstractIQListener {
	
	private final NineCardsService service; 
	
	public IQListener(NineCardsService service) {
		super();
		this.service = service;
	}

	@Override
	public GetGameConfigurationResponse onGetGameConfigurationRequest(GetGameConfigurationRequest inBean,
			GetGameConfigurationResponse outBean) {
		if (service.getGame().getGameState() == Game.State.READY) {
			outBean.setMaxPlayers(service.getSettings().getMaxPlayers());
			outBean.setMaxRounds(service.getSettings().getRounds());
			outBean.setMuc(service.getSettings().getChatID());
		} else {
			outBean.setType(XMPPBean.TYPE_ERROR);
            outBean.errorType = XMPPBean.ERROR_TYPE_CANCEL;
            outBean.errorCondition = XMPPBean.ERROR_CONDITION_BAD_REQUEST;
            outBean.errorText = "Operation not allowed in this state.";
		}
		return outBean;
	}

	@Override
	public ConfigureGameResponse onConfigureGameRequest(ConfigureGameRequest inBean, ConfigureGameResponse outBean) {
		if (service.getGame().getGameState() == Game.State.UNINITIALIZED) {
			
			service.getSettings().setRounds(inBean.getRounds());
			service.getSettings().setMaxPlayers(inBean.getPlayers());

			service.getGame().setGameState(Game.State.READY);

			outBean.setMuc(service.getSettings().getChatID());
			service.getSettings().setAdminBareJID(StringUtils.parseBareAddress(inBean.getFrom()));
		} else {
			outBean.setType(XMPPBean.TYPE_ERROR);
            outBean.errorType = XMPPBean.ERROR_TYPE_CANCEL;
            outBean.errorCondition = XMPPBean.ERROR_CONDITION_BAD_REQUEST;
            outBean.errorText = "Operation not allowed in this state.";
		}
		return outBean;
	}

	@Override
	public void sendIQBean(XMPPBean bean) {
		service.getAgent().getConnection().sendPacket(new BeanIQAdapter(bean));

	}

}
