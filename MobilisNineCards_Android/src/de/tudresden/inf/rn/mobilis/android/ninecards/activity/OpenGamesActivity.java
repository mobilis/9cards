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
public class OpenGamesActivity extends Activity
{
	
	/** The connection to the background service. */
	private ServiceConnector mBackgroundServiceConnector;
	/** The connection to the XMPP server. */
	private ServerConnection serverConnection;
	
	/** The list adapter for the list of the open games. */
	private OpenGamesListAdapter mOpenGamesListAdapter;
	/** The layout inflater. */
	private LayoutInflater mLayoutInflater;
	
	
	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_open_games);

		// For dynamic layout of the games list
		mLayoutInflater = getLayoutInflater();
		
		bindBackgroundService();
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
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
		public void handleMessage(Message messg) {
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateOpenGames());
			serverConnection = mBackgroundServiceConnector.getBackgroundService().getServerConnection();
			
			discoverOpenGames();
		}
	};
	
	
	/**
	 * Initialize all UI elements from resources.
	 */
    private void initComponents()
    {
    	mOpenGamesListAdapter = new OpenGamesListAdapter();    	
    	
    	ListView lv_Games = (ListView)findViewById(R.id.opengames_list);
    	lv_Games.setEmptyView(findViewById(R.id.opengames_list_empty));
    	lv_Games.setAdapter(mOpenGamesListAdapter);
    	
    	lv_Games.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id)
			{
				// If user click on a game in the list, load the game details
				OpenGameItem openGameItem = mOpenGamesListAdapter.getItem(position);
				Log.i(OpenGamesActivity.class.getSimpleName(), "Game item tapped (itemId: " + openGameItem.GameId + ")");
				
				mBackgroundServiceConnector.getBackgroundService().setGameServiceJid(openGameItem.Jid);
				mBackgroundServiceConnector.getBackgroundService().createGame(openGameItem.Name);

	        	startActivity(new Intent(OpenGamesActivity.this, PlayActivity.class));
			}
		});
    	
    	Button btn_CreateGame = (Button)findViewById(R.id.opengames_btn_newgame);
    	btn_CreateGame.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(OpenGamesActivity.this, CreateGameActivity.class));
			}
		});
    	
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
     * Discover open games
     */
    private void discoverOpenGames()
    {
		// Clear game list 
		if(mOpenGamesListAdapter != null)
			mOpenGamesListAdapter.List.clear();
    	
		// Send a ServiceDiscovery to the Mobilis-Server to ask for running 9Cards-Services
    	serverConnection.sendServiceDiscovery("http://mobilis.inf.tu-dresden.de#services/MobilisNineCardsService");
    }
    
    
    /** The handler for response of the MobilisServiceDiscoveryBean. */
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
     * The Class OpenGamesListAdapter used to display and manage the list of games.
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
			return List.size() > 0 ? List.get(position).GameId : 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
	        View view = null;

	        if(convertView != null) {
	        	view = (LinearLayout) convertView;
	        }
	        
	        else if(convertView == null) {
	        	// Use a custom layout inside the listelement
	        	view = mLayoutInflater.inflate(R.layout.listitem_opengames, null);
	        }

	        ImageView img_image = (ImageView)view.findViewById(R.id.listitem_opengames_image);
        	img_image.setBackgroundResource(List.get(position).DrawableId);
        	view.setTag(img_image);
        	
        	TextView tv_name = (TextView)view.findViewById(R.id.listitem_opengames_name);
        	tv_name.setText(List.get(position).Name != null ? List.get(position).Name : List.get(position).Jid);
        	view.setTag(tv_name);
	        
	        return view;
		}
    }
    
    
    /**
     * The Class OpenGameItem is used to handle complex structure in list adapter for games.
     */
    private class OpenGameItem
    {
    	/** The Game id. */
	    public long GameId;
    	
	    /** The Name. */
	    public String Name;
    	
	    /** The Jid. */
	    public String Jid;

	    /** The service version (currently not used). */
	    public int ServiceVersion;

	    /** The Drawable id. */
	    public int DrawableId;
    	
    	/**
	     * Instantiates a new OpenGameItem.
	     *
	     * @param gameId the id of the game
	     * @param jid the jid of the game service
	     * @param name the name of the game
	     * @param players the players which are currently in the game
	     */
	    public OpenGameItem(long gameId, String jid, int serviceVersion, String name)
	    {
    		this.GameId = gameId;
			this.Jid = jid;
			this.ServiceVersion = serviceVersion;
			this.Name = name;

			this.DrawableId = R.drawable.ic_game;
		}
    }
    
    
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume()
	{
		if(mBackgroundServiceConnector.getBackgroundService() != null)
			mBackgroundServiceConnector.getBackgroundService().setGameState(new GameStateOpenGames());
		
		if(serverConnection != null && serverConnection.isConnected())
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
	 * 
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
			
			// Handle MobilisServiceDiscoveryBean and the containing services
			if(inBean instanceof MobilisServiceDiscoveryBean) {
				MobilisServiceDiscoveryBean bean = (MobilisServiceDiscoveryBean) inBean;

				if(bean != null && bean.getType() != XMPPBean.TYPE_ERROR) {
					if(bean.getDiscoveredServices() != null && bean.getDiscoveredServices().size() > 0 ) {
						
						// check if ServiceDiscoveryBean contains game instances or just admin-/coordinator-/etc services
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
								mOpenGamesListAdapter.List.add(new OpenGameItem(info.hashCode(),
										info.getJid(), Integer.parseInt(info.getVersion()), info.getServiceName()));
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
			
			// Other Beans of type get or set will be responded with an ERROR
			else if(inBean.getType() == XMPPBean.TYPE_GET || inBean.getType() == XMPPBean.TYPE_SET) {
				inBean.errorType = "wait";
				inBean.errorCondition = "unexpected-request";
				inBean.errorText = "This request is not supportet at this game state(opengames)";
				
				serverConnection.sendXMPPBeanError(inBean);
			}		
		}
		
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

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.open_games, menu);
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
