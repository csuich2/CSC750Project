package edu.ncsu.interactionmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartServiceActivity extends Activity {
	
	Button settingsButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		settingsButton = (Button)findViewById(R.id.buttonSettings);
		
		settingsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(StartServiceActivity.this, SettingsActivity.class));
			}
		});
	}

	@Override
	public void onResume() {
	    super.onResume();
	    
	    startService(new Intent(this, MyService.class));
	}

}
