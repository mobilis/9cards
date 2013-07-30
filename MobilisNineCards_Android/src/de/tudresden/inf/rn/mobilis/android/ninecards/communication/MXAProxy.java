package de.tudresden.inf.rn.mobilis.android.ninecards.communication;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;

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
public class MXAProxy implements MXAListener {

	/** The application service running in the background. */
	private BackgroundService bgService;

	/** The XMPP service provided by MXA. */
	private IXMPPService iXMPPService;
	
	/** The class for sending/receiving IQs. */
	private IQProxy mIqProxy;
	
	/** The class for handling the multi user chat communication. */
	private MUCProxy mMucProxy;
	
	/** The XMPP connect handlers to notify when XMPP is connected. */
	private List<Handler> xmppConnectedHandlers;
	
	/** The XMPP disconnect handlers to notify when XMPP is disconnected. */
	private List<Handler> xmppDisconnectedHandlers;
	
	
	/**
	 * 
	 * @param appContext
	 */
	public MXAProxy(BackgroundService bgService) {
		this.bgService = bgService;
		
		xmppConnectedHandlers = new ArrayList<Handler>();
		xmppDisconnectedHandlers = new ArrayList<Handler>();
	}
	
	
	/**
	 * 
	 */
	public void connectMXA() {
		MXAController.get().connectMXA(bgService.getApplicationContext(), this);
	}
	

	/*
	 * (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.mxa.MXAListener#onMXAConnected()
	 */
	@Override
	public void onMXAConnected() {
		Log.v(this.getClass().getName(), "Connection to MXA Remote Service established.");

		// connect the MXA Remote Service to the XMPP Server
		iXMPPService = MXAController.get().getXMPPService();
		if(iXMPPService != null) {
			try {
				iXMPPService.connect(new Messenger(mConnectedToXMPPServerHandler));
			} catch (RemoteException e) {
				Log.e(this.getClass().getSimpleName(), "MXA Remote Service couldn't connect to XMPP Server");
			}
		}
	}
	
	
	/**
	 * 
	 */
	private Handler mConnectedToXMPPServerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if((iXMPPService != null) && (iXMPPService.isConnected())) {
					Log.v(this.getClass().getName(), "Connected to XMPP Server.");
					for(int i=0; i<xmppConnectedHandlers.size(); i++)
						xmppConnectedHandlers.get(i).sendEmptyMessage(0);

					mIqProxy = new IQProxy(bgService, iXMPPService);
					mMucProxy = new MUCProxy(bgService, iXMPPService);
				}
			} catch (RemoteException e) {
				Log.e(this.getClass().getSimpleName(), "Couldn't connect to XMPP Server!" + e.getMessage());
			}
		}
	};
	
	
	/**
	 * Disconnect XMPP service.
	 */
	public void disconnectFromXMPPServer() {
		if (iXMPPService != null) {
			try {
				iXMPPService.disconnect(new Messenger(mDisconnectedFromXMPPServerHandler));
			} catch (RemoteException e) {
				Log.e(this.getClass().getSimpleName(),
						"MXA Remote Service couldn't disconnected from XMPP Server");
			}
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see de.tudresden.inf.rn.mobilis.mxa.MXAListener#onMXADisconnected()
	 */
	@Override
	public void onMXADisconnected() {
		// nothing to do here
	}

		
	/**
	 * The XMPP result disconnection handler.
	 */
	private Handler mDisconnectedFromXMPPServerHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				if ((iXMPPService != null) && (!iXMPPService.isConnected())) {
					Log.v(this.getClass().getName(), "Disconnected from XMPP Server.");
					for(int i=0; i<xmppDisconnectedHandlers.size(); i++)
						xmppDisconnectedHandlers.get(i).sendEmptyMessage(0);
				}
			} catch (RemoteException e) {
				Log.e(this.getClass().getSimpleName(), "Couldn't disconnect from XMPP Server!" + e.getMessage());
			}
		}
	};

	
	/**
	 * 
	 * @return
	 */
	public boolean isConnectedToXMPPServer() {
		try {
			return iXMPPService.isConnected();
		} catch (Exception e) {
			return false;
		}
	}
	
	
	/**
	 * 
	 * @param handler
	 */
	public void addXMPPConnectedHandler(Handler handler) {
		this.xmppConnectedHandlers.add(handler);
	}
	
	
	/**
	 * 
	 * @param handler
	 */
	public void addXMPPDisconnectedHandler(Handler handler) {
		this.xmppDisconnectedHandlers.add(handler);
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IQProxy getIqProxy() {
		return mIqProxy;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public MUCProxy getMucProxy() {
		return mMucProxy;
	}
	
	
	/**
	 * 
	 * @return
	 */
	public IXMPPService getXMPPService() {
		return iXMPPService;
	}
}
