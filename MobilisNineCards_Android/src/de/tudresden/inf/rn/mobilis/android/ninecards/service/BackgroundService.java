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
package de.tudresden.inf.rn.mobilis.android.ninecards.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Game;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.ServerConnection;

/**
 * The application service running in the background.
 * 
 * @author Matthias Köngeter
 *
 */
public class BackgroundService extends Service
{

	/** The connection to the XMPP Server. */
	private ServerConnection mServerConnection;
	/** The JID of the current NineCards game service instance */
	private String mGameServiceJid;

	/** The game instance. */
	private Game mGame;
	/** The current state of the game. */
	private GameState mGameState;
	
	/** Defines the path to the shared preferences. */
	public static final String SHARED_PREF_KEY_FILENAME = "de.tudresden.inf.rn.mobilis.android.ninecards_prefs";

	/** Is used if Mobilis Server supports 9Cards Service. */
	public static final int CODE_SERVICE_SUPPORTED = 0;
	/** Is used if Mobilis Server doesn't supports NineCards Service. */
	public static final int CODE_SERVICE_NOT_AVAILABLE = 1;
	/** Is used if contacting the Mobilis Server fails. */
	public static final int CODE_SERVER_RESPONSE_ERROR = 2;

	/** Is used if there are games available. */
	public static final int CODE_GAMES_AVAILABLE = 3;
	/** Is used if there are no games available. */
	public static final int CODE_NO_GAMES_AVAILABLE = 4;
	/** Is used if contacting the Mobilis Server fails. */
	public static final int CODE_DISCOVER_GAMES_FAILURE = 5;
	
	/** The code used by the UpdateUIHandler to signalize that the start button shall be enabled. */
	public static final int CODE_ENABLE_START_GAME_BUTTON = 6;
	/** The code used for disabling the start button, canceling the waiting dialogue and enable all cards. */
	public static final int CODE_START_GAME = 7;
	/** The code used by the UpdateUIHandler to signalize that the players list shall be updated. */
	public static final int CODE_UPDATE_GAME_PLAYERS_LIST = 8;
	/** The code used for re-enabling the cards and updating the current round. */
	public static final int CODE_START_NEW_ROUND = 9;
	/** The code used for creating and displaying the game over dialog. */
	public static final int CODE_SHOW_GAMEOVER_DIALOG = 10;
	

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate()
    {
    	super.onCreate();

		mServerConnection = new ServerConnection(this);		
		Log.i(this.getClass().getName(), this.getClass().getName() + " started");
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
	 * Class used by ServiceConnector to get the instance of this BackgroundService.
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
		Log.i(this.getClass().getName(), this.getClass().getName() + " stopped");
	}
	
// -------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Returns the own user's XMPP JID which he entered in the Settings view.
	 * 
	 * @return the own user's XMPP JID
	 */
	public String getUserJID()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_jid), null);
	}
	
	
	/**
	 * Returns the own user's password which he entered in the Settings view.
	 * 
	 * @return the own user's XMPP password
	 */
	public String getUserPassword()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_password), null);
	}
	
	
	/**
	 * Returns the own user's nickname which he entered in the Settings view.
	 * 
	 * @return the own user's nickname
	 */
	public String getUserNick()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_user_nick), null);
	}
	
	
	/**
	 * Returns the address of the XMPP server which was entered in the Settings view.
	 * 
	 * @return the address of the XMPP server
	 */
	public String getXmppServerAddress()
	{
		SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_server_xmpp), null);
	}
	
    
    /**
     * Returns the JID of the Mobilis Server which was entered in the Settings view.
     * 
     * @return the JID of the Mobilis Server
     */
    public String getMobilisServerJID()
    {
    	SharedPreferences prefs = getSharedPreferences("de.tudresden.inf.rn.mobilis.android.ninecards_preferences", MODE_PRIVATE);
		return prefs.getString(getResources().getString(R.string.edit_text_pref_server_mobilis_jid), null);
    }
    
    
    /**
     * Returns the JID of the game service instance.
     * 
     * @return the JID of the game instance
     */
    public String getGameServiceJID()
    {
    	return mGameServiceJid;
    }
    
    
    /**
     * Sets the JID of the game service instance.
     * 
     * @param gameServiceJid the JID of the game instance
     */
    public void setGameServiceJID(String gameServiceJid)
    {
    	this.mGameServiceJid = gameServiceJid;
    }

 // -------------------------------------------------------------------------------------------------------------------------------
	
    /**
     * Returns the instance of ServerConnection class.
     * 
     * @return the instance of ServerConnection class
     */
    public ServerConnection getServerConnection()
    {
    	return mServerConnection;
    }

    
	/**
	 * Returns the instance of Game class.
	 * 
	 * @return the current game object
	 */
	public Game getGame()
	{
		return mGame;
	}
	
	
	/**
	 * Instantiates a new game object.
	 * 
	 * @param name the name of the new game
	 */
	public void createGame(String name)
	{
		mGame = new Game(name);
	}
	
	
	/**
	 * Returns the current GameState object which is needed to process messages.
	 * 
	 * @return the current GameState object
	 */
	public GameState getGameState()
	{
		return mGameState;
	}
	
	
	/**
	 * Sets the GameState which is needed to process messages.
	 * 
	 * @param state the new state of the game
	 */
	public void setGameState(GameState state)
	{
		this.mGameState = state;
	}
}
