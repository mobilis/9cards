package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
import de.tudresden.inf.rn.mobilis.mxa.activities.PreferencesClient;
import de.tudresden.inf.rn.mobilis.mxa.activities.Setup;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceInfo;

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
public class StartActivity extends Activity {
	
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	
	/** The MXAProxy. */
	private MXAProxy mMxaProxy;

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		MXAController.get().setSharedPreferencesName(this, "de.tudresden.inf.rn.mobilis.android.ninecards.mxa");
		
		initComponents();
		bindBackgroundService();
		
		try {
			// delete old MUC messages from internal database
			getContentResolver().delete(MessageItems.contentUri, null, null);
		} catch (Exception ignore) {}
	}
	
	
	/**
	 * 
	 */
	private void bindBackgroundService() {
		mBackgroundServiceConnector = new ServiceConnector();
		//getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
		mBackgroundServiceConnector.addHandlerToList(mBackgroundServiceBoundHandler);
		
		bindService(
				new Intent(this, BackgroundService.class),
				mBackgroundServiceConnector,
				Context.BIND_AUTO_CREATE );
	}
	
	
	/**
	 * 
	 */
	private void connectToXMPP() {
		if((mMxaProxy != null) && mMxaProxy.isConnectedToXMPPServer())
			requestOpenGames();
		
		else {
			Toast.makeText(this, "Setting up XMPP connection...", Toast.LENGTH_LONG).show();

			mMxaProxy.addXMPPConnectedHandler(mMxaConnectedHandler);
			try {
				mMxaProxy.connectMXA();
			} catch (Exception e) {
				Log.e(this.getClass().getName(), e.getMessage());
				Toast.makeText(this, "Failed to connect to MXA!\n(" + e.getMessage() + ")", Toast.LENGTH_LONG).show();
			}
		}
	}

	
	/**
	 * 
	 */
	private void initComponents() {
		
		// 'Start' button
		Button btn_Play = (Button) findViewById(R.id.act_start_btn_play);
		btn_Play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {		
				// check if user already configured XMPP
				if (!MXAController.get().checkSetupDone()) {
					Toast.makeText(StartActivity.this,
							StartActivity.this.getResources().getString(R.string.act_start_toast_xmppsetup_needed),
							Toast.LENGTH_SHORT).show();
					
					Intent quickSetupIntent = new Intent(StartActivity.this, Setup.class);
					startActivity(quickSetupIntent);
					
					return;
				}
				
				// connect to XMPP if not done yet
				if ((mMxaProxy != null) && (mMxaProxy.isConnectedToXMPPServer()))
					requestOpenGames();
				
				else connectToXMPP();
				
			}
		});
		
		// 'Settings' button
		Button btn_Settings = (Button) findViewById(R.id.act_start_btn_settings);
		btn_Settings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StartActivity.this, PreferencesClient.class);
				startActivity(intent);
			}
		});
		
		// 'Instructions' button
		Button btn_Instructions = (Button) findViewById(R.id.act_start_btn_instructions);
		btn_Instructions.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(StartActivity.this, InstructionsActivity.class);
				startActivity(intent);
			}
		});
		
		// 'Exit' button
		Button btn_Exit = (Button) findViewById(R.id.act_start_btn_exit);
		btn_Exit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	
    /**
     * Start the game. This will set the nickname of the player and send an empty
     * MobilisServiceDiscoveryBean to the Mobilis-Server.
     */
    private void requestOpenGames() {
		//mMxaProxy.setNickname(mBackgroundServiceConnector.getBackgroundService().getSharedPrefHelper()
		//		.getValue(getResources().getString(R.string.bundle_key_settings_username)));

		mMxaProxy.getIqProxy().sendServiceDiscoveryIQ(null);
    }
	
	
	/** The handler which is called if the XHuntService was bound. */
	private Handler mBackgroundServiceBoundHandler = new Handler() {
		@Override
		public void handleMessage(Message messg) {
			mMxaProxy = mBackgroundServiceConnector.getBackgroundService().getMXAProxy();

			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateStart());
		}
	};
	
	
    /** The handler which is called when the XMPP connection was established successfully. */
    private Handler mMxaConnectedHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(StartActivity.this, "XMPP connection established", Toast.LENGTH_SHORT).show();	
			mMxaProxy.getIqProxy().registerCallbacks();
			
			requestOpenGames();
		}
	};
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// if the back-button of the device was pressed, finish the application 
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    	this.finish();
	    
	    return super.onKeyDown(keyCode, event);	    
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateStart());
		
		super.onResume();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		// if the background service is up, unregister all IQ-Listeners and stop the background service
		if((mBackgroundServiceConnector != null)
				&& (mBackgroundServiceConnector.getBackgroundService() != null)
				&& (mMxaProxy != null)
				&& (mMxaProxy.getIqProxy() != null)) {
			mMxaProxy.getIqProxy().unregisterCallbacks();
			
			mBackgroundServiceConnector.getBackgroundService().stopSelf();
			unbindService(mBackgroundServiceConnector);
		}
		
		try {
			// delete old MUC messages from internal database
			getContentResolver().delete(MessageItems.contentUri, null, null);
		} catch (Exception ignore) {}
		
		super.finish();
	}
	
	
	private class GameStateStart extends GameState {

		@Override
		public void processPacket(XMPPBean inBean) {	
			
			if(inBean.getType() == XMPPBean.TYPE_ERROR)
				Log.e(this.getClass().getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
		

			if(inBean instanceof MobilisServiceDiscoveryBean) {		
				MobilisServiceDiscoveryBean bean = (MobilisServiceDiscoveryBean) inBean;
				
				// check if NineCards is supported
				if((bean != null) && (bean.getType() != XMPPBean.TYPE_ERROR)) {
					if((bean.getDiscoveredServices() != null) && (bean.getDiscoveredServices().size() > 0)) {
						
						boolean serverSupportsNineCards = false;
						for(MobilisServiceInfo info : bean.getDiscoveredServices()){
							if(info.getServiceNamespace().toLowerCase().contains("ninecards")){
								serverSupportsNineCards = true;
								break;
							}								
						}
					
						// if it's supported, go to open games view
						if(serverSupportsNineCards) {
							Intent intent = new Intent(StartActivity.this, OpenGamesActivity.class);
							StartActivity.this.startActivity(intent);
						}
						
						// else notify user
						else 
							Toast.makeText(StartActivity.this, "No 9Cards Service installed on Server", Toast.LENGTH_SHORT).show();
					}
				}
				
				// If Mobilis Server doesn't respond, notify user
				else if(bean.getType() == XMPPBean.TYPE_ERROR)
					Toast.makeText(StartActivity.this, "Server not found. Please check your settings!", Toast.LENGTH_SHORT).show();			
			}
			
			// Other Beans of type get or set will be responded with an ERROR
			else if(inBean.getType() == XMPPBean.TYPE_GET || inBean.getType() == XMPPBean.TYPE_SET) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(main)";
				
				mMxaProxy.getIqProxy().sendXMPPBeanError(inBean);
			}
		}
		
	}
	
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}*/
}
