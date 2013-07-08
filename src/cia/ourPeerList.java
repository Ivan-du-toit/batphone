package cia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ourPeerList extends Activity {

	private final int PEER_LIST_RETURN = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Intent mIntent;

		super.onCreate(savedInstanceState);

		mIntent = new Intent(this, org.servalproject.PeerList.class);
		startActivityForResult(mIntent, PEER_LIST_RETURN);

	}

	// ArrayList<Peer> pl = new ArrayList<Peer>();

}
