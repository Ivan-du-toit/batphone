/**
 * Copyright (C) 2011 The Serval Project
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

package org.servalproject;

import org.servalproject.ServalBatPhoneApplication.State;
import org.servalproject.account.AccountService;
import org.servalproject.rhizome.RhizomeMain;
import org.servalproject.servald.Identity;
import org.servalproject.ui.Networks;
import org.servalproject.ui.ShareUsActivity;
import org.servalproject.ui.help.HtmlHelp;

import za.co.csir.Setup;
import za.co.csir.walkiemesh.R;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 * Main activity which presents the Serval launcher style screen. On the first
 * time Serval is installed, this activity ensures that a warning dialog is
 * presented and the user is taken through the setup wizard. Once setup has been
 * confirmed the user is taken to the main screen.
 *
 * @author Paul Gardner-Stephen <paul@servalproject.org>
 * @author Andrew Bettison <andrew@servalproject.org>
 * @author Corey Wallis <corey@servalproject.org>
 * @author Jeremy Lakeman <jeremy@servalproject.org>
 * @author Romana Challans <romana@servalproject.org>
 */
public class Main extends Activity {
	public ServalBatPhoneApplication app;
	private static final String PREF_WARNING_OK = "warningok";
	BroadcastReceiver mReceiver;
	private TextView buttonToggle;
	private ImageView buttonToggleImg;
	private Drawable powerOnDrawable;
	private Drawable powerOffDrawable;
	private boolean changingState;

	private OnClickListener listener = new OnClickListener(){
		@Override
		public void onClick(View view) {
			switch (view.getId()){
			case R.id.btncall:
				startActivity(new Intent(Intent.ACTION_DIAL));
				break;
			case R.id.messageLabel:
				startActivity(new Intent(getApplicationContext(),
						org.servalproject.messages.MessagesListActivity.class));
				break;
			case R.id.contactsLabel:
				myPeerList();
				break;
			case R.id.settingsLabel:
				startActivity(new Intent(getApplicationContext(),
						org.servalproject.ui.SettingsScreenActivity.class));
				break;
			case R.id.sharingLabel:
				startActivity(new Intent(getApplicationContext(),
						RhizomeMain.class));
				break;
			case R.id.helpLabel:
				Intent intent = new Intent(getApplicationContext(),
						HtmlHelp.class);
				intent.putExtra("page", "helpindex.html");
				startActivity(intent);
				break;
			case R.id.servalLabel:
				startActivity(new Intent(getApplicationContext(),
						ShareUsActivity.class));
				break;
			case R.id.powerLabel:
				startActivity(new Intent(getApplicationContext(),
						Networks.class));
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.app = (ServalBatPhoneApplication) this.getApplication();
		setContentView(R.layout.main);

		// adjust the power button label on startup
		buttonToggle = (TextView) findViewById(R.id.btntoggle);
		buttonToggleImg = (ImageView) findViewById(R.id.powerLabel);
		buttonToggleImg.setOnClickListener(listener);

		// load the power drawables
		powerOnDrawable = getResources().getDrawable(
				R.drawable.ic_launcher_power);
		powerOffDrawable = getResources().getDrawable(
				R.drawable.ic_launcher_power_off);

		int listenTo[] = {
				R.id.btncall,
				R.id.messageLabel,
				R.id.mapsLabel,
				R.id.contactsLabel,
				R.id.settingsLabel,
				R.id.sharingLabel,
				R.id.helpLabel,
				R.id.servalLabel,
		};

		for (int i = 0; i < listenTo.length; i++) {
			this.findViewById(listenTo[i]).setOnClickListener(listener);
		}

		// Start the mesh service
		Intent serviceIntent = new Intent(Main.this, Control.class);
		startService(serviceIntent);

		// Go Straight to the peerlist
		// myPeerList();
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int stateOrd = intent.getIntExtra(
					ServalBatPhoneApplication.EXTRA_STATE, 0);
			State state = State.values()[stateOrd];
			stateChanged(state);
		}
	};

	boolean registered = false;

	private void stateChanged(State state) {
		changingState = false;
		buttonToggle.setText(state.getResourceId());

		// change the image for the power button
		// TODO add other drawables for other state if req'd
		switch (state) {
		case On: // set the drawable to the power on image
			buttonToggleImg.setImageDrawable(powerOnDrawable);
			break;
		default: // for every other state use the power off drawable
			buttonToggleImg.setImageDrawable(powerOffDrawable);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		checkAppSetup();
		// myPeerList();
	}

	private final int PEER_LIST_RETURN = 0;
	private void myPeerList() {

		Intent mIntent;

		mIntent = new Intent(this, org.servalproject.PeerList.class);
		startActivityForResult(mIntent, PEER_LIST_RETURN);
	}

	/**
	 * Run initialisation procedures to setup everything after install. Called
	 * from onResume() and after agreeing Warning dialog
	 */
	private void checkAppSetup() {
		State state = app.getState();
		stateChanged(state);

		if (ServalBatPhoneApplication.terminate_main) {
			ServalBatPhoneApplication.terminate_main = false;
			finish();
			return;
		}

		if (state == State.Installing || state == State.Upgrading) {
			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... arg0) {
					app.installFiles();
					return null;
				}
			}.execute();

			if (state == State.Installing) {
				this.startActivity(new Intent(this, Setup.class));
				finish();
				return;
			}
		}

		// Start by showing the preparation wizard
		// Intent prepintent = new Intent(this, PreparationWizard.class);
		// prepintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(prepintent);

		Identity main = Identity.getMainIdentity();
		if (main == null || AccountService.getAccount(this) == null
				|| main.getDid() == null) {
			Log.v("MAIN",
					"Keyring doesn't seem to be initialised, starting wizard");

			this.startActivity(new Intent(this, Setup.class));
			finish();
			return;
		}

		if (!registered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ServalBatPhoneApplication.ACTION_STATE);
			this.registerReceiver(receiver, filter);
			registered = true;
		}

		TextView pn = (TextView) this.findViewById(R.id.mainphonenumber);
		String id = "";

		if (main.getDid() != null)
			id = main.getDid();
		else
			id = main.subscriberId.abbreviation();

		pn.setText(id);

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (registered) {
			this.unregisterReceiver(receiver);
			registered = false;
		}
	}
}
