package za.co.csir.walkiemesh.ui;

import za.co.csir.walkiemesh.R;
import za.co.csir.walkiemesh.ServalBatPhoneApplication;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PeerlistHelp extends Activity {
	public ServalBatPhoneApplication app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peerlist_help);

		app = (ServalBatPhoneApplication) this.getApplication();
		Button btnMoreHelp = (Button) this.findViewById(R.id.btnMoreHelp);
		btnMoreHelp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						HelpCentralActivity.class));
			}
		});

	}

}
