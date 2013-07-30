package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.JoinGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Player;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;
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
public class LobbyActivity extends Activity {

	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	
	/** The MXAProxy. */
	private MXAProxy mMxaProxy;
	
	/** Represents the TableLayout of the Lobby. */
	private TableLayout tbl_lobby;
	
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lobby);

		initComponents();
		bindBackgroundService();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * Initialize all UI elements from resources.
	 */
	private void initComponents() {

		// Get the TableLayout of the Lobby
		tbl_lobby = (TableLayout) findViewById(R.id.tbl_lobby);

		// Get the Ready-Button of the Lobby
		Button btn_ready = (Button) findViewById(R.id.btn_ready);
		btn_ready.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				final Player player = mBackgroundServiceConnector.getBackgroundService().getGame().getPlayer(
						mBackgroundServiceConnector.getBackgroundService().getUserJid());

		//		if (player != null)
		//			startGame(player);
			}
		});
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
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateLobby());
			mMxaProxy = mBackgroundServiceConnector.getBackgroundService().getMXAProxy();
			
			mMxaProxy.getIqProxy().joinGame(
					mBackgroundServiceConnector.getBackgroundService().getGameServiceJid(),
					_joinGameCallback );
		}
	};
	
	
	private IXMPPCallback<JoinGameResponse> _joinGameCallback = new IXMPPCallback<JoinGameResponse>() {

		@Override
		public void invoke(JoinGameResponse bean) {
			
			if (bean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(this.getClass().getSimpleName(), "Joining game failed (" + bean.errorText +")");
				LobbyActivity.this.finish();	
			}
			
			else {
				mBackgroundServiceConnector.getBackgroundService().setMucRoomId(bean.getChatRoom().toLowerCase());
				mBackgroundServiceConnector.getBackgroundService().setMucRoomPw(bean.getChatPassword());
				mMxaProxy.getMucProxy().registerIncomingMessageObserver(LobbyActivity.this);
				
	//			boolean isOwnGame = mBackgroundServiceConnector
				mBackgroundServiceConnector.getBackgroundService().createGame();
	//			mBackgroundServiceConnector.getBackgroundService().getGame().setOwnGame(isOwnGame);
				
				
				try {
					mMxaProxy.getMucProxy().connectToMUC(
							mBackgroundServiceConnector.getBackgroundService().getMucRoomId(),
							mBackgroundServiceConnector.getBackgroundService().getMucRoomPw());
				} catch (RemoteException e) {
					Log.e(this.getClass().getSimpleName(), "Failed to connect to MUC");
					Toast.makeText(LobbyActivity.this, "Failed to connect to chat", Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateLobby());
		
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
	
	
	private class GameStateLobby extends GameState {

		@Override
		public void processPacket(XMPPBean bean) {
			// TODO Auto-generated method stub
			
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
		getMenuInflater().inflate(R.menu.lobby, menu);
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
