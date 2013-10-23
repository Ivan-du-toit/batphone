package za.co.csir.walkiemesh.ui;

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

	}

}
