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
package za.co.csir.walkiemesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import za.co.csir.Setup;
import za.co.csir.walkiemesh.ServalBatPhoneApplication.State;
import za.co.csir.walkiemesh.account.AccountService;
import za.co.csir.walkiemesh.batphone.CallHandler;
import za.co.csir.walkiemesh.servald.AbstractId.InvalidHexException;
import za.co.csir.walkiemesh.servald.AbstractJniResults;
import za.co.csir.walkiemesh.servald.IPeer;
import za.co.csir.walkiemesh.servald.IPeerListListener;
import za.co.csir.walkiemesh.servald.Identity;
import za.co.csir.walkiemesh.servald.Peer;
import za.co.csir.walkiemesh.servald.PeerComparator;
import za.co.csir.walkiemesh.servald.PeerListService;
import za.co.csir.walkiemesh.servald.ServalD;
import za.co.csir.walkiemesh.servald.SubscriberId;
import za.co.csir.walkiemesh.ui.help.BriefOverview;
import za.co.csir.walkiemesh.ui.help.HelpCentralActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 *
 * @author Jeremy Lakeman <jeremy@servalproject.org>
 *
 *         Peer List fetches a list of known peers from the PeerListService.
 *         When a peer is received from the service this activity will attempt
 *         to resolve the peer by calling ServalD in an async task.
 */
public class PeerList extends ListActivity {
	public ServalBatPhoneApplication app;
	PeerListAdapter listAdapter;
	Identity identity;

	private boolean displayed = false;
	private static final String TAG = "PeerList";

	public static final String PICK_PEER_INTENT = "za.co.csir.walkiemesh.PICK_FROM_PEER_LIST";

	public static final String CONTACT_NAME = "za.co.csir.walkiemesh.PeerList.contactName";
	public static final String CONTACT_ID = "za.co.csir.walkiemesh.PeerList.contactId";
	public static final String DID = "za.co.csir.walkiemesh.PeerList.did";
	public static final String SID = "za.co.csir.walkiemesh.PeerList.sid";
	public static final String NAME = "za.co.csir.walkiemesh.PeerList.name";
	public static final String RESOLVED = "za.co.csir.walkiemesh.PeerList.resolved";

	private boolean returnResult = false;

	List<IPeer> peers = new ArrayList<IPeer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.app = (ServalBatPhoneApplication) this.getApplication();
		checkAppSetup();
		startMeshService();

		Intent intent = getIntent();
		if (intent != null) {
			if (PICK_PEER_INTENT.equals(intent.getAction())) {
				returnResult = true;
			}
		}

		listAdapter = new PeerListAdapter(this, peers);
		listAdapter.setNotifyOnChange(false);
		this.setListAdapter(listAdapter);

		ListView lv = getListView();

