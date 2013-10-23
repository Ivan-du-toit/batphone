package za.co.csir.walkiemesh.ui;

import za.co.csir.walkiemesh.R;
import za.co.csir.walkiemesh.R.layout;
import za.co.csir.walkiemesh.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class PeerlistHelp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peerlist_help);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peerlist_help, menu);
		return true;
	}

}
