package de.tudresden.inf.rn.mobilis.android.ninecards.service;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

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
public class ServiceConnector implements ServiceConnection {
	
	/** The application's android service running in the background. */
	private BackgroundService mBackgroundService;
	
	/** The handlers bound to the background service. */
	private List<Handler> mServiceBoundHandlers;
	
	
	/**
	 * 
	 */
	public ServiceConnector() {
		mServiceBoundHandlers = new ArrayList<Handler>();
	}
	

	/*
	 * (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		mBackgroundService = ((BackgroundService.LocalBinder)service).getService();
        Log.v(this.getClass().getName(), "BackgroundService bound");
        
        // notify all registered handlers
        for(int i=0; i<mServiceBoundHandlers.size(); i++){
        	mServiceBoundHandlers.get(i).sendEmptyMessage(0);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
	 */
	@Override
    public void onServiceDisconnected(ComponentName className) {
        mBackgroundService = null;
        Log.v(this.getClass().getName(), "BackgroundService unbound");
    }

	
	/**
	 * 
	 * @param handler
	 */
	public void addHandlerToList(Handler handler) {
		mServiceBoundHandlers.add(handler);
	}
	
	
    /**
     * Gets the BackgroundService.
     * @return the BackgroundService
     */
    public BackgroundService getBackgroundService(){
    	return mBackgroundService;
    }
}
