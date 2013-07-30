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

import de.tudresden.inf.rn.mobilis.services.ninecards.Game;
import de.tudresden.inf.rn.mobilis.services.ninecards.Game.State;
import de.tudresden.inf.rn.mobilis.services.ninecards.NineCardsService;
import de.tudresden.inf.rn.mobilis.services.ninecards.Player;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.ConfigureGameRequest;
import de.tudresden.inf.rn.mobilis.services.ninecards.proxy.JoinGameRequest;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

public class IqPacketProcessor {
	
	/**	The 9Cards service instance. */
	private NineCardsService mServiceInstance;
	
	
	/**
	 * 
	 * @param connection
	 */
	public IqPacketProcessor(NineCardsService serviceInstance) {
		this.mServiceInstance = serviceInstance;
	}
	
	
	/**
	 * 
	 * @param inBean
	 */
	public void processPacket(XMPPBean inBean) {
		
		if(inBean instanceof ConfigureGameRequest)
			onConfigureGame((ConfigureGameRequest) inBean);

		else if(inBean instanceof JoinGameRequest)
			onJoinGame((JoinGameRequest) inBean);
		
		else {
			inBean.errorType = "wait";
			inBean.errorCondition = "unexpected-request";
			inBean.errorText = "This request is not supportet!";
			
			mServiceInstance.getIqConnection().sendXMPPBeanError(inBean, inBean);			
		}
	}
	
	
	/**
	 * 
	 * @param inBean
	 */
	private void onJoinGame(JoinGameRequest inBean) {
		Game.State state = mServiceInstance.getGame().getGameState();
		
		switch(state) {
			case UNINITIALIZED :
				XMPPBean out_u = inBean.buildClosedGameFault( "This GameService is not yet configured properly." );
				mServiceInstance.getIqConnection().getProxy().getBindingStub().sendXMPPBean(out_u);
				break;
				
			case LOBBY :
				if(mServiceInstance.getGame().isGameOpen()) {
					// add player if he's not already joined
					if(!mServiceInstance.getGame().getPlayers().keySet().contains(inBean.getFrom())) {
						// if he's the first one, he is the creator and can start the game
						if(mServiceInstance.getGame().getPlayers().values().size() == 1)
							mServiceInstance.getGame().addPlayer(new Player(inBean.getFrom(), "", true));
						else mServiceInstance.getGame().addPlayer(new Player(inBean.getFrom(), "", false));
						// confirm the joining of the game and send the chat information to the client
						mServiceInstance.getIqConnection().getProxy().JoinGame(
								inBean.getFrom(),
								inBean.getId(),
								mServiceInstance.getSettings().getChatID(),
								mServiceInstance.getSettings().getChatPW(),
								mServiceInstance.getGame().getCreator());
						// close game if max. number of participants is reached
						if(mServiceInstance.getGame().getPlayers().size() == mServiceInstance.getSettings().getMaxPlayers())
							mServiceInstance.getGame().setGameOpen(false);
					}
				}
				// if game was already closed, send an error message
				mServiceInstance.getIqConnection().getProxy().getBindingStub().sendXMPPBean(
						inBean.buildClosedGameFault("Maximum of players reached."));
				break;
				
			case PLAY :
				XMPPBean out_p = inBean.buildClosedGameFault( "The game is already running." );
				mServiceInstance.getIqConnection().getProxy().getBindingStub().sendXMPPBean(out_p);
				break;
		}
	}
	
	
	/**
	 * 
	 * @param inBean
	 */
	private void onConfigureGame(ConfigureGameRequest inBean) {
		Game.State state = mServiceInstance.getGame().getGameState();
		
		switch(state) {
			case UNINITIALIZED :
				mServiceInstance.getSettings().setGameName(inBean.getGameName());
				mServiceInstance.getSettings().setRounds(inBean.getNumberOfRounds());
				mServiceInstance.getSettings().setMaxPlayers(inBean.getMaxPlayers());
				mServiceInstance.getGame().setCreator(inBean.getFrom());
				
				mServiceInstance.getGame().setGameOpen(true);
				mServiceInstance.getGame().setGameState(State.LOBBY);
				
				mServiceInstance.getIqConnection().getProxy().ConfigureGame(inBean.getFrom(), inBean.getId());
				break;
				
			default :
				XMPPBean out = inBean.buildInputDataFault("Not allowed in this state.");
				mServiceInstance.getIqConnection().getProxy().getBindingStub().sendXMPPBean(out);
				break;
		}
	}
}
