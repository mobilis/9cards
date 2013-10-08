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

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
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

/**
 * View used for displaying existing game instances which were discovered on the mobilis server.
 * 
 * @author Matthias Köngeter
 *
 */
public class OpenGamesActivity extends Activity
{
	
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The connection to the XMPP server. */
	private ServerConnection mServerConnection;
	
	/** The list adapter for the list of the open games. */
	private OpenGamesListAdapter mOpenGamesListAdapter;
	/** The layout inflater needed for dynamically changing the view. */
	private LayoutInflater mLayoutInflater;
	
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_games);

		mLayoutInflater = getLayoutInflater();
		
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
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateOpenGames());
			mServerConnection = mBackgroundServiceConnector.getBackgroundService().getServerConnection();
			
			discoverOpenGames();
		}
	};
	
	
	/**
	 * Needs to be called in the beginning to initialize all UI elements.
	 */
    private void initComponents()
    {
    	mOpenGamesListAdapter = new OpenGamesListAdapter();    	
    	
    	ListView lv_Games = (ListView)findViewById(R.id.opengames_list);
    	lv_Games.setEmptyView(findViewById(R.id.opengames_list_empty));
    	lv_Games.setAdapter(mOpenGamesListAdapter);
    	
    	// listener for game items
    	lv_Games.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				// If user clicks on a game in the list, try to join it
				OpenGameItem openGameItem = mOpenGamesListAdapter.getItem(position);
				Log.i(OpenGamesActivity.class.getSimpleName(), "Game item tapped (itemId: " + openGameItem.gameID + ")");
				
				mBackgroundServiceConnector.getBackgroundService().setGameServiceJID(openGameItem.gameJID);
				mBackgroundServiceConnector.getBackgroundService().createGame(openGameItem.gameName);

	        	startActivity(new Intent(OpenGamesActivity.this, PlayActivity.class));
			}
		});
    	
    	// listener for 'create new game' button
    	Button btn_CreateGame = (Button)findViewById(R.id.opengames_btn_newgame);
    	btn_CreateGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(OpenGamesActivity.this, CreateGameActivity.class));
			}
		});
    	
    	// listener for 'reload games' button
    	Button btn_ReloadGames = (Button)findViewById(R.id.opengames_btn_reloadgames);
    	btn_ReloadGames.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				discoverOpenGames();
			}
		});
    }
	
	
    /**
     * Sends a service discovery to the mobilis server to request open games
     */
    private void discoverOpenGames()
    {
		// Clear game list 
		if(mOpenGamesListAdapter != null)
			mOpenGamesListAdapter.List.clear();
    	
		// ask for existing instances of ninecards service by using ninecards service namespace
    	mServerConnection.sendServiceDiscovery("http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService");
    }
    
    
    /**
     * The handler for reacting to a service discovery response.
     */
    private Handler mDiscoverGamesHandler = new Handler()
    {
		@Override
		public void handleMessage(Message msg)
		{
			// Update list of the open games
			mOpenGamesListAdapter.notifyDataSetChanged();
			
			switch(msg.what) {
				case BackgroundService.CODE_GAMES_AVAILABLE : {					
					break;
				}
				case BackgroundService.CODE_NO_GAMES_AVAILABLE : {
					Toast.makeText(OpenGamesActivity.this, "There are no games available right now!", Toast.LENGTH_LONG).show();
					break;
				}
				case BackgroundService.CODE_DISCOVER_GAMES_FAILURE : {
					Toast.makeText(OpenGamesActivity.this, "Failed to load open games!", Toast.LENGTH_LONG).show();
					break;
				}
				default :
					Log.w(getClass().getSimpleName(), "Unexpected handler event code (" + msg.what + ")");
			}
		}
	};
	
    
    /**
     * Internal class which is used to display and manage the list of discovered/existing games
     */
    private class OpenGamesListAdapter extends BaseAdapter
    {
    	/** The List of games. */
	    public List<OpenGameItem> List;
    	
    	/**
	     * Instantiates a new OpenGamesListAdapter.
	     */
	    public OpenGamesListAdapter()
	    {
			this.List = new ArrayList<OpenGamesActivity.OpenGameItem>();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount()
		{
			return List.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public OpenGameItem getItem(int position)
		{
			return List.get(position);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position)
		{
			return List.size() > 0 ? List.get(position).gameID : 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
	        View view = null;
	        
	        view = (convertView != null)
	        		? (LinearLayout) convertView
	        		: mLayoutInflater.inflate(R.layout.listitem_opengames, null);

	        ImageView img_image = (ImageView) view.findViewById(R.id.listitem_opengames_image);
        	img_image.setBackgroundResource(List.get(position).drawableID);
        	view.setTag(img_image);
        	
        	TextView tv_name = (TextView)view.findViewById(R.id.listitem_opengames_name);
        	tv_name.setText(List.get(position).gameName != null ? List.get(position).gameName : List.get(position).gameJID);
        	view.setTag(tv_name);
	        
	        return view;
		}
    }
    
    
    /**
     * Internal class which is used as a complex structure for the game list adapter.
     */
    private class OpenGameItem
    {
    	/** The game ID. */
	    public long gameID;
    	
	    /** The game name. */
	    public String gameName;
    	
	    /** The game JID. */
	    public String gameJID;

	    /** The version number of the ninecards service (currently not used). */
	    public int gameServiceVersion;

	    /** The drawable ID. */
	    public int drawableID;
    	
    	/**
	     * Instantiates a new OpenGameItem.
	     *
	     * @param gameID the id of the game
	     * @param gameJID the jid of the game service
	     * @param gameName the name of the game
	     * @param players the players which are currently in the game
	     */
	    public OpenGameItem(long gameID, String gameJID, int gameServiceVersion, String gameName)
	    {
    		this.gameID = gameID;
			this.gameJID = gameJID;
			this.gameServiceVersion = gameServiceVersion;
			this.gameName = gameName;

			this.drawableID = R.drawable.ic_game;
		}
    }
    
    
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateOpenGames());
		
		if(mServerConnection != null && mServerConnection.isConnected())
			discoverOpenGames();
		
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
	private class GameStateOpenGames extends GameState
	{
		/*
		 * (non-Javadoc)
		 * @see de.tudresden.inf.rn.mobilis.android.ninecards.game.GameState#processPacket(de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean)
		 */
		@Override
		public void processPacket(XMPPBean inBean)
		{
			if(inBean.getType() == XMPPBean.TYPE_ERROR) {
				Log.e(this.getClass().getSimpleName(), "IQ Type ERROR: " + inBean.toXML());
			}
			
			// Handle MobilisServiceDiscoveryBean
			if(inBean instanceof MobilisServiceDiscoveryBean) {
				MobilisServiceDiscoveryBean bean = (MobilisServiceDiscoveryBean) inBean;

				if(bean != null && bean.getType() != XMPPBean.TYPE_ERROR) {
					if(bean.getDiscoveredServices() != null && bean.getDiscoveredServices().size() > 0 ) {
						
						// check if ServiceDiscoveryBean contains game instances or just admin-/coordinator-/other services
						List<MobilisServiceInfo> gameInstances = new ArrayList<MobilisServiceInfo>();
						for(MobilisServiceInfo info : bean.getDiscoveredServices()) {
							if((info.getJid() != null)
									&& (info.getJid().toLowerCase().contains("ninecards"))
									&& (!info.getJid().toLowerCase().contains("coordinator"))
									&& (!info.getJid().toLowerCase().contains("admin"))
									&& (!info.getJid().toLowerCase().contains("deployment"))) {
								gameInstances.add(info);
							}
						}
						
						// if ServiceDiscoveryBean contained game instances, refresh list
						if(gameInstances.size() > 0) {
							mOpenGamesListAdapter.List.clear();
							for(MobilisServiceInfo info : gameInstances) {
								mOpenGamesListAdapter.List.add(new OpenGameItem(
										info.hashCode(), info.getJid(), Integer.parseInt(info.getVersion()), info.getServiceName()));
							}
							
							mDiscoverGamesHandler.sendEmptyMessage(BackgroundService.CODE_GAMES_AVAILABLE);
						}
						
						else {
							mDiscoverGamesHandler.sendEmptyMessage(BackgroundService.CODE_NO_GAMES_AVAILABLE);
						}

					} else {
						mDiscoverGamesHandler.sendEmptyMessage(BackgroundService.CODE_NO_GAMES_AVAILABLE);
					}
				}
			}
			
			// Other Beans of type get or set will be responded to with an ERROR
			else if(inBean.getType() == XMPPBean.TYPE_GET || inBean.getType() == XMPPBean.TYPE_SET) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(opengames)";
				
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
		getMenuInflater().inflate(R.menu.open_games, menu);
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
