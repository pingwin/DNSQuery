package com.comwired.dnsquery;

import android.app.ListActivity;
import android.view.Menu;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ServerList extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;
	private static final int ACTIVITY_EDIT   = 1;
	private static final int ACTIVITY_REMOVE = 2;
	
	private static final int INSERT_ID = Menu.FIRST;
	private static final int DELETE_ID = Menu.FIRST + 1;
	
	private ServerDbAdapter mDbHelper;
	private Cursor mServerCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_list);
		mDbHelper = new ServerDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
		
	}
	
	private void fillData() {
		mServerCursor = mDbHelper.fetchAllResolvers();
		
		if (mServerCursor == null) {
    		Toast.makeText(this, "error: resolver cursor is null", Toast.LENGTH_LONG).show();
    		return;
    	}
		
		startManagingCursor(mServerCursor);
		
		SimpleCursorAdapter servers = new SimpleCursorAdapter(
				this,
				R.layout.server_row,
				mServerCursor,
				new String[]{ServerDbAdapter.KEY_DISPLAY},
				new int[]{R.id.text1}
				);
		setListAdapter(servers);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, INSERT_ID, 0, R.string.menu_insert);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch(item.getItemId()) {
		case INSERT_ID:
			createServer();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case DELETE_ID:
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			mDbHelper.deleteResolver(info.id);
			fillData();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void createServer() {
		Intent i = new Intent(this, ResolverEdit.class);
		startActivityForResult(i, ACTIVITY_CREATE);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Cursor c = mServerCursor;
		c.moveToPosition(position);
		Intent i = new Intent(this, ResolverEdit.class);
		i.putExtra(ServerDbAdapter.KEY_ROWID, id);
		i.putExtra(ServerDbAdapter.KEY_DISPLAY, c.getString(c.getColumnIndexOrThrow(ServerDbAdapter.KEY_DISPLAY)));
		i.putExtra(ServerDbAdapter.KEY_ADDRESS, c.getString(c.getColumnIndexOrThrow(ServerDbAdapter.KEY_ADDRESS)));
		startActivityForResult(i, ACTIVITY_EDIT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			Bundle extras = intent.getExtras();
			switch(requestCode) {
			case ACTIVITY_CREATE:
				String display = extras.getString(ServerDbAdapter.KEY_DISPLAY);
				String address = extras.getString(ServerDbAdapter.KEY_ADDRESS);
				mDbHelper.createResolver(display, address);
				break;
			case ACTIVITY_EDIT:
				Long rowId = extras.getLong(ServerDbAdapter.KEY_ROWID);
				if (rowId != null) {
					String editDisplay = extras.getString(ServerDbAdapter.KEY_DISPLAY);
					String editAddress = extras.getString(ServerDbAdapter.KEY_ADDRESS);
					mDbHelper.updateResolver(rowId, editDisplay, editAddress);
				}
				break;
			}
		}
		fillData();
	}
}
