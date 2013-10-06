package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.util.StringUtils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPBean;
import de.tudresden.inf.rn.mobilis.android.ninecards.borrowed.XMPPInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Game;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.Player;
import de.tudresden.inf.rn.mobilis.android.ninecards.game.ServerConnection;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.CardPlayedMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.GameOverMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.PlayCardMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.PlayerInfo;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.RoundCompleteMessage;
import de.tudresden.inf.rn.mobilis.android.ninecards.message.StartGameMessage;
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
public class PlayActivity extends Activity
{
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The connection to the XMPP server. */
	private ServerConnection serverConnection;
	
	/** The TableLayout showing the players. */
	private TableLayout tbl_players;
	/** A Dialog which blocks the screen until the creator of the game starts it. */
	private ProgressDialog blockBeforeStartDialog;
	
	/** A Map containing all Cards/ImageButtons, with their value as key. */
	private Map<Integer, ImageButton> cardSet;
	/** The game instance. */
	private Game game;
	
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		
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
		blockBeforeStartDialog.setCancelable(true);
		blockBeforeStartDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		blockBeforeStartDialog.setIndeterminate(true);
		blockBeforeStartDialog.show();
	}
	
	
	/**
	 * 
	 * @param button
	 */
	private void handleCardClick(ImageButton button)
	{
		// if game is null, the backgroundservice is not bound yet
		if(game == null)
			return;

		// check which card was tapped and act depending on whether it has already been used
		List<Integer> myUsedCards = game.getPlayers().get(serverConnection.getMyChatID()).getUsedCards();
		
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
		
		// if the tapped card has not been used yet, disable all cards until beginning of next round
		if(validCardId > -1) {
			for (int i=1; i<=cardSet.size(); i++) {
				cardSet.get(i).setClickable(false);

				// "turn over" cards that have not been used yet
				if((i != validCardId) && (!myUsedCards.contains(i)))
					cardSet.get(i).setAlpha(0);
			}
			
			game.getPlayers().get(serverConnection.getMyChatID()).setChosenCard(validCardId);
			mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
			
			//  inform server
			PlayCardMessage playCardMesg = new PlayCardMessage(game.getRound(), validCardId );
			serverConnection.sendPrivateToService(playCardMesg);
		}
		
		else
			Toast.makeText(this, "This card has already been used", Toast.LENGTH_LONG).show();
	}
	
	
	/**
	 * Bind background service using the mBackgroundServiceBoundHandler.
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
	
	
	/** The handler which is called if the XHuntService was bound. */
	private Handler mBackgroundServiceBoundHandler = new Handler()
	{
		@Override
		public void handleMessage(Message messg)
		{
			serverConnection = mBackgroundServiceConnector.getBackgroundService().getServerConnection();
			boolean gameStillOpen = serverConnection.initializeMucAndChat(mUpdateUIHandler);
			
			if(gameStillOpen) {
				game = mBackgroundServiceConnector.getBackgroundService().getGame();
				setTitle(game.getName());
				mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStatePlay());
				initComponents();
				
				// check if we are creator (the UserStatusListener in ServerConnection only informs us if our
				// affiliation changes to admin, but this wouldn't work if the admin leaves and rejoins the game)
				if(serverConnection.isPlayerAdmin())
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_ENABLE_START_GAME_BUTTON);
				
			} else {
				Message mesg = new Message();
				mesg.obj = "This game is already closed";		
				mDisplayToastHandler.sendMessage(mesg);
				PlayActivity.this.finish();
			}
		}
	};
	
	
	/**
	 * The handler to update the players view if something changed, like a player
	 * joined/left the game or the card infos changed after finishing of a round.
	 */
	private Handler mUpdateUIHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what) {
				case BackgroundService.CODE_ENABLE_START_GAME_BUTTON : {
					Button btn_ready = (Button) findViewById(R.id.btn_ready);
					btn_ready.setText(getResources().getString(R.string.txt_btn_play_start_2));
					btn_ready.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							StartGameMessage startMesg = new StartGameMessage();
							serverConnection.sendPrivateToService(startMesg);
						}
					});
	
					btn_ready.setEnabled(true);
					if(blockBeforeStartDialog.isShowing())
						blockBeforeStartDialog.dismiss();
					
					break;
				}
				
				case BackgroundService.CODE_START_GAME : {
					// disable start button for admin users and clear button text for all players
					final Button startBtn = (Button) findViewById(R.id.btn_ready);
					if(startBtn.isEnabled())
						startBtn.setEnabled(false);
					startBtn.setText("");

					// enable card buttons 
					for(ImageButton button : cardSet.values()) {
						button.setAlpha(255);
						button.setClickable(true);
					}
					
					// display round in title bar
					setTitle(game.getName() + " - Round " + game.getRound() + "/" + game.getMaxRounds());
					
					// disable waiting dialog for not-creator players
					if(blockBeforeStartDialog.isShowing())
						blockBeforeStartDialog.dismiss();
					
					break;
				}
				
				case BackgroundService.CODE_START_NEW_ROUND : {
					// update round in title bar
					setTitle(game.getName() + " - Round " + game.getRound() + "/" + game.getMaxRounds());
					
					// re-enable remaining cards
					List<Integer> myUsedCards = game.getPlayers().get(serverConnection.getMyChatID()).getUsedCards();
					for (Map.Entry<Integer, ImageButton> entry : cardSet.entrySet()) {
						if (!myUsedCards.contains(entry.getKey())) {
							entry.getValue().setClickable(true);
							entry.getValue().setAlpha(255);
						}
					}
					
					break;
				}
				
				case BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST : {
					tbl_players.removeAllViews();
					for (Player player : game.getPlayers().values())
						insertNewPlayerRow(player);
					break;
				}
				
				case BackgroundService.CODE_SHOW_GAMEOVER_DIALOG : {
					new AlertDialog.Builder(PlayActivity.this)
						.setTitle("Game Over")
						.setMessage(msg.obj.toString())
						.setPositiveButton("Quit", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								PlayActivity.this.finish();
							}
						})
						.show();
					
					break;
				}
				
				default :
					Log.w(getClass().getSimpleName(), "Unexpected handler event code (" + msg.what + ")");
			}
		}
	};
	
	
	/**
	 * 
	 */
	private Handler mDisplayToastHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if(msg.obj != null)
				Toast.makeText(PlayActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
		}
	};
	
	
	
	/**
	 * Inserts a new player row in the list of players.
	 * 
	 * @param player the player to be insert
	 * @return true, if successful
	 */
	private boolean insertNewPlayerRow(Player player)
	{
		// Set name of player
		TextView tv_player = new TextView(PlayActivity.this);
		tv_player.setText(StringUtils.parseResource(player.getPlayerID()));
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
		tv_rounds_won.setText(player.getRoundsWon() + "");
		tv_rounds_won.setGravity(Gravity.RIGHT);
		tv_rounds_won.setPadding(3, 0, 0, 10);

		// TableRow which contains the data of the player
		TableRow row = new TableRow(PlayActivity.this);
		row.setTag(player.getPlayerID());
		row.addView(tv_player);
		row.addView(tv_used_cards);
		row.addView(tv_rounds_won);

		tbl_players.addView(row);

		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStatePlay());
		
		super.onResume();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#finish()
	 */
	@Override
	public void finish()
	{
		serverConnection.leaveChat();
		unbindService(mBackgroundServiceConnector);	
		super.finish();
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// If the back button of the device was pressed, leave the game
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			finish();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	
	/**
	 * 
	 */
	public class GameStatePlay extends GameState
	{
		/*
		 * (non-Javadoc)
		 * @see de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState#processPacket(de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean)
		 */
		@Override
		public void processPacket(XMPPBean inBean)
		{
			if (inBean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(PlayActivity.class.getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
			}
			
			// in this game state we only expect MUC messages
			else {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(Play)";

				serverConnection.sendXMPPBeanError(inBean);
			}
		}
		
		
		/**
		 * 
		 * @param message
		 */
		public void processChatMessage(XMPPInfo xmppInfo)
		{
			// StartGameMessage
			if(xmppInfo instanceof StartGameMessage) {
				
				if(game.getRound() == 0) {
					// set round to 1 to prevent more start messages from being accepted
					game.setRound(1);
					game.setMaxRounds(((StartGameMessage) xmppInfo).getRounds());
					
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_START_GAME);
				}
			}
			
			// CardPlayedMessage
			else if(xmppInfo instanceof CardPlayedMessage) {

				// check if message corresponds to current round
				if (((CardPlayedMessage) xmppInfo).getRound() == game.getRound()) {
					Player player = game.getPlayers().get(((CardPlayedMessage) xmppInfo).getPlayer());

					// if it's our own player, the value of the chosen card is already set; else set 0 (a '?' will be displayed in UI)
					if (player != null && player.getChosenCard() == -1) {
						
						player.setChosenCard(0);
						mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
					}
				}
			}
			
			// RoundCompleteMessage
			else if(xmppInfo instanceof RoundCompleteMessage) {

				// check if message corresponds to current round
				if (((RoundCompleteMessage) xmppInfo).getRound() == game.getRound()) {
									
					game.setRound(game.getRound() + 1);

					// update all players
					List<PlayerInfo> infos = ((RoundCompleteMessage) xmppInfo).getPlayerInfos();
					for (PlayerInfo info : infos) {
						Player player = game.getPlayers().get(info.getId());
						player.setUsedCards(info.getUsedcards());
						player.setRoundsWon(info.getScore());
						player.setChosenCard(-1);
					}
		
					// update players list on top of screen
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
					
					// update displayed round and re-enable cards
					mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_START_NEW_ROUND);
		
					// display a Toast about the winner of the round
					Message mesg = new Message();
					String winnerFull = ((RoundCompleteMessage) xmppInfo).getWinner();
					mesg.obj =  "Round " + (game.getRound() -1) + " completed!"
							+ "\nWinner is " + StringUtils.parseResource(winnerFull);		
					mDisplayToastHandler.sendMessage(mesg);			
				}
			}
			
			// GameOverMessage
			else if(xmppInfo instanceof GameOverMessage) {
				
				List<PlayerInfo> playerInfos = ((GameOverMessage) xmppInfo).getPlayerInfos();

				// update players list on top of screen
				for (PlayerInfo info : playerInfos) {
					Player player = game.getPlayers().get(info.getId());
					player.setUsedCards(info.getUsedcards());
					player.setRoundsWon(info.getScore());
					player.setChosenCard(-1);
				}
				mUpdateUIHandler.sendEmptyMessage(BackgroundService.CODE_UPDATE_GAME_PLAYERS_LIST);
				
				// create String to be displayed in Game Over Dialog
				StringBuilder displayedString = new StringBuilder();

				while(playerInfos.size() > 0) {
					int score = -1;
					String playerID = "";
					
					for(PlayerInfo info : playerInfos) {
						if(info.getScore() > score) {
							score = info.getScore();
							playerID = info.getId();
						}
					}
					
					for(PlayerInfo info : playerInfos) {
						if(info.getId().equals(playerID)) {
							playerInfos.remove(info);
							break;
						}
					}
			
					displayedString.append(StringUtils.parseResource(playerID) + " - " + score + " points");
					if(playerInfos.size() > 0)
						displayedString.append("\n");
				}
				
				// send string to UI thread which will display it in a dialog
				Message mesg = new Message();
				mesg.obj = displayedString.toString();
				mesg.what = BackgroundService.CODE_SHOW_GAMEOVER_DIALOG;
				mUpdateUIHandler.sendMessage(mesg);
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
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.play, menu);
		return true;
	}

	@Override
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
