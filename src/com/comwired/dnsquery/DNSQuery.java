package com.comwired.dnsquery;

import android.app.Activity;
import android.widget.SimpleCursorAdapter;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.text.Editable;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
// for logging
import android.util.Log;
// for dns lookup
import org.xbill.DNS.*;
import java.io.IOException;

// for menu
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;

public class DNSQuery extends Activity {
	private static final String TAG = "DNSQuery";
	private EditText mResolver;
	private Button goButton;
	private Spinner mServer;
	
	public static final int MENU_EDIT_SERVER_LIST = Menu.FIRST;
	
	private ServerDbAdapter mDbHelper;
	private Cursor mServerCursor;

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mResolver = (EditText)findViewById(R.id.server);
        goButton  = (Button)  findViewById(R.id.go);
        
        final EditText mQuestionText = (EditText) findViewById(R.id.query);
        final TextView mResultView   = (TextView) findViewById(R.id.result_view);
        final Spinner  mQtype = (Spinner)findViewById(R.id.qtype);
        
        mQuestionText.setOnClickListener(new EditText.OnClickListener() {
        	public void onClick(View v) {
        		mQuestionText.setText("");
        	}
        });
        
        mResolver.setOnClickListener(new EditText.OnClickListener() {
        	public void onClick(View v) {
        		mResolver.setText("");
        	}
        });
        
        goButton.setOnClickListener(new Button.OnClickListener() {
        	public void onClick(View v) {
        		goButton.setClickable(false);
        		performLookup(
        				mResultView,
        				mQuestionText.getText().toString(),
        				getResources().getIntArray(R.array.rr_types_int)[mQtype.getSelectedItemPosition()],
        				mResolver.getText().toString()
        				);
        	}
        });
        
        mDbHelper = new ServerDbAdapter(this);
        mDbHelper.open();
        
        fillServerSpinner();
        
        Toast.makeText(getApplicationContext(), "This tool is provided free by Comwired.com the hot new geodirectional managed DNS service provider.", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_EDIT_SERVER_LIST, 0, "Edit Server List");
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case MENU_EDIT_SERVER_LIST:
    		Intent i = new Intent(this, ServerList.class);
    		startActivity(i);
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
    
    private void fillServerSpinner() {
    	// get all the rows from the database and create the item list
    	mServerCursor = mDbHelper.fetchAllResolvers();
    	if (mServerCursor == null) {
    		Toast.makeText(this, "error: resolver cursor is null", Toast.LENGTH_SHORT);
    		return;
    	}
   		startManagingCursor(mServerCursor);
   		
    	SimpleCursorAdapter server_adapter = new SimpleCursorAdapter(
    			this,
    			android.R.layout.simple_spinner_item,
    			mServerCursor,
    			new String[]{ServerDbAdapter.KEY_DISPLAY},
    			new int[]{android.R.id.text1}
    			);
    	
    	server_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	Spinner spinner = (Spinner)findViewById(R.id.server_spinner);
    	spinner.setAdapter(server_adapter);
    	
    	
    	spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
    		public void onItemSelected(AdapterView parent, View v, int pos, long id) {
    			mServerCursor.moveToPosition(pos);
    			mResolver.setText(mServerCursor.getString(mServerCursor.getColumnIndex(ServerDbAdapter.KEY_ADDRESS)));
    		}
    		public void onNothingSelected(AdapterView arg0){}
    	});
    }
    
    protected void performLookup(TextView result, String question, int qtype, String resolver) {
    	// cls
    	result.setText("");
    	
    	if (question.length() <= 3) {
    		Toast.makeText(this, "error: Query must be at least 3 chars or more", Toast.LENGTH_SHORT);
    		goButton.setClickable(true);
    		return;
    	}
    	
    	if (resolver.length() <= 3)
    		resolver = "208.67.222.222"; /* opendns */
    	
    	// display what we are looking for and where
    	result.append("Lookup of: "+question+" @"+resolver+" Type: "+qtype+"\n\n");
    	
    	Cache cache = new Cache();
    	
    	
    	try {
    		Lookup lu = new Lookup(question, qtype);
    		
    		SimpleResolver res = new SimpleResolver(resolver);
    		
    		res.setTCP(false);
    		
    		lu.setCache(cache);
    		lu.setResolver(res);
    		
    		Record[] records = lu.run();
    		
    		result.append("Answers: "+records.length+"\n");
    		
    		for (int i=0; i<records.length; i ++) {
    			switch(qtype) {
    			case 15: // mx
    				MXRecord mx = (MXRecord)records[i];
    				result.append("Host: "+mx.getTarget()+" Priority: "+mx.getPriority());
    				break;
    			default:
    				result.append(records[i].toString());
    				break;
    			}
    			result.append("\n");
    		}
    		
    	} catch (org.xbill.DNS.TextParseException err) {
    		result.append("TextParseException: "+err.toString());
    	} catch (java.net.UnknownHostException err) {
    		result.append("Resolver not found: "+err.toString());
    	} catch (Exception err) {
    		result.append("UnkownException: "+err.getMessage());
    	}
    	
    	cache.clearCache();
    	goButton.setClickable(true);
    }
}