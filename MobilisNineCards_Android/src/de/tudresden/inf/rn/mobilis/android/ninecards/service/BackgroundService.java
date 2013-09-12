package de.tudresden.inf.rn.mobilis.android.ninecards.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.ServerConnection;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Game;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;

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
public class BackgroundService extends Service
{

	/** The connection to the XMPP Server. */
	private ServerConnection mServerConnection;
	/** The JID of the current Mobilis9Cards game service (running on server) */
	private String gameServiceJid;

	/** The Game instance. */
	private Game mGame;
	/** The state of the game. */
	private GameState gameState;
	
	/** Defines path to shared preferences for 9Cards. */
	public static final String SHARED_PREF_KEY_FILENAME = "de.tudresden.inf.rn.mobilis.android.ninecards_prefs";
	

	/** Is used if Mobilis Server supports 9Cards Service. */
	public static final int CODE_SERVICE_SUPPORTED = 0;
	/** Is used if Mobilis Server doesn't supports 9Cards Service. */
	public static final int CODE_SERVICE_NOT_AVAILABLE = 1;
	/** Is used if contacting the Mobilis Server fails. */
	public static final int CODE_SERVER_RESPONSE_ERROR = 2;

	/** Is used if there are games available. */
	public static final int CODE_GAMES_AVAILABLE = 3;
	/** Is used if there are no games available. */
	public static final int CODE_NO_GAMES_AVAILABLE = 4;
	/** Is used if contacting the Mobilis Server fails. */
	public static final int CODE_DISCOVER_GAMES_FAILURE = 5;
	
	/** The code used by mUpdateUIHandler to signalize that the start button shall be enabled. */
	public static final int CODE_ENABLE_START_GAME_BUTTON = 6;
	/** The code used by mUpdateUIHandler to signalize that theplayers list shall be updated. */
	public static final int CODE_UPDATE_GAME_PLAYERS_LIST = 7;
	

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate()
    {
    	super.onCreate();

		mServerConnection = new ServerConnection(this);		
		Log.v(this.getClass().getName(), this.getClass().getName() + " started");
    }
    
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return new LocalBinder();
	}
	
	
	/**
	 * 
	 */
	public class LocalBinder extends Binder
	{
		public BackgroundService getService()
		{
			return BackgroundService.this;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		mServerConnection.disconnectFromXmppServer();
		Log.v(this.getClass().getName(), this.getClass().getName() + " stopped");
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Gets the own XMPP JID.
	 * @return the own XMPP JID
	 */
	public String getUserJid()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_jid), null);
	}
	
	
	/**
	 * Gets the own XMPP password.
	 * @return the own XMPP password
	 */
	public String getUserPassword()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_password), null);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getUserNick()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_nick), null);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public String getXmppServerAddress()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_server_xmpp), null);
	}
	
    
    /**
     * 
     * @return
     */
    public String getMobilisServerJID()
    {
    	SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_server_mobilis_jid), null);
    }
    
    
    /**
     * 
     * @return
     */
    public String getGameServiceJid()
    {
    	return gameServiceJid;
    }
    
    
    /**
     * 
     * @param gameServiceJid
     */
    public void setGameServiceJid(String gameServiceJid)
    {
    	this.gameServiceJid = gameServiceJid;
    }

 // -------------------------------------------------------------------------------------------------------------------------------
	
    /**
     * Returns the connection to the XMPP server.
     * @return
     */
    public ServerConnection getServerConnection()
    {
    	return mServerConnection;
    }

    
	/**
	 * 
	 * @return
	 */
	public Game getGame()
	{
		return mGame;
	}
	
	
	/**
	 * 
	 */
	public void createGame(String name)
	{
		mGame = new Game(name);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public GameState getGameState()
	{
		return gameState;
	}
	
	
	/**
	 * 
	 * @param state
	 */
	public void setGameState(GameState state)
	{
		this.gameState = state;
	}
}
