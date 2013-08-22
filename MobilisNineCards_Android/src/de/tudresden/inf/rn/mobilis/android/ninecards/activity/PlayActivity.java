package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.IXMPPCallback;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.JoinGameResponse;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.MessageWrapper;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerInfosMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.PlayerLeavingMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.clientstub.StartGameMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.communication.MXAProxy;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Player;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.BackgroundService;
import de.tudresden.inf.rn.mobilis.android.ninecards.service.ServiceConnector;
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
public class PlayActivity extends Activity {

	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The MXAProxy. */
	private MXAProxy mMxaProxy;
	
	/** The TableLayout showing the players. */
	private TableLayout tbl_players;
	/** A Dialog which blocks the screen until the creator of the game starts it. */
	private ProgressDialog blockBeforeStartDialog;
	
	/** A Map containing all Cards/ImageButtons, with their value as key. */
	private Map<Integer, ImageButton> cardSet;
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
		initComponents();
		bindBackgroundService();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * 
	 */
	private void initComponents() {
		tbl_players = (TableLayout) findViewById(R.id.tbl_players);
		cardSet = new HashMap<Integer, ImageButton>(9);
		
		cardSet.put(1, (ImageButton) findViewById(R.id.imageButton1));
		cardSet.put(2, (ImageButton) findViewById(R.id.imageButton2));
		cardSet.put(3, (ImageButton) findViewById(R.id.imageButton3));
		cardSet.put(4, (ImageButton) findViewById(R.id.imageButton4));
		cardSet.put(5, (ImageButton) findViewById(R.id.imageButton5));
		cardSet.put(6, (ImageButton) findViewById(R.id.imageButton6));
		cardSet.put(7, (ImageButton) findViewById(R.id.imageButton7));
		cardSet.put(8, (ImageButton) findViewById(R.id.imageButton8));
		cardSet.put(9, (ImageButton) findViewById(R.id.imageButton9));
		
		for(ImageButton button : cardSet.values()) {
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					handleCardClick((ImageButton) v);
				}
			});
			
			button.setClickable(false);
			button.setAlpha(0);
		}
		
		// display a progress dialog until the creator starts the game.
		blockBeforeStartDialog = new ProgressDialog(this);
		blockBeforeStartDialog.setTitle("Please wait until creator starts the game.");
		blockBeforeStartDialog.setCancelable(true);	//TODO
		blockBeforeStartDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		blockBeforeStartDialog.setIndeterminate(true);
		blockBeforeStartDialog.show();
	}
	
	
	/**
	 * 
	 * @param button
	 */
	private void handleCardClick(ImageButton button) {
		
		// check which card was tapped and act depending on whether it has already been used
		List<Integer> myUsedCards = mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers()
				.get(mBackgroundServiceConnector.getBackgroundService().getUserJid()).getUsedCards();
		
		int validCardId = -1;
		
		switch(button.getId()) {
		case R.id.imageButton1 :
			if(!myUsedCards.contains(1)) {
				((ImageButton) findViewById(R.id.imageButton1)).setAlpha(25);
				validCardId = 1;
			}
			break;
		case R.id.imageButton2 :
			if(!myUsedCards.contains(2)) {
				((ImageButton) findViewById(R.id.imageButton2)).setAlpha(25);
				validCardId = 2;
			}
			break;
		case R.id.imageButton3 :
			if(!myUsedCards.contains(3)) {
				((ImageButton) findViewById(R.id.imageButton3)).setAlpha(25);
				validCardId = 3;
			}
			break;
		case R.id.imageButton4 :
			if(!myUsedCards.contains(4)) {
				((ImageButton) findViewById(R.id.imageButton4)).setAlpha(25);
				validCardId = 4;
			}
			break;
		case R.id.imageButton5 :
			if(!myUsedCards.contains(5)) {
				((ImageButton) findViewById(R.id.imageButton5)).setAlpha(25);
				validCardId = 5;
			}
			break;
		case R.id.imageButton6 :
			if(!myUsedCards.contains(6)) {
				((ImageButton) findViewById(R.id.imageButton6)).setAlpha(25);
				validCardId = 6;
			}
			break;
		case R.id.imageButton7 :
			if(!myUsedCards.contains(7)) {
				((ImageButton) findViewById(R.id.imageButton7)).setAlpha(25);
				validCardId = 7;
			}
			break;
		case R.id.imageButton8 :
			if(!myUsedCards.contains(8)) {
				((ImageButton) findViewById(R.id.imageButton8)).setAlpha(25);
				validCardId = 8;
			}
			break;
		case R.id.imageButton9 :
			if(!myUsedCards.contains(9)) {
				((ImageButton) findViewById(R.id.imageButton9)).setAlpha(25);
				validCardId = 9;
			}
			break;
		}
		
		// if the tapped card has not been used yet, disable all cards until next round begins
		if(validCardId > -1) {
			for (int i=1; i<=cardSet.size(); i++) {
				cardSet.get(i).setClickable(false);

				// "turn over" cards that have not been used yet
				if((i != validCardId) && (!myUsedCards.contains(i)))
					cardSet.get(i).setAlpha(0);
			}
			
			//  inform server
			PlayCardMessage playCardMesg = new PlayCardMessage(
					mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().get(mBackgroundServiceConnector.getBackgroundService().getUserJid()).getName(),
					mBackgroundServiceConnector.getBackgroundService().getUserJid(),
					validCardId );
					
			MessageWrapper wrapper = new MessageWrapper(true, playCardMesg.toXML(), "PlayCard");
			mBackgroundServiceConnector.getBackgroundService().getMXAProxy().getMucProxy().sendMessageToMuc(wrapper.toXML());
		}
		
		else
			Toast.makeText(this, "This card has already been used", Toast.LENGTH_LONG).show();
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

			// if the player is the one who created the game, he'll be enabled start it
			boolean isOwnGame = bean.getCreatorJid().equals(mBackgroundServiceConnector.getBackgroundService().getUserJid());
			if (isOwnGame)
				mJoinGameHandler.sendEmptyMessage(1);

		}
	};
	
	
	/** The handler for JoinGameBeans. */
	private Handler mJoinGameHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			// If joining failed, notify user and go back to open games view
			if (msg.obj != null) {
				Log.e(this.getClass().getSimpleName(), "Failed to join game (" + msg.obj.toString() + ")");
				Toast.makeText(PlayActivity.this, "Failed to join game (" + msg.obj.toString() + ")", Toast.LENGTH_LONG).show();
				PlayActivity.this.finish();
			}
			
			// If joining game was successful, enter chatroom
			else if (msg.what == 0) {
				try {
					mMxaProxy.getMucProxy().connectToMUC(
							mBackgroundServiceConnector.getBackgroundService().getMucRoomId(),
							mBackgroundServiceConnector.getBackgroundService().getMucRoomPw());

					mMxaProxy.getMucProxy().registerIncomingMessageObserver(
							PlayActivity.this, mMucHandler);
					Log.i(this.getClass().getSimpleName(), "Connected to MUC");

				} catch (RemoteException e) {
					Log.e(this.getClass().getSimpleName(), "Failed to connect to MUC");
					Toast.makeText(PlayActivity.this, "Failed to connect to chat", Toast.LENGTH_LONG).show();
					PlayActivity.this.finish();
				}
			}
			
			// only fired for creator of the game, enables him to start it
			else if (msg.what == 1) {
				enableStartButton();
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
	 * The handler to update the players view if something changed, like a player
	 * joined/left the game or the card infos changed after finishing of a round.
	 */
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
	
	
	/**
	 * 
	 */
	private Handler mRoundCompleteHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if(msg.obj != null) {
				String text = "Round " + (mBackgroundServiceConnector.getBackgroundService().getGame().getRound() -1) + " completed!"
						+ " The point goes to " + msg.obj.toString();
				Toast.makeText(PlayActivity.this, text, Toast.LENGTH_LONG).show();
			}
			
			else {
				String text = "The game has finished! Winner is "
						+ mBackgroundServiceConnector.getBackgroundService().getGame().getWinner().getName()
						+ " (" + mBackgroundServiceConnector.getBackgroundService().getGame().getWinner().getRoundsWon() + " wins)";
				Toast.makeText(PlayActivity.this, text, Toast.LENGTH_LONG).show();
				
				// send leave message to muc and finish game
				PlayerLeavingMessage leavingMesg = new PlayerLeavingMessage(mBackgroundServiceConnector.getBackgroundService().getUserJid());
				MessageWrapper wrapper = new MessageWrapper(true, leavingMesg.toXML(), "PlayerLeaving");
				mBackgroundServiceConnector.getBackgroundService().getMXAProxy().getMucProxy().sendMessageToMuc(wrapper.toXML());
				
				PlayActivity.this.finish();
			}
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
		boolean ownPlayer = player.getJid().equals(mBackgroundServiceConnector.getBackgroundService().getUserJid());
		tv_used_cards.setText(player.getUsedCardsAsString(ownPlayer));
		tv_used_cards.setGravity(Gravity.CENTER);
		tv_used_cards.setPadding(3, 0, 0, 10);
		
		// Set the number of rounds which the player already won
		TextView tv_rounds_won = new TextView(PlayActivity.this);
		tv_rounds_won.setText(player.getRoundsWon() + "");
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
				
				final Button btn_ready = (Button) findViewById(R.id.btn_ready);
				btn_ready.setText(getResources().getString(R.string.txt_btn_play_start_2));

				btn_ready.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						
						// embed start message into message wrapper
						StartGameMessage startMesg = new StartGameMessage();
						MessageWrapper wrapper = new MessageWrapper(true, startMesg.toXML(), "StartGame");
						mBackgroundServiceConnector.getBackgroundService().getMXAProxy().getMucProxy().sendMessageToMuc(wrapper.toXML());

						btn_ready.setEnabled(false);
					}
				});

				btn_ready.setEnabled(true);
				
				if(blockBeforeStartDialog.isShowing())
					blockBeforeStartDialog.dismiss();
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
	
	
	public class GameStatePlay extends GameState {

		@Override
		public void processPacket(XMPPBean inBean) {

			if (inBean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(PlayActivity.class.getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
			}
			
			// in this game state we only expect MUC messages
			else {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(Play)";

				mMxaProxy.getIqProxy().sendXMPPBeanError(inBean);
			}
		}
		
		
		/**
		 * 
		 * @param message
		 */
		public void processMucMessage(XMPPInfo message) {
			
			if(message instanceof PlayerInfosMessage) {
				List<PlayerInfo> infos = ((PlayerInfosMessage) message).getPlayers();
				for(PlayerInfo info : infos) {
					Player player = new Player(info.getPlayersJID(), info.getPlayersName());
					player.setUsedCards(info.getPlayersUsedCards());
					player.setRoundsWon(info.getPlayersWins());
					
					mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().put(player.getJid(), player);
				}
				mUpdatePlayersHandler.sendEmptyMessage(0);
			}
			
			else if(message instanceof StartGameMessage) {
				if(mBackgroundServiceConnector.getBackgroundService().getGame().getRound() == 0) {

					// set initial round and enable card buttons 
					mBackgroundServiceConnector.getBackgroundService().getGame().setRound(1);
					for(ImageButton button : cardSet.values()) {
						button.setAlpha(255);
						button.setClickable(true);
					}
					
					// display round in title bar
					setTitle(
							mBackgroundServiceConnector.getBackgroundService().getGame().getName()
							+ " - Round "
							+ mBackgroundServiceConnector.getBackgroundService().getGame().getRound());
					
					// disable waiting dialog for not-creator players
					if(blockBeforeStartDialog.isShowing())
						blockBeforeStartDialog.dismiss();
				}
			}
			
			else if(message instanceof PlayCardMessage) {
				Player player = mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().get(
						((PlayCardMessage) message).getPlayersJID());
				
				if(player == null)
					return;
				
				// check if card has already been used
				if (!player.getUsedCards().contains(((PlayCardMessage) message).getCardID()))
					// check if player has already chosen a card in this round
					if (player.getChosenCard() == -1)
						player.setChosenCard(((PlayCardMessage) message).getCardID());

				mUpdatePlayersHandler.sendEmptyMessage(0);
			}
			
			else if(message instanceof RoundCompleteMessage) {
				
				// check if completed message corresponds to current round
				if(((RoundCompleteMessage) message).getRoundID() == mBackgroundServiceConnector.getBackgroundService().getGame().getRound()) {		
					mBackgroundServiceConnector.getBackgroundService().getGame().setRound(((RoundCompleteMessage) message).getRoundID() +1);
					
					// update all players
					List<PlayerInfo> infos = ((RoundCompleteMessage) message).getPlayerInfos();
					for (PlayerInfo info : infos) {
						Player player = mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().get(info.getPlayersJID());
						player.setUsedCards(info.getPlayersUsedCards());
						player.setRoundsWon(info.getPlayersWins());
						player.setChosenCard(-1);
					}					
					
					// update players list on top of screen
					mUpdatePlayersHandler.sendEmptyMessage(0);
					
					// update round in title bar
					setTitle(
							mBackgroundServiceConnector.getBackgroundService().getGame().getName()
							+ " - Round "
							+ mBackgroundServiceConnector.getBackgroundService().getGame().getRound());
				
					// display a Toast about the winner of the round
					Message mesg = new Message();
					mesg.obj = ((RoundCompleteMessage) message).getRoundWinnersName();
					mRoundCompleteHandler.sendMessage(mesg);
					
					// check if the game is finished
					if(((RoundCompleteMessage) message).getEndOfGame()) {
						mRoundCompleteHandler.sendEmptyMessage(0);
					}
					
					// if not, re-enable remaining cards
					else {
						List<Integer> myUsedCards = mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers()
							.get(mBackgroundServiceConnector.getBackgroundService().getUserJid()).getUsedCards();

						for(Map.Entry<Integer, ImageButton> entry : cardSet.entrySet()) {
							if(!myUsedCards.contains(entry.getKey())) {
								entry.getValue().setClickable(true);
								entry.getValue().setAlpha(255);
							}
						}
					}
				}
			}
			
			else if(message instanceof PlayerLeavingMessage) {
				String jid = ((PlayerLeavingMessage) message).getLeavingJID();
				mBackgroundServiceConnector.getBackgroundService().getGame().getPlayers().remove(jid);
				mUpdatePlayersHandler.sendEmptyMessage(0);
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
