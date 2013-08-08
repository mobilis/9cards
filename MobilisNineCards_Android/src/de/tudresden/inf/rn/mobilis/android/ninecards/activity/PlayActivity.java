package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
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
public class PlayActivity extends Activity {

	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The MXAProxy. */
	private MXAProxy mMxaProxy;
	
	/** The TableLayout showing the players. */
	private TableLayout tbl_players;
	/** A Dialog which blocks the screen until the creator of the game starts it. */
	private ProgressDialog blockBeforeStartDialog;
	
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);

		tbl_players = (TableLayout) findViewById(R.id.tbl_players);
		bindBackgroundService();
		
		// display a progress dialog until the creator starts the game.
		blockBeforeStartDialog = new ProgressDialog(this);
		blockBeforeStartDialog.setTitle("Please wait until creator starts the game.");
		blockBeforeStartDialog.setCancelable(false);
		blockBeforeStartDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		blockBeforeStartDialog.setIndeterminate(true);
		blockBeforeStartDialog.show();
		//TODO --> das irgendwo nutzen
		//if(blockBeforeStartDialog.isShowing())
		//	blockBeforeStartDialog.dismiss();
		
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
			setTitle(mBackgroundServiceConnector.getBackgroundService().getGame().getName());
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStatePlay());
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
				Message msg = new Message();
				msg.what = -1;
				msg.obj = bean.errorText;
				mJoinGameHandler.sendMessage(msg);
			}

			else {
				mBackgroundServiceConnector.getBackgroundService().setMucRoomId(bean.getChatRoom().toLowerCase());
				mBackgroundServiceConnector.getBackgroundService().setMucRoomPw(bean.getChatPassword());

				mJoinGameHandler.sendEmptyMessage(0);
			}

			// if the player is the one who created the game, he'll be enabled
			// to start it
			boolean isOwnGame = bean.getCreatorJid().equals(mBackgroundServiceConnector.getBackgroundService().getUserJid());
			if (isOwnGame)
				mJoinGameHandler.sendEmptyMessage(1);

		}
	};
	
	
	/** The handler for JoinGameBeans. */
	private Handler mJoinGameHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			// If joining game was successful, enter chatroom
			if (msg.what == 0) {
				if(!mMxaProxy.getMucProxy().hasJoinedRoom()) {
		/*			try {
						mMxaProxy.getMucProxy().connectToMUC(
								mBackgroundServiceConnector.getBackgroundService().getMucRoomId(),
								mBackgroundServiceConnector.getBackgroundService().getMucRoomPw());

						mMxaProxy.getMucProxy().registerIncomingMessageObserver(PlayActivity.this, mMucHandler);
						
	System.out.println("verbunden mit MUC!");
	mMxaProxy.getMucProxy().sendMessageToMuc("Dies ist ein Test-Body! <>");

					} catch (RemoteException e) {
						Log.e(this.getClass().getSimpleName(), "Failed to connect to MUC");
						Toast.makeText(PlayActivity.this, "Failed to connect to chat", Toast.LENGTH_LONG).show();
						PlayActivity.this.finish();
					}*/
				}
			}
			
			// only fired for creator of the game, enables him to start it
			if (msg.what == 1) {
				enableStartButton();
			}

			// If joining failed, notify user and go back to open games view
			else if (msg.obj != null) {
				Log.e(this.getClass().getSimpleName(), "Failed to join game (" + msg.obj.toString() + ")");
				Toast.makeText(PlayActivity.this, "Failed to join game (" + msg.obj.toString() + ")", Toast.LENGTH_LONG).show();
				PlayActivity.this.finish();
			}
		}
	};
	
	
	/**
	 * The handler for chat messages. The message will be displayed using a Toast and the device will vibrate.
	 */
	private Handler mMucHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.obj != null) {
				Toast.makeText(PlayActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
				
				Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				// start in 0ms, vibrate for 500ms, pause for 300ms, vibrate for 500ms
		    	long[] vibratePattern = {0, 500, 300, 500 };
		    	// -1 prevents from repeating the pattern
		    	vibrator.vibrate(vibratePattern, -1);
			}
		}
	};
	
	
	/**
	 * The handler to update the players view if something changed, like new
	 * player joined the game or the card infos after each round.
	 */
	//TODO --> kann man vllt auch so irgendwie aufrufen
	private Handler mUpdatePlayersHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// Remove all player views
			tbl_players.removeAllViews();

			// For each player insert a row in the players list
			for (Player player : mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().values()) {
				insertNewPlayerRow(player);
			}

			// Display a message shipped by the Bean
			if (msg.obj != null) {
				Toast.makeText(PlayActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
			}
		}
	};
	
	
	private Handler mPlayerLeavingHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO --> hier player-table updaten oder so
		}
	};
	
	
	/**
	 * Inserts a new player row in the list of players.
	 * 
	 * @param player the player to be insert
	 * @return true, if successful
	 */
	private boolean insertNewPlayerRow(Player player) {

		// Set name of player
		TextView tv_player = new TextView(PlayActivity.this);
		tv_player.setText(player.getName());
		tv_player.setTextSize(20);
		tv_player.setTypeface(Typeface.DEFAULT_BOLD);
		tv_player.setPadding(3, 0, 0, 10);

		// Set the cards which the player already used
		TextView tv_used_cards = new TextView(PlayActivity.this);
		tv_used_cards.setText(player.getUsedCardsAsString());
		tv_used_cards.setGravity(Gravity.CENTER);
		tv_used_cards.setPadding(3, 0, 0, 10);
		
		// Set the number of rounds which the player already won
		TextView tv_rounds_won = new TextView(PlayActivity.this);
		tv_rounds_won.setText(player.getRoundsWon());
		tv_rounds_won.setGravity(Gravity.RIGHT);
		tv_rounds_won.setPadding(3, 0, 0, 10);

		// TableRow which contains the data of the player
		TableRow row = new TableRow(PlayActivity.this);
		row.setTag(player.getJid());
		row.addView(tv_player);
		row.addView(tv_used_cards);
		row.addView(tv_rounds_won);

		tbl_players.addView(row);

		return true;
	}
	
	
	/**
	 * The start button will only be enabled for the creator of the game
	 */
	private void enableStartButton() {
		
		runOnUiThread(new Runnable() {
			public void run() {
				
				Button btn_ready = (Button) findViewById(R.id.btn_ready);
				btn_ready.setText(getResources().getString(R.string.txt_btn_play_start_2));

				btn_ready.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//TODO --> StartGame senden
						((ImageView) findViewById(R.id.imageButton1)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton2)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton3)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton4)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton5)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton6)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton7)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton8)).setClickable(true);
						((ImageView) findViewById(R.id.imageButton9)).setClickable(true);
					}
				});

				btn_ready.setEnabled(true);
			}
		});
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStatePlay());
		
		super.onResume();
	}
	
	
	
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish() {
		unbindService(mBackgroundServiceConnector);
		
		super.finish();
}
	
	
	private class GameStatePlay extends GameState {

		@Override
		public void processPacket(XMPPBean inBean) {
			
			System.out.println("GameStatePlay received packet: " + inBean.toXML());

			if (inBean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(PlayActivity.class.getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
			}

			// TODO --> ausarbeiten
			
			// Other Beans of type get or set will be responded with an ERROR
			else if (inBean.getType() == XMPPBean.TYPE_GET || inBean.getType() == XMPPBean.TYPE_SET) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(Lobby)";

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
		getMenuInflater().inflate(R.menu.play, menu);
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
