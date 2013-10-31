/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: https://github.com/mobilis
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.services.ninecards.communication;

import de.tudresden.inf.rn.mobilis.services.ninecards.Game.State;
import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.GetGameConfigurationRequest;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

/**
 * This class is responsible for processing incoming packets of type 'IQ'.
 * 
 * @author Matthias Köngeter
 *
 */
public class IqPacketProcessor
{
	
	/**	The ninecards game service instance. */
	private NineCardsService mServiceInstance;
	
	
	/**
	 * The constructor for creating a new IqPacketProcessor instance.
	 * 
	 * @param serviceInstance the ninecards game service instance
	 */
	public IqPacketProcessor(NineCardsService serviceInstance)
	{
		this.mServiceInstance = serviceInstance;
	}
	
	
	/**
	 * This method analyzes the type of a message and then parses it into a ConfigureGameRequest
	 * before it is passed to the corresponding method. If the type is not 'ConfigureGameRequest',
	 * an error response message will be sent.
	 * 
	 * @param inBean the bean to be parsed
	 */
	public void processPacket(XMPPBean inBean)
	{
		if (inBean instanceof ConfigureGameRequest) {
			onConfigureGame((ConfigureGameRequest) inBean);
		} else if (inBean instanceof GetGameConfigurationRequest) {
			onGetGameConfigration((GetGameConfigurationRequest) inBean);
		} else {
			inBean.errorType = "wait";
			inBean.errorCondition = "unexpected-request";
			inBean.errorText = "This request is not supported!";
			
			mServiceInstance.getIqConnection().sendXMPPBeanError(inBean, inBean);			
		}
	}
	
	
	private void onGetGameConfigration(GetGameConfigurationRequest inBean) {
		if (mServiceInstance.getGame().getGameState() == State.READY) {

			mServiceInstance
					.getIqConnection()
					.getProxy()
					.GetGameConfiguration(inBean.getFrom(), inBean.getId(),
							mServiceInstance.getSettings().getChatID(),
							mServiceInstance.getSettings().getRounds(),
							mServiceInstance.getSettings().getMaxPlayers());
		} else {
			XMPPBean out = inBean
					.buildGameConfigFault("Not allowed in this state.");
			mServiceInstance.getIqConnection().getProxy().getBindingStub()
					.sendXMPPBean(out);
		}
	}

	/**
	 * This method checks whether the game has already been initialized, and if that's not the case,
	 * settings are configured and the multiuser chat is created.
	 * 
	 * @param inBean the ConfigureGameRequest to be processed
	 */
	private void onConfigureGame(ConfigureGameRequest inBean)
	{
		if (mServiceInstance.getGame().getGameState() == State.UNINITIALIZED) {
			
			mServiceInstance.getSettings().setRounds(inBean.getRounds());
			mServiceInstance.getSettings().setMaxPlayers(inBean.getPlayers());

			mServiceInstance.getGame().setGameState(State.READY);

			mServiceInstance
					.getIqConnection()
					.getProxy()
					.ConfigureGame(inBean.getFrom(), inBean.getId(),
							mServiceInstance.getSettings().getChatID());

			// TODO: set MUC admin to game creator and not to first joiner.
		}

		else {
			XMPPBean out = inBean.buildInputDataFault("Not allowed in this state.");
			mServiceInstance.getIqConnection().getProxy().getBindingStub().sendXMPPBean(out);
		}
	}
}
