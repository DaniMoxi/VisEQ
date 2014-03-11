package com.lsu.vizeq;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class RoleActivity extends Activity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_role);
		findViewById(R.id.DJ).setOnClickListener(new View.OnClickListener() 
		{
				@Override
				public void onClick(View v)
				{
					Intent nextIntent = new Intent(RoleActivity.this, HostActivity.class);
					startActivity(nextIntent);					
				}

		});
		findViewById(R.id.NotADJ).setOnClickListener(new View.OnClickListener() 
		{
				@Override
				public void onClick(View v)
				{
					Intent nextIntent = new Intent(RoleActivity.this, SoundVisualizationActivity.class);
					startActivity(nextIntent);					
				}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.role, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent nextIntent  = new Intent(RoleActivity.this, SettingsActivity.class);
			startActivity(nextIntent);
		}
		return true;
	}	
}
