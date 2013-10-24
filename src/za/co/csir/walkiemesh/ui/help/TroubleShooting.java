package za.co.csir.walkiemesh.ui.help;

import za.co.csir.walkiemesh.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TroubleShooting extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trouble_shooting_guide);

		Button btnMoreHelp = (Button) this.findViewById(R.id.btnMoreHelp);
		btnMoreHelp.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						HelpCentralActivity.class));
				finish();
			}
		});
	}

}
