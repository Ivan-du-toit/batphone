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

package org.servalproject.wizard;

import java.util.List;

import org.servalproject.Main;
import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.account.AccountService;
import org.servalproject.servald.Identity;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Wizard extends Activity {
	ServalBatPhoneApplication app;

	Identity identity;
	Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = (ServalBatPhoneApplication) this.getApplication();

		setContentView(R.layout.wizard);

		button = (Button) this.findViewById(R.id.btnwizard);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setEnabled(false);

				new AsyncTask<Void, Void, Boolean>() {

					@Override
					protected Boolean doInBackground(Void... params) {
						try {
							identity.setDetails(app, getNumber(), "");// name.getText().toString()

							// create the serval android acount if it doesn't
							// already exist
							Account account = AccountService
									.getAccount(Wizard.this);
							if (account == null) {
								account = new Account("Serval Mesh",
										AccountService.TYPE);
								AccountManager am = AccountManager
										.get(Wizard.this);

								if (!am.addAccountExplicitly(account, "", null))
									throw new IllegalStateException(
											"Failed to create account");

								Intent ourIntent = Wizard.this.getIntent();
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
							app.displayToastMessage(e.getMessage());
						} catch (Exception e) {
							Log.e("BatPhone", e.getMessage(), e);
							app.displayToastMessage(e.getMessage());
						}
						return false;
					}

					@Override
					protected void onPostExecute(Boolean result) {
						if (result) {
							Intent intent = new Intent(Wizard.this,
									Main.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							Wizard.this.startActivity(intent);
							Wizard.this.setResult(RESULT_OK);
							Wizard.this.finish();
							return;
						}
						button.setEnabled(true);
					}
				}.execute((Void[]) null);

				// startActivityForResult(new Intent(Wizard.this,
				// SetPhoneNumber.class), 0);
			}
		});
	}

	private String getNumber() {

		List<Identity> identities = Identity.getIdentities();
		TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String existingNumber = null;
		if (identities.size() > 0) {
			identity = identities.get(0);

			existingNumber = identity.getDid();
			if (existingNumber == null) {
				existingNumber = mTelephonyMgr.getLine1Number();
			}
			if (existingNumber == null) {
				existingNumber = mTelephonyMgr.getDeviceId();
			}
		}
		else {
			// try to get number from phone, probably wont work though...
			existingNumber = mTelephonyMgr.getLine1Number();
			if (existingNumber == null) {
				existingNumber = mTelephonyMgr.getDeviceId();
			}
			try {
				identity = Identity.createIdentity();
			} catch (Exception e) {
				Log.e("getNumber", e.getMessage(), e);
				app.displayToastMessage(e.getMessage());
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
}
