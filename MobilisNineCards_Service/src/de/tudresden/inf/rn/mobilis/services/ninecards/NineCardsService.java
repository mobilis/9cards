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

import java.util.List;
import java.util.logging.Logger;

import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.PacketExtension;

import de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent;
import de.tudresden.inf.rn.mobilis.server.services.MobilisService;
import de.tudresden.inf.rn.mobilis.services.ninecards.communication.IqConnection;
import de.tudresden.inf.rn.mobilis.services.ninecards.communication.MucConnection;

/**
 * This class extends MobilisService and manages the whole game service lifecycle.
 * 
 * @author Matthias Köngeter, Markus Wutzler
 *
 */
public class NineCardsService extends MobilisService
{
	
	/** The class for managing IQ communication. */
	private IqConnection mIqConnection;
	/** The class for multiuser and private chat communication. */
	private MucConnection mMucConnection;
	
	/** The game instance. */
	private Game mGame;
	/** The settings object which contains game specific configuration. */
	private Settings mSettings;
	
	/** The class specific Logger object. */
	private final static Logger LOGGER = Logger.getLogger(NineCardsService.class.getCanonicalName());
	

	/*
	 * (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.server.services.MobilisService#startup(de.tudresden.inf.rn.mobilis.server.agents.MobilisAgent)
	 */
	@Override
	public void startup(MobilisAgent agent) throws Exception
	{
		mSettings = new Settings(this.getAgent());
		mGame = new Game(this);
		
		try {
			mIqConnection = new IqConnection(this);
			mMucConnection = new MucConnection(this);
			mMucConnection.createMultiUserChat();
			LOGGER.info("Succesfully setup connections");
		} catch (Exception e) {
			LOGGER.severe("Failed to setup connections, shutting down! (" + e.getClass() + " - " + e.getMessage() + ")");
			this.shutdown();
		}
		
		super.startup(agent);
	}
	

	/*
	 * (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.server.services.MobilisService#registerPacketListener()
	 */
	@Override
	protected void registerPacketListener()
	{
		// set packet filters, ignore packets of type presence
		PacketTypeFilter iqFilter = new PacketTypeFilter(IQ.class);		
		getAgent().getConnection().addPacketListener(mIqConnection, iqFilter);
		
		PacketTypeFilter mesgFilter = new PacketTypeFilter(Message.class);		
		getAgent().getConnection().addPacketListener(mMucConnection, mesgFilter);		
		
		LOGGER.info("PacketListeners successfully registered (IQListener and MessageListener");
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.server.services.MobilisService#shutdown()
	 */
	@Override
	public void shutdown()
	{
		LOGGER.info(getAgent().getFullJid() + " is shutting down.");
		//
		// if (mGame != null)
		// for (String playerJID : mGame.getPlayers().keySet())
		// mGame.removePlayer(playerJID, "shutting down ninecards service");
		
		if (mMucConnection != null) {
			mMucConnection
					.closeMultiUserChat("shutting down ninecards service");
			mMucConnection = null;
		}

		try {
			super.shutdown();			
		} catch (Exception e) {
			LOGGER.warning("failed to shut down service (" + e.getClass().getSimpleName() + " - " + e.getMessage() + ")");
		}
	}
	
	
	/**
	 * Returns the IqConnection object.
	 * 
	 * @return the IQ communication wrapper
	 */
	public IqConnection getIqConnection()
	{
		return mIqConnection;
	}
	
	
	/**
	 * Returns the MucConnection object.
	 * 
	 * @return the MUC communication wrapper
	 */
	public MucConnection getMucConnection()
	{
		return mMucConnection;
	}
	
	
	/**
	 * Returns the game instance.
	 * 
	 * @return the game instance
	 */
	public Game getGame()
	{
		return mGame;
	}
	
	
	/**
	 * Returns the Settings object.
	 * 
	 * @return the Settings object
	 */
	public Settings getSettings()
	{
		return mSettings;
	}


	@Override
	public List<PacketExtension> getNodePacketExtensions() {
		// TODO Auto-generated method stub
		return null;
	}
}
