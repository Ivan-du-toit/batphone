/* Copyright (C) 2012 The Serval Project
 *
 * This file is part of Serval Software (http://www.servalproject.org)
 *
 * Serval Software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

/**
 * Settings - main settings screen
 *
 * @author Romana Challans <romana@servalproject.org>
 */

package za.co.csir.walkiemesh.ui;

import java.io.File;

import za.co.csir.walkiemesh.PreparationWizard;
import za.co.csir.walkiemesh.R;
import za.co.csir.walkiemesh.ServalBatPhoneApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class SettingsScreenActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsscreen);

		// Network Settings Screen
		Button btnNetworkSettings = (Button) this
				.findViewById(R.id.btnNetworkSettings);
		btnNetworkSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(getApplicationContext(),
						Networks.class));
			}
		});

		// Wifi Settings Screen
		Button btnWifiSettings = (Button) this
				.findViewById(R.id.btnWifiSettings);
		btnWifiSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SettingsScreenActivity.this.startActivity(new Intent(SettingsScreenActivity.this,
						SetupActivity.class));
			}
		});

		// Log file display
		/*
		 * Button btnLogShow = (Button) this .findViewById(R.id.btnLogShow);
		 * btnLogShow.setOnClickListener(new View.OnClickListener() {
		 *
		 * @Override public void onClick(View arg0) {
		 * SettingsScreenActivity.this.startActivity(new Intent(
		 * SettingsScreenActivity.this,
		 * za.co.csir.walkiemesh.LogActivity.class)); } });
		 */
		// Accounts Settings Screen
		Button btnAccountsSettings = (Button) this
				.findViewById(R.id.btnAccountsSettings);
		btnAccountsSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SettingsScreenActivity.this.startActivity(new Intent(
						SettingsScreenActivity.this,
						AccountsSettingsActivity.class));
			}
		});

		// Reset Settings Screen
		Button btnResetWifiSettings = (Button) this
				.findViewById(R.id.btnResetWifi);
		btnResetWifiSettings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Clear out old attempt_ files
				File varDir = new File(
						ServalBatPhoneApplication.context.coretask.DATA_FILE_PATH
								+
								"/var/");
				if (varDir.isDirectory())
					for (File f : varDir.listFiles()) {
						if (!f.getName().startsWith("attempt_"))
							continue;
						f.delete();
					}
				// Re-run wizard
				Intent prepintent = new Intent(SettingsScreenActivity.this,
						PreparationWizard.class);
				prepintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(prepintent);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.help, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId())
		{
		case R.id.settings_help:

			startActivity(new Intent(getApplicationContext(),
					za.co.csir.walkiemesh.ui.HelpActivity.class));

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.help, menu);
		return super.onPrepareOptionsMenu(menu);
	}
}
