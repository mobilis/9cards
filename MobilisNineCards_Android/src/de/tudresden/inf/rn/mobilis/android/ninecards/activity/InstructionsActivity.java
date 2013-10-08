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
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.android.ninecards.R;

/**
 * View used for displaying instructions for playing.
 * 
 * @author Matthias Köngeter
 *
 */
public class InstructionsActivity extends Activity
{

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	/**
	 * Needs to be called in the beginning to initialize the text view which displays the instructions.
	 */
	private void initComponents()
	{
		TextView tv_instructions = (TextView)findViewById(R.id.instructions_tv_text);
		
		tv_instructions.setText("This game is a simple multiplayer card game as part of the mobilis project.\n\n");
		
		tv_instructions.setText(tv_instructions.getText() 
				+ "Each player starts with nine cards representing values from 1-9. "
				+ "In each round the players choose a card, the one who played the highest card"
				+ " wins the round. The player who wins the most rounds, wins the game.\n\n");
		
		tv_instructions.setText(tv_instructions.getText() 
				+ "Once you joined a game, you have to wait until its creator decides to start it."
				+ " Only games which haven't been started yet can be joined!\n\n");
		
		tv_instructions.setText(tv_instructions.getText()
				+ "Visit us at http://github.com/mobilis for further information!");
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
		getMenuInflater().inflate(R.menu.instructions, menu);
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
