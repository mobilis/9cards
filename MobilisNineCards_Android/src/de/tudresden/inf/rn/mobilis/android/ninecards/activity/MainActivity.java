package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import org.jivesoftware.smack.SmackAndroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.MobilisServiceInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.ServerConnection;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;

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
public class MainActivity extends Activity
{
	
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The connection to the XMPP server. */
	private ServerConnection serverConnection;

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		PreferenceManager.setDefaultValues(this, R.xml.layout_settings, false);
		
		bindBackgroundService();
		initComponents();
		
		// the following line is needed by aSmack (see https://github.com/Flowdalic/asmack/blob/master/README.asmack)
		SmackAndroid.init(this);
	}


	/**
	 * 
	 */
	private void bindBackgroundService()
	{
		mBackgroundServiceConnector = new ServiceConnector();
		getApplicationContext().startService(new Intent(getApplicationContext(), BackgroundService.class));
		
		mBackgroundServiceConnector.addHandlerToList(mBackgroundServiceBoundHandler);
		bindService(
				new Intent(this, BackgroundService.class),
				mBackgroundServiceConnector,
				Context.BIND_AUTO_CREATE );
	}
	
	
	/** The handler which is called if the XHuntService was bound. */
	private Handler mBackgroundServiceBoundHandler = new Handler()
	{
		@Override
		public void handleMessage(Message messg) {
			serverConnection = mBackgroundServiceConnector.getBackgroundService().getServerConnection();
			serverConnection.registerXmppExtensions();
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateStart());
		}
	};

	
	/**
	 * 
	 */
	private void initComponents()
	{
		// 'Start' button
		Button btn_Play = (Button) findViewById(R.id.act_start_btn_play);
		btn_Play.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// connect to XMPP server if not done yet
				if (!serverConnection.isConnected()) {
					String server = mBackgroundServiceConnector.getBackgroundService().getXmppServerAddress();
					String userJid = mBackgroundServiceConnector.getBackgroundService().getUserJid();
					String userPw = mBackgroundServiceConnector.getBackgroundService().getUserPassword();
					
					// if it fails, notify user
					if(!serverConnection.connectToXmppServer(server, userJid, userPw)) {
						Toast.makeText(MainActivity.this,
								"Failed to connect to XMPP server.\nPlease check your settings!",
								Toast.LENGTH_LONG).show();
						
						return;
					}
				}
				
				// if successfully connected, request open games
				serverConnection.sendServiceDiscovery(null);
			}
		});
		
		// 'Settings' button
		Button btn_Settings = (Button) findViewById(R.id.act_start_btn_settings);
		btn_Settings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		// 'Instructions' button
		Button btn_Instructions = (Button) findViewById(R.id.act_start_btn_instructions);
		btn_Instructions.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, InstructionsActivity.class);
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
	
	
    /** Handler to handle MobilisServiceDiscoveryBeans. */
    private Handler mServiceDiscoveryResultHandler = new Handler()
    {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case BackgroundService.CODE_SERVICE_SUPPORTED : {
					startActivity(new Intent(MainActivity.this, OpenGamesActivity.class));
					break;
				}
				case BackgroundService.CODE_SERVICE_NOT_AVAILABLE : {
					Toast.makeText(MainActivity.this.getApplicationContext(), "No 9Cards Service installed on Server", Toast.LENGTH_SHORT).show();
					break;
				}
				case BackgroundService.CODE_SERVER_RESPONSE_ERROR : {
					Toast.makeText(MainActivity.this, "Server not found. Please check your settings!", Toast.LENGTH_SHORT).show();
					break;
				}
				default :
					Log.w(getClass().getSimpleName(), "Unexpected handler event code (" + msg.what + ")");
			}
		}
	};
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		// if the back-button of the device was pressed, finish the application 
	    if ((keyCode == KeyEvent.KEYCODE_BACK))
	    	this.finish();
	    
	    return super.onKeyDown(keyCode, event);	    
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateStart());
		
		super.onResume();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish()
	{	
		// destroy background service
		if ((mBackgroundServiceConnector != null) && (mBackgroundServiceConnector.getBackgroundService() != null)) {
			mBackgroundServiceConnector.getBackgroundService().stopSelf();
			unbindService(mBackgroundServiceConnector);
		}
		
		// we need to call onDestroy() on SmackAndroid to unregister ConnectivityChangedReceiver.
		// SmackAndroid uses the Singleton pattern, so we can just call init() to get an instance. 
		SmackAndroid asmack = SmackAndroid.init(this);
		asmack.onDestroy();
		
		super.finish();
	}
	
	
	/**
	 * 
	 */
	private class GameStateStart extends GameState
	{
		/*
		 * (non-Javadoc)
		 * @see de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState#processPacket(de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean)
		 */
		@Override
		public void processPacket(XMPPBean inBean)
		{
		
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
							mServiceDiscoveryResultHandler.sendEmptyMessage(BackgroundService.CODE_SERVICE_SUPPORTED);
						}
						
						// else notify user
						else {
							Log.w(MainActivity.class.getSimpleName(), "No 9Cards Service installed on Server");
							mServiceDiscoveryResultHandler.sendEmptyMessage(BackgroundService.CODE_SERVICE_NOT_AVAILABLE);
						}
					}
				}
				
				// If Mobilis Server doesn't respond, notify user
				else if(bean.getType() == XMPPBean.TYPE_ERROR)
					mServiceDiscoveryResultHandler.sendEmptyMessage(BackgroundService.CODE_SERVER_RESPONSE_ERROR);		
			}
			
			// Other Beans of type get or set will be responded with an ERROR
			else if(inBean.getType() == XMPPBean.TYPE_GET || inBean.getType() == XMPPBean.TYPE_SET) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(main)";
				
				serverConnection.sendXMPPBeanError(inBean);
			}
		}
		
		@Override
		public void processChatMessage(XMPPInfo xmppInfo)
		{}
	}
	
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.start, menu);
		return true;
	}*/
}