		// TODO Long click listener for more options, eg text message
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				try {
					Peer p = (Peer) listAdapter.getItem(position);
					if (returnResult) {
						Log.i(TAG, "returning selected peer " + p);
						Intent returnIntent = new Intent();
						returnIntent.putExtra(
								CONTACT_NAME,
								p.getContactName());
						returnIntent.putExtra(SID, p.sid.toString());
						returnIntent.putExtra(CONTACT_ID, p.contactId);
						returnIntent.putExtra(DID, p.did);
						returnIntent.putExtra(NAME, p.name);
						returnIntent.putExtra(RESOLVED,
								p.cacheUntil > SystemClock.elapsedRealtime());
						setResult(Activity.RESULT_OK, returnIntent);
						finish();
					} else {
						Log.i(TAG, "calling selected peer " + p);
						CallHandler.dial(p);
					}
				} catch (Exception e) {
					ServalBatPhoneApplication.context.displayToastMessage(e
							.getMessage());
					Log.e("BatPhone", e.getMessage(), e);
				}
			}
		});

		boolean firstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
				.getBoolean("isFirstRun", true);

		if (firstRun) {

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						startActivity(new Intent(getApplicationContext(),
								BriefOverview.class));
						getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
								.putBoolean("isFirstRun", false).commit();
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
								.putBoolean("isFirstRun", false).commit();
						break;

					default:
						break;
					}

				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Do you want to see a brief overview of the app?")
					.setPositiveButton("Yes", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();

		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent != null) {
			if (PICK_PEER_INTENT.equals(intent.getAction())) {
				returnResult = true;
			}
		}
	}

	private void peerUpdated(IPeer p) {
		if (!peers.contains(p))
			peers.add(p);
		Collections.sort(peers, new PeerComparator());
		listAdapter.notifyDataSetChanged();
	}

	private IPeerListListener listener = new IPeerListListener() {
		@Override
		public void peerChanged(final Peer p) {

			// if we haven't seen recent active network confirmation for the
			// existence of this peer, don't add to the UI
			if (p.sid.isBroadcast() || !p.stillAlive())
				return;

			if (p.cacheUntil <= SystemClock.elapsedRealtime())
				resolve(p);

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					peerUpdated(p);
				};

			});
		}
	};

	ConcurrentMap<SubscriberId, Peer> unresolved = new ConcurrentHashMap<SubscriberId, Peer>();

	private boolean searching = false;

	private void search() {
		if (searching)
			return;
		searching = true;

		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				while (!unresolved.isEmpty()) {
					for (Peer p : unresolved.values()) {
						PeerListService.resolve(p);
						unresolved.remove(p.sid);
					}
				}
				searching = false;
				return null;
			}
		}.execute();
	}

	private synchronized void resolve(Peer p) {
		if (!displayed)
			return;

		unresolved.put(p.sid, p);
		search();
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (registered) {
			this.unregisterReceiver(receiver);
			registered = false;
		}

		PeerListService.removeListener(listener);
		Control.peerList = null;
		displayed = false;
		unresolved.clear();
		peers.clear();
		listAdapter.notifyDataSetChanged();
	}

	public void monitorConnected() {
		this.refresh();
	}

	private synchronized void refresh() {
		final long now = SystemClock.elapsedRealtime();
		ServalD.peers(new AbstractJniResults() {

			@Override
			public void putBlob(byte[] val) {
				try {
					if (!displayed)
						return;

					String value = new String(val);
					SubscriberId sid = new SubscriberId(value);
					PeerListService.peerReachable(getContentResolver(),
							sid, true);

					final Peer p = PeerListService.getPeer(
							getContentResolver(), sid);
					p.lastSeen = now;

					if (p.cacheUntil <= SystemClock.elapsedRealtime())
						unresolved.put(p.sid, p);

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							peerUpdated(p);
						};

					});

				} catch (InvalidHexException e) {
					Log.e(TAG, e.toString(), e);
				}
			}
		});

		if (!displayed)
			return;

		for (Peer p : PeerListService.peers.values()) {
			if (p.lastSeen < now)
				PeerListService.peerReachable(getContentResolver(),
						p.sid, false);
		}

		if (!unresolved.isEmpty())
			search();
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkAppSetup();

		displayed = true;
		Control.peerList = this;

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				refresh();
				PeerListService.addListener(PeerList.this, listener);
				return null;
			}

		}.execute();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		if (app.getState() == State.Off) {
			menu.findItem(R.id.menu_toggleMesh).setTitle("Connect");
			menu.findItem(R.id.menu_toggleMesh).setIcon(R.drawable.play);
		} else {
			menu.findItem(R.id.menu_toggleMesh).setTitle("Disconnect");
			menu.findItem(R.id.menu_toggleMesh).setIcon(R.drawable.pause);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_toggleMesh:
			if (app.getState() == State.Off)
				startMeshService();
			else
				stopMeshService();
			return true;
		case R.id.menu_exit:
			stopMeshService();
			finish();
			return true;
		case R.id.menu_settings:
			startActivity(new Intent(getApplicationContext(),
					za.co.csir.walkiemesh.ui.SettingsScreenActivity.class));
			return true;
			// case R.id.menu_network:
			// startActivity(new Intent(getApplicationContext(),
			// Networks.class));
			// return true;
		case R.id.menu_messages:
			startActivity(new Intent(getApplicationContext(),
					za.co.csir.walkiemesh.messages.MessagesListActivity.class));
			return true;
		case R.id.menu_help:
			Intent help = new Intent(getApplicationContext(),
					HelpCentralActivity.class);
			help.putExtra("showButton", true);
			startActivity(help);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int stateOrd = intent.getIntExtra(
					ServalBatPhoneApplication.EXTRA_STATE, 0);
			State state = State.values()[stateOrd];
			stateChanged(state);
			if (Build.VERSION.SDK_INT >= 11)
				invalidateOptionsMenu();
		}
	};

	boolean registered = false;

	private void stateChanged(State state) {
		Log.i("State", "new State: " + app.getState().toString());
	}

	private final int PEER_LIST_RETURN = 0;

	/**
	 * Run initialisation procedures to setup everything after install. Called
	 * from onResume()
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
				doAppSetup();
				return;
			}
		}

		Identity main = Identity.getMainIdentity();
		if (main == null || AccountService.getAccount(this) == null
				|| main.getDid() == null) {
			Log.v(TAG,
					"Keyring doesn't seem to be initialised, starting wizard");

			doAppSetup();
			return;
		}

		if (!registered) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(ServalBatPhoneApplication.ACTION_STATE);
			this.registerReceiver(receiver, filter);
			registered = true;
		}
	}

	private void doAppSetup() {
		this.startActivity(new Intent(this, Setup.class));
		finish();
	}

	private void startMeshService() {
		if (app.getState() == State.Off) {
			Intent serviceIntent = new Intent(PeerList.this, Control.class);
			startService(serviceIntent);
		} else
			Log.i(TAG, "Trying to start service from invalid state: "
					+ app.getState());
	}

	private void stopMeshService() {
		if (app.getState() == State.On) {
			Intent serviceIntent = new Intent(PeerList.this, Control.class);
			stopService(serviceIntent);
		} else
			Log.i(TAG,
					"Trying to stop service from invalid state: "
							+ app.getState());
	}
}
