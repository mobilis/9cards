package de.tudresden.inf.rn.mobilis.android.ninecards.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.activity.PlayActivity.GameStatePlay;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Game;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPInfo;

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
public class BackgroundService extends Service {

	/** The MXAProxy instance. */
	private MXAProxy mMxaProxy;

	/** The Game instance. */
	private Game mGame;
	
	/** The JID of the current Mobilis9Cards game service (running on server) */
	private String gameServiceJid;
	
	/** The JID of the Coordinator Service of the Mobilis Server. */
	private String coordinatorServiceJid;
	
	/** The ID of the chat room. */
	private String mucRoomID;

	/** The password of the chat room. */
	private String mucRoomPw;
	
	/** The state of the game. */
	private GameState gameState;
	
	/** Whether the player is the one who created the game. */
	private boolean isCreator;
	
	private int serviceVersion;
	

	// =====================================================================================
	// Service specific methods
	// -------------------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
    	super.onCreate();

		mMxaProxy = new MXAProxy(this);
	
		Log.v(this.getClass().getName(), this.getClass().getName() + " started");
    }
    
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return new LocalBinder();
	}
	
	
	/**
	 * 
	 */
	public class LocalBinder extends Binder {
		public BackgroundService getService() {
			return BackgroundService.this;
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		mMxaProxy.disconnectFromXMPPServer();
		Log.v(this.getClass().getName(), this.getClass().getName() + " stopped");
	}
	
	
	// =====================================================================================
	// Message processing methods
	// -------------------------------------------------------------------------------------
	/**
	 * 
	 * @param inBean
	 */
	public void processIq(XMPPBean inBean) {
		Log.i(this.getClass().getSimpleName(), "Incoming IQ from " + inBean.getFrom() + ": " + inBean.toXML());
		
		if(gameState != null)
			gameState.processPacket(inBean);
		
		else Log.e(this.getClass().getSimpleName(), "Couldn't pass IQ to GameState because GameState was null!");
	}
	
	
	/**
	 * 
	 * @param message
	 */
	public void processMucMessage(XMPPInfo xmppInfo) {
		
		if(!(gameState instanceof GameStatePlay))
			Log.e(this.getClass().getSimpleName(), "MUC Messages only allowed in GameStatePlay!");
		
		else
			((GameStatePlay) gameState).processMucMessage(xmppInfo);
	}
	
	
	// =====================================================================================
	// Getter/Setter and other simple methods
	// -------------------------------------------------------------------------------------
		/**
	 * 
	 */
	public void createGame(String name) {
		mGame = new Game(name);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Game getGame() {
		return mGame;
	}
	
	
	public void setGameState(GameState state) {
		this.gameState = state;
	}
	
	
	public void setServiceVersion(int version) {
		this.serviceVersion = version;
	}
	
	
    /**
     * Gets the MXAProxy.
     * @return the MXAProxy
     */
    public MXAProxy getMXAProxy(){
    	return mMxaProxy;
    }
    
    
	/**
	 * Gets the own XMPP JID.
	 * @return the own XMPP JID
	 */
	public String getUserJid() {
		try {
			if (mMxaProxy.isConnectedToXMPPServer())
				return mMxaProxy.getXMPPService().getUsername();
			else
				return "";
				// TODO hier auch eine gespeicherte JID aus den Shared Preferences holen
		} catch (RemoteException e) {
			return "";
		}

	}
    
    
    /**
     * 
     * @return
     */
    public String getGameServiceJid() {
    	if(gameServiceJid == null) {
    		Log.w(this.getClass().getName(), "GameServiceJid was not set!");
    		return "";
    	}
    	return gameServiceJid;
    }
    
    
    /**
     * 
     * @param gameServiceJid
     */
    public void setGameServiceJid(String gameServiceJid) {
    	this.gameServiceJid = gameServiceJid;
    }
    
    
    public String getCoordinatorServiceJID() {
    	if(coordinatorServiceJid == null) {
    		String node = "mobilis";
    		String domain = getUserJid().substring(getUserJid().indexOf("@"), getUserJid().indexOf("/"));
    		String ressource = "/Coordinator";
    		
    		Log.w(this.getClass().getName(),
    				"CoordinatorServiceJid was not set! Using generated JID " + node + domain + ressource);
    		
    		return node + domain + ressource;
    	}
    	
    	return coordinatorServiceJid;
    }
    
    
    public void setMucRoomId(String mucRoomId) {
    	this.mucRoomID = mucRoomId;
    }
    
    
    public String getMucRoomId() {
    	return mucRoomID;
    }
    
    
    public void setMucRoomPw(String mucRoomPw) {
    	this.mucRoomPw = mucRoomPw;
    }
    
    
    public String getMucRoomPw() {
    	return mucRoomPw;
    }
    
	/**
	 * Returns true if the player is the one who created the game, else if not.
	 * @return
	 */
	public boolean isCreator() {
		return isCreator;
	}
}
