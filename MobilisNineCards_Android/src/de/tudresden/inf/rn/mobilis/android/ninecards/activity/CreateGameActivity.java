package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.annotation.TargetApi;
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
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.ConfigureGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.CreateNewServiceInstanceBean;

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
public class CreateGameActivity extends PreferenceActivity {
	
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	
	/** The MXAProxy. */
	private MXAProxy mMxaProxy;
	
	/** The textfield to define the name of the new game. */
	private EditTextPreference mEditGameName;
	
	/** The textfield to configure the max. count of players. */
	private EditTextPreference mEditMaxPlayers;
	
	/** The textfield to configure the count of rounds to play. */
	private EditTextPreference mEditRounds;
	

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.layout_creategame);
		this.setDefaultKeyMode(MODE_PRIVATE);
		setContentView(R.layout.activity_create_game);
		
		bindBackgroundService();
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * Bind background service using the mBackgroundServiceBoundHandler.
	 */
	private void bindBackgroundService() {
		mBackgroundServiceConnector = new ServiceConnector();
		mBackgroundServiceConnector.addHandlerToList(mBackgroundServiceBoundHandler);
		
		bindService(
				new Intent(this, BackgroundService.class),
				mBackgroundServiceConnector,
				Context.BIND_AUTO_CREATE );
	}
	
	
	/** The handler which is called if the XHuntService was bound. */
	private Handler mBackgroundServiceBoundHandler = new Handler() {
		@Override
		public void handleMessage(Message messg) {
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateCreateGame());
			mMxaProxy = mBackgroundServiceConnector.getBackgroundService().getMXAProxy();
			mMxaProxy.getMucProxy().registerIncomingMessageObserver(CreateGameActivity.this);
		}
	};
	
	
	/**
	 * Initialize all UI elements from resources.
	 */
	private void initComponents() {

		// The parameters for the new game 
		mEditGameName = (EditTextPreference) getPreferenceScreen().findPreference(
				getResources().getString(R.string.key_newgame_gamename));

		mEditRounds = (EditTextPreference) getPreferenceScreen().findPreference(
				getResources().getString(R.string.key_newgame_rounds));
		
		mEditMaxPlayers = (EditTextPreference) getPreferenceScreen().findPreference(
				getResources().getString(R.string.key_newgame_maxplayers));

		updateSummaries();

		// Button listener for creating an actual game
		Button btn_Create = (Button)findViewById(R.id.creategame_btn_create);
		btn_Create.setOnClickListener(new OnClickListener() {	
			
			@Override
			public void onClick(View v) {
				if(v instanceof Button)
					((Button) v).setEnabled(false);
				
				mMxaProxy.getIqProxy().sendCreateNewServiceInstanceIQ(
						"http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService",
						mEditGameName.getText(),
						null);
			}
		});
	}
	
	
	/**
	 * Update summaries of all preference entries.
	 * A summary displays the current value of a preference.
	 */
	private void updateSummaries() {
		
		if (mEditGameName != null)
			mEditGameName.setSummary(mEditGameName.getText());
		
		if (mEditMaxPlayers != null)
			mEditMaxPlayers.setSummary(mEditMaxPlayers.getText());
		
		if (mEditRounds != null)
			mEditRounds.setSummary(mEditRounds.getText());
			
	}
	
	
	/**
	 * Creates the game using the configurable parameters.
	 */
	private void configureGame() {
		
		int rounds = 9;
		int maxplayers = 6;
		
		try{
			rounds = Integer.valueOf(mEditRounds.getText());
			maxplayers = Integer.valueOf(mEditMaxPlayers.getText());
		} catch (Exception e) { Log.e(this.getClass().toString(), e.getMessage()); }
		
		mMxaProxy.getIqProxy().configureGame(
				mBackgroundServiceConnector.getBackgroundService().getGameServiceJid(),
				mBackgroundServiceConnector.getBackgroundService().getGame().getName(),
				maxplayers,
				rounds,
				_configureGameCallback );
	}
	
	
	private IXMPPCallback<ConfigureGameResponse> _configureGameCallback = new IXMPPCallback<ConfigureGameResponse>() {
		
		@Override
		public void invoke(ConfigureGameResponse bean) {
			
			if((bean.getType() == XMPPIQ.TYPE_ERROR) && (bean.errorText != null))
				Toast.makeText(CreateGameActivity.this, "Couldn't create game. Reasons: " + bean.errorText, Toast.LENGTH_LONG).show();
			
			else if((bean != null) && (bean.getType() != XMPPBean.TYPE_ERROR)) {
				startActivity(new Intent(CreateGameActivity.this, LobbyActivity.class));
				unbindService(mBackgroundServiceConnector);
				CreateGameActivity.this.finish();
			}
		}
	};
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		updateSummaries();
		super.onWindowFocusChanged(hasFocus);
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateCreateGame());
		
		super.onResume();
	}
	
	
	/**
	 * 
	 */
	@Override
	public void finish() {
		unbindService(mBackgroundServiceConnector);
		super.finish();
	}
	
	
	/**
	 *
	 */
	private class GameStateCreateGame extends GameState {

		@Override
		public void processPacket(XMPPBean inBean) {
			
			// Handle error bean
			if(inBean.getType() == XMPPBean.TYPE_ERROR){
				Log.e(this.getClass().getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
				
				if(inBean.errorText != null)
					Log.e(this.getClass().getSimpleName(), "errorText: " + inBean.errorText);
			}
			
			// Handle CreateNewServiceInstanceBean
			if(inBean instanceof CreateNewServiceInstanceBean) {
				CreateNewServiceInstanceBean bean = (CreateNewServiceInstanceBean)inBean;
				
				if((bean != null) && (bean.getType() != XMPPBean.TYPE_ERROR)) {
					mBackgroundServiceConnector.getBackgroundService().setGameServiceJid(bean.jidOfNewService);
					mBackgroundServiceConnector.getBackgroundService().setServiceVersion(bean.serviceVersion);		

					configureGame();
				}
			}
			
			// Other Beans of type get or set will be responded with an ERROR
			else if((inBean.getType() == XMPPBean.TYPE_GET) || (inBean.getType() == XMPPBean.TYPE_SET)) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(create)";
				
				mMxaProxy.getIqProxy().sendXMPPBeanError(inBean);
			}			
		}
		
	}
	
	

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.create_game, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
