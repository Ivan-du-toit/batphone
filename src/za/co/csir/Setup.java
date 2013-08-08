package za.co.csir;

import java.util.List;


import za.co.csir.walkiemesh.PeerList;
import za.co.csir.walkiemesh.R;
import za.co.csir.walkiemesh.ServalBatPhoneApplication;
import za.co.csir.walkiemesh.account.AccountService;
import za.co.csir.walkiemesh.servald.Identity;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ProgressBar;

public class Setup extends Activity {
	ServalBatPhoneApplication app;
	private static final String TAG = "Setup";
	Identity identity;
	ProgressBar busy;
	private static final long DELAY = 5000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (ServalBatPhoneApplication) this.getApplication();
		setContentView(R.layout.wizard);

		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {

					while (!sleep()) {
						Log.i("WTF", "sleeping");
					}
					String did = getNumber();
					Log.i("Setup", "DID: " + did);
					String name = getName();
					Log.i("Setup", "Name: " + name);
					if (app == null)
						Log.i("Setup", "App is null");
					if (identity == null)
						Log.i("Setup", "Identity is null");
					identity.setDetails(app, did, name);

					// create the serval android acount if it doesn't
					// already exist
					Account account = AccountService
							.getAccount(Setup.this);
					if (account == null) {
						account = new Account("Serval Mesh",
								AccountService.TYPE);
						AccountManager am = AccountManager
								.get(Setup.this);

						if (!am.addAccountExplicitly(account, "", null))
							throw new IllegalStateException(
									"Failed to create account");

						Intent ourIntent = Setup.this.getIntent();
						if (ourIntent != null
								&& ourIntent.getExtras() != null) {
							AccountAuthenticatorResponse response = ourIntent
									.getExtras()
									.getParcelable(
											AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
							if (response != null) {
								Bundle result = new Bundle();
								result.putString(
										AccountManager.KEY_ACCOUNT_NAME,
										account.name);
								result.putString(
										AccountManager.KEY_ACCOUNT_TYPE,
										AccountService.TYPE);
								response.onResult(result);
							}
						}
					}

					return true;
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e.getMessage(), e);
					// app.displayToastMessage(e.getMessage());
				} catch (Exception e) {
					Log.e(TAG, e.getMessage(), e);
					// app.displayToastMessage(e.getMessage());
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				if (result) {
					Intent intent = new Intent(Setup.this,
							PeerList.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					Setup.this.startActivity(intent);
					Setup.this.setResult(RESULT_OK);
					Setup.this.finish();
					return;
				}
				Log.e(TAG, "Invalid result");
				app.displayToastMessage("The setup failed unexpectedly please try running the app again.");
			}
		}.execute((Void[]) null);
	}

	private String getName() {
		// Just return empty for now should use multiple fallback methods for
		// deriving a name
		// if (!ContactsContract.Profile.DISPLAY_NAME.equals("display_name"))
		// return ContactsContract.Profile.DISPLAY_NAME;
		try {
			AccountManager accountMan = AccountManager.get(app);
			Account[] accounts = accountMan.getAccountsByType("com.google");
			return accountMan.getUserData(accounts[0],
					AccountManager.KEY_USERDATA);
		} catch (Exception e) {
			Log.i(TAG, "Could not get account name");
		}
		return "";
	}

	private String getNumber() {
		Log.i("WTF", "Getting num");
		List<Identity> identities = Identity.getIdentities();
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String existingNumber = null;
		if (identities.size() > 0) {
			identity = identities.get(0);
			Log.i("WTF", "Exiting Ident");
			existingNumber = identity.getDid();
			if (existingNumber == null) {
				existingNumber = mTelephonyMgr.getLine1Number();
			}
			if (existingNumber == null || existingNumber.equals("")) {
				existingNumber = mTelephonyMgr.getDeviceId();
			}
		}
		else {
			Log.i("WTF", "No Ident");
			// try to get number from phone, probably wont work though...
			existingNumber = mTelephonyMgr.getLine1Number();
			if (existingNumber == null || existingNumber.equals("")) {
				existingNumber = mTelephonyMgr.getDeviceId();
			}
			try {
				identity = Identity.createIdentity();
			} catch (Exception e) {
				Log.e("Setup", e.getMessage(), e);
				// app.displayToastMessage(e.getMessage());
			}
		}
		return existingNumber;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
	}

	private boolean sleep() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
}
