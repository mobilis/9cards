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
package de.tudresden.inf.rn.mobilis.services.ninecards;

import java.util.logging.Logger;

import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.services.ninecards.communication.IqConnection;
import de.tudresden.inf.rn.mobilis.services.ninecards.communication.MucConnection;

public class NineCardsService extends MobilisService {
	
	/** The raw XMPP connection wrapper for this service. */
	private IqConnection mIqConnection;
	/** The class for MUC management. */
	private MucConnection mMucConnection;
	
	/** The actual game instance. */
	private Game mGame;
	/** The Settings which contains game specific configuration. */
	private Settings mSettings;
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(NineCardsService.class.getCanonicalName());
	
	
	/* (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.server.services.MobilisService#startup(de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent)
	 */
	@Override
	public void startup(MobilisAgent agent) throws Exception  {
		super.startup(agent);
		
		try {
			mIqConnection = new IqConnection(this);
			mMucConnection = new MucConnection(this);
			LOGGER.info("Succesfully setup connections");
		} catch (Exception e) {
			LOGGER.severe("Failed to setup connections, shutting down! (" + e.getClass() + " - " + e.getMessage() + ")");
			this.shutdown();
		}
		
		mSettings = new Settings(getAgent());
		mGame = new Game(this);
	}

	@Override
	protected void registerPacketListener() {
		PacketTypeFilter mesFil = new PacketTypeFilter(Message.class);		
		getAgent().getConnection().addPacketListener(mMucConnection, mesFil);		
		
		PacketTypeFilter locFil = new PacketTypeFilter(IQ.class);		
		getAgent().getConnection().addPacketListener(mIqConnection, locFil);	
		
		LOGGER.info("PacketListeners successfully registered (IQListener and MessageListener");
	}
	
	@Override
	public void shutdown() {
		LOGGER.info(getAgent().getFullJid() + " is shutting down.");
		try {
			for(String playerJID : mGame.getPlayers().keySet())
				mGame.removePlayerByJid(playerJID);
			mMucConnection.closeMultiUserChat();
		} catch (Exception e) {
			LOGGER.warning("failed to close MUC (" + e.getClass() + " - " + e.getMessage() + ")");
		}
		
		try {
			super.shutdown();			
		} catch (Exception e) {
			LOGGER.warning("failed to shut down (" + e.getClass() + " - " + e.getMessage() + ")");
		}

	}
	
	/**
	 * Gets the IqConnection for this service. This is a wrapper for the raw XMPP connection.
	 * @return the iq connection
	 */
	public IqConnection getIqConnection() {
		return mIqConnection;
	}
	
	/**
	 * Gets the MucConnection for this service. This is a wrapper for the muc connection.
	 * @return the muc connection
	 */
	public MucConnection getMucConnection() {
		return mMucConnection;
	}
	
	/**
	 * Gets the actual game instance.
	 * @return the actual game instance
	 */
	public Game getGame() {
		return mGame;
	}
	
	/**
	 * Gets the Settings.
	 * @return the Settings
	 */
	public Settings getSettings() {
		return mSettings;
	}
}
