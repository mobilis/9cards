/*******************************************************************************
 * Copyright (C) 2013 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science Computer
 * Networks Group: http://www.rn.inf.tu-dresden.de mobilis project:
 * https://github.com/mobilis
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.android.ninecards.game;

import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;

/**
 * The Class GameState is an abstract class to handle different specific states of the game
 * and process messages in dependency of the current state.
 * 
 * @author Matthias Köngeter
 *
 */
public abstract class GameState {

	/**
	 * Process an XMPPBean forwarded from 
	 * @see de.tudresden.inf.rn.mobilis.android.xhunt.proxy.IQProxy#AbstractCallback.
	 *
	 * @param xmppBean the bean which shall be processed
	 */
	public abstract void processPacket(XMPPBean xmppBean);
	
	/**
	 * Process an XMPPInfo which was received via multiuser or private chat.
	 * 
	 * @param xmppInfo the xmppInfo which shall be processed
	 */
	public abstract void processChatMessage(XMPPInfo xmppInfo);
}
