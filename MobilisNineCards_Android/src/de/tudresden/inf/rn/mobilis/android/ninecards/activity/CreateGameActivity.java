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
package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.CreateNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.SendNewServiceInstanceBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.ServerConnection;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.ConfigureGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;

/**
 * View used for creating a new game instance.
 * 
 * @author Matthias Köngeter
 *
 */
public class CreateGameActivity extends PreferenceActivity
{
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The connection to the XMPP server. */
	private ServerConnection mServerConnection;
	
	/** The edit text preferences to enter and safe the settings for a new game. */
	private EditTextPreference mEditGameName;
	private EditTextPreference mEditMaxPlayers;
	private EditTextPreference mEditRounds;

	/** A wait dialog which is shown until a new game service was started. */
	private ProgressDialog mWaitDialog;
	

	/*
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.layout_creategame);
		this.setDefaultKeyMode(MODE_PRIVATE);
		setContentView(R.layout.activity_create_game);
		
		bindBackgroundService();
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * Needs to be called in the beginning to bind the background service.
	 */
	private void bindBackgroundService()
	{
		mBackgroundServiceConnector = new ServiceConnector();
		mBackgroundServiceConnector.addHandlerToList(mBackgroundServiceBoundHandler);
		
		bindService(
				new Intent(this, BackgroundService.class),
				mBackgroundServiceConnector,
				Context.BIND_AUTO_CREATE );
	}
	
	
	/**
	 * The handler which is called after the background service was bound successfully.
	 */
	private Handler mBackgroundServiceBoundHandler = new Handler()
	{
		@Override
		public void handleMessage(Message messg) {
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateCreateGame());
			mServerConnection = mBackgroundServiceConnector.getBackgroundService().getServerConnection();
			
			// enable button for creating a new game service instance after background service was bound
			Button btn_Create = (Button)findViewById(R.id.creategame_btn_create);
			btn_Create.setEnabled(true);
		}
	};
	
	
	/**
	 * Needs to be called in the beginning to initialize all UI elements.
	 */
	private void initComponents()
	{
		// The parameters for the new game 
		mEditGameName = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_newgame_gamename));
		mEditRounds = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_newgame_rounds));
		mEditMaxPlayers = (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.key_newgame_maxplayers));

		updateSummaries();

		// Button listener for creating an actual game
		Button btn_Create = (Button)findViewById(R.id.creategame_btn_create);
		btn_Create.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				if(v instanceof Button)
					((Button) v).setEnabled(false);
				
				mServerConnection.sendCreateNewServiceInstance(
						"http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService",
						mEditGameName.getText(),
						null);
			}
		});
	}
	
	
	/**
	 * Update the summary of each edit text preference to have it show its current value
	 */
	private void updateSummaries()
	{
		if (mEditGameName != null)
			mEditGameName.setSummary(mEditGameName.getText());
		
		if (mEditMaxPlayers != null)
			mEditMaxPlayers.setSummary(mEditMaxPlayers.getText());
		
		if (mEditRounds != null)
			mEditRounds.setSummary(mEditRounds.getText());
	}
	
	
    /**
     * The handler used to configure the new game instance after it has been successfully created.
     */
    private Handler mCreateNewInstanceHandler = new Handler()
    {
		@Override
		public void handleMessage(Message msg) {
			configureGame();
		}
	};
	
	
	/**
	 * Needs to be called after a new ninecards service instance was successfully created.
	 * Configures the new service using the parameters entered by the user, then switches to play view.
	 */
	private void configureGame()
	{
		mBackgroundServiceConnector.getBackgroundService().createGame(mEditGameName.getText());
		
		int rounds = 9;
		int maxplayers = 6;

		try {
			rounds = Integer.valueOf(mEditRounds.getText());
			maxplayers = Integer.valueOf(mEditMaxPlayers.getText());
		} catch (Exception e) { Log.e(this.getClass().toString(), e.getMessage()); }
		
		mServerConnection.sendGameConfiguration(
				maxplayers,
				rounds);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		updateSummaries();
		super.onWindowFocusChanged(hasFocus);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateCreateGame());
		
		super.onResume();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish()
	{
		unbindService(mBackgroundServiceConnector);
		super.finish();
	}
	
	
	/**
	 * Internal class which represents the current state of the game.
	 * Also responsible for processing messages from the mobilis server. 
	 */
	private class GameStateCreateGame extends GameState
	{
		/*
		 * (non-Javadoc)
		 * @see de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState#processPacket(de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean)
		 */
		@Override
		public void processPacket(XMPPBean inBean)
		{
			// Handle error bean
			if(inBean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(this.getClass().getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
				
				if(inBean.errorText != null)
					Log.e(this.getClass().getSimpleName(), "errorText: " + inBean.errorText);
			}
			
			// Handle CreateNewServiceInstanceBean
			if(inBean instanceof CreateNewServiceInstanceBean) {
				CreateNewServiceInstanceBean bean = (CreateNewServiceInstanceBean) inBean;

				// display a progress dialog until the new game service instance was created.
				mWaitDialog = new ProgressDialog(CreateGameActivity.this);
				mWaitDialog.setTitle("Please wait...");
				mWaitDialog.setCancelable(true);
				mWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mWaitDialog.setIndeterminate(true);
				mWaitDialog.show();
			}
			
			if(inBean instanceof SendNewServiceInstanceBean) {
				SendNewServiceInstanceBean bean = (SendNewServiceInstanceBean) inBean;
				
				if((bean != null) && (bean.getType() != XMPPBean.TYPE_ERROR)) {
					mBackgroundServiceConnector.getBackgroundService().setGameServiceJID(bean.getJidOfNewService());
					mCreateNewInstanceHandler.sendEmptyMessage(0);
				}
			}
			
			// Handle ConfigureGameResponse
			if(inBean instanceof ConfigureGameResponse) {
				ConfigureGameResponse bean = (ConfigureGameResponse) inBean;
				
				if((bean != null) && bean.getType() != (XMPPBean.TYPE_ERROR)) {
					mBackgroundServiceConnector.getBackgroundService().setMucId(bean.getMuc());
					
					if(mWaitDialog.isShowing())
						mWaitDialog.dismiss();

					startActivity(new Intent(CreateGameActivity.this, PlayActivity.class));
					CreateGameActivity.this.finish();
				}

			}
			
			// Other Beans of type get or set will be responded with an ERROR
			else if((inBean.getType() == XMPPBean.TYPE_GET) || (inBean.getType() == XMPPBean.TYPE_SET)) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(create)";
				
				mServerConnection.sendXMPPBeanError(inBean);
			}			
		}
		
		
		/*
		 * (non-Javadoc)
		 * @see de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState#processChatMessage(de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo)
		 */
		@Override
		public void processChatMessage(XMPPInfo xmppInfo)
		{}
	}
		

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_game, menu);
		return true;
	}*/

	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	/*@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/
}
