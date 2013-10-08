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
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;

/**
 * View used for entering addresses of XMPP and Mobilis Server as well as user credentials.
 * 
 * @author Matthias Köngeter
 *
 */
public class SettingsActivity extends PreferenceActivity
{
	
	/** The edit text preferences for entering and saving connectivity settings. */
	private EditTextPreference mMobilisServerJid;
	private EditTextPreference mXmppServerAddress;
	private EditTextPreference mUserJid;
	private EditTextPreference mUserNick;
	private EditTextPreference mUserPassword;
	
	/*
	 * (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		addPreferencesFromResource(R.xml.layout_settings);
		
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * Needs to be called in the beginning to initialize the components.
	 */
	private void initComponents()
	{
		mXmppServerAddress	= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_server_xmpp));
		mMobilisServerJid	= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_server_mobilis_jid));
		mUserJid				= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_jid));
		mUserNick			= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_nick));
		mUserPassword		= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_password));
		
		// make sure that the server JID contains "/Coordinator" as resource
		mMobilisServerJid.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference, Object newValue)
					{
						String serverJid = (String) newValue;
						
						if (serverJid != null && !serverJid.equals("")) {
							String[] arr = serverJid.split("/");
							if (arr.length < 2)
								((EditTextPreference) preference).setText(arr[0].toLowerCase() + "/Coordinator");
						}
						
						return true;
					}
				});
		
		updateSummaries();
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
	
	
	/**
	 * Update the summary of each edit text preference to have it show its current value
	 */
	private void updateSummaries()
	{
		if(mXmppServerAddress != null)
			mXmppServerAddress.setSummary(mXmppServerAddress.getText());
		
		if(mMobilisServerJid != null)
			mMobilisServerJid.setSummary(mMobilisServerJid.getText());
		
		if(mUserJid != null)
			mUserJid.setSummary(mUserJid.getText());
		
		if(mUserNick != null)
			mUserNick.setSummary(mUserNick.getText());
		
		if(mUserPassword != null)
			mUserPassword.setSummary(mUserPassword.getText());
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
		getMenuInflater().inflate(R.menu.settings, menu);
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
