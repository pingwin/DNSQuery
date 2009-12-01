package com.comwired.dnsquery;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class ResolverEdit extends Activity {
	private EditText mDisplayText;
	private EditText mAddressText;
	private Long mRowId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resolver_edit);
		
		mDisplayText = (EditText) findViewById(R.id.display);
		mAddressText = (EditText) findViewById(R.id.address);
		
		Button confirmButton = (Button) findViewById(R.id.confirm);
		
		mRowId = null;
		Bundle extras = getIntent().getExtras();
		
		if (extras != null) {
			String display = extras.getString(ServerDbAdapter.KEY_DISPLAY);
			String address = extras.getString(ServerDbAdapter.KEY_ADDRESS);
			mRowId = extras.getLong(ServerDbAdapter.KEY_ROWID);
			
			if (display != null)
				mDisplayText.setText(display);
			if (address != null)
				mAddressText.setText(address);
		}
		
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Bundle bundle = new Bundle();
				
				bundle.putString(ServerDbAdapter.KEY_DISPLAY, mDisplayText.getText().toString());
				bundle.putString(ServerDbAdapter.KEY_ADDRESS, mAddressText.getText().toString());
				if (mRowId != null)
					bundle.putLong(ServerDbAdapter.KEY_ROWID, mRowId);
				
				Intent i = new Intent();
				i.putExtras(bundle);
				setResult(RESULT_OK, i);
				finish();
			}
		});
	}
}
