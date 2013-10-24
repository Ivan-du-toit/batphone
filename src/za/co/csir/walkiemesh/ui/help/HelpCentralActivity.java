package za.co.csir.walkiemesh.ui.help;

import za.co.csir.walkiemesh.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpCentralActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help_central);

		Button btnSettingsHelp = (Button) this
				.findViewById(R.id.btnSettingsHelp);
		btnSettingsHelp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						HelpActivity.class));
			}
		});

		Button btnOverviewHelp = (Button) this
				.findViewById(R.id.btnOverviewHelp);
		btnOverviewHelp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						BriefOverview.class));
			}
		});

		((Button) this.findViewById(R.id.btnPeerListHelp))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(getApplicationContext(),
								PeerlistHelp.class));
					}
				});

		((Button) this.findViewById(R.id.btnTroubleHelp))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						startActivity(new Intent(getApplicationContext(),
								TroubleShooting.class));
					}
				});

	}

}
