package de.tudresden.inf.rn.mobilis.android.ninecards.activity;

import de.tudresden.inf.rn.mobilis.android.ninecards.R;
import de.tudresden.inf.rn.mobilis.android.ninecards.R.layout;
import de.tudresden.inf.rn.mobilis.android.ninecards.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class InstructionsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		initComponents();
		
		// Show the Up button in the action bar.
		setupActionBar();
	}
	
	
	private void initComponents() {
		
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
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.instructions, menu);
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
