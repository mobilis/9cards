package de.tudresden.inf.rn.mobilis.android.ninecards.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Game;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;

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
	public void processMucMessage(String sender, String body) {
		//TODO
	}
	
	
	public void processIq(XMPPBean inBean) {
		//TODO
	}
	
	
	// =====================================================================================
	// Getter/Setter and other simple methods
	// -------------------------------------------------------------------------------------
		/**
	 * 
	 */
	public void createGame() {
		mGame = new Game();
	}
	
	
	/**
	 * 
	 * @return
	 */
	public Game getCurrentGame() {
		return mGame != null ? mGame : new Game();
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
    
    
    public String getCoordinatorServiceJID() {
    	if(coordinatorServiceJid == null) {
    		Log.w(this.getClass().getName(), "CoordinatorServiceJid was not set!");
    		return "";
    	}
    	return coordinatorServiceJid;
    }
    
    
    public String getMucRoomId() {
    	return mucRoomID;
    }
    
    
    public String getMucRoomPw() {
    	return mucRoomPw;
    }
}
