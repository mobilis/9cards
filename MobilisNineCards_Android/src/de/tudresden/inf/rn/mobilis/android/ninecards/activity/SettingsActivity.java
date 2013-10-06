package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;

public class SettingsActivity extends PreferenceActivity
{
	private EditTextPreference mobilisServerJid;
	private EditTextPreference xmppServerAddress;
	private EditTextPreference userJid;
	private EditTextPreference userNick;
	private EditTextPreference userPassword;
	
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
	 * 
	 */
	private void initComponents()
	{
		xmppServerAddress	= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_server_xmpp));
		mobilisServerJid	= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_server_mobilis_jid));
		userJid				= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_jid));
		userNick			= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_nick));
		userPassword		= (EditTextPreference) getPreferenceScreen().findPreference(getResources().getString(R.string.edit_text_pref_user_password));
		
		
		mobilisServerJid.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

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
	 * 
	 */
	private void updateSummaries()
	{
		if(xmppServerAddress != null)
			xmppServerAddress.setSummary(xmppServerAddress.getText());
		
		if(mobilisServerJid != null)
			mobilisServerJid.setSummary(mobilisServerJid.getText());
		
		if(userJid != null)
			userJid.setSummary(userJid.getText());
		
		if(userNick != null)
			userNick.setSummary(userNick.getText());
		
		if(userPassword != null)
			userPassword.setSummary(userPassword.getText());
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
		getMenuInflater().inflate(R.menu.settings, menu);
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
