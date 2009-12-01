/**
 * 
 */
package com.comwired.dnsquery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author pingwin
 *
 */
public class ServerDbAdapter {
	public static final String KEY_DISPLAY = "display_name";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_ROWID   = "_id";
	
	private static final String TAG = "ServerDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	/**
	 * Database creation sql statement
	 */
	private static final String DATABASE_NAME = "dnsquery";
	private static final String DATABASE_TABLE = "resolvers";
	private static final int DATABASE_VERSION = 2;
	
	private static final String DATABASE_CREATE = 
		"CREATE TABLE "+DATABASE_TABLE+" ("+KEY_ROWID+" integer primary key autoincrement, "
		+ KEY_ADDRESS+" text not null, "+KEY_DISPLAY+" text not null);";
	private static final String DATABASE_INSERT = 
		"INSERT INTO "+DATABASE_TABLE+" ("+KEY_DISPLAY+", "+KEY_ADDRESS+") VALUES ";
	
	
	private final Context mCtx;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
			
			db.execSQL(DATABASE_INSERT+"(\"Verizon\", \"4.2.2.1\")");
			db.execSQL(DATABASE_INSERT+"(\"OpenDNS\", \"208.67.222.222\")");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data.");
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
			onCreate(db);
		}	
	}
	
	/**
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx the Context within which to work
	 */
	public ServerDbAdapter(Context ctx) {
		this.mCtx = ctx;
	}
	
	/**
	 * Open the notes database. If it cannot be opened, try to create a new
	 * instance of the database. If it cannot be created, throw an exception to
	 * signal the failure
	 * 
	 * @return this
	 * @throws SQLException
	 */
	public ServerDbAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public long createResolver(String display_name, String address) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_DISPLAY, display_name);
		initialValues.put(KEY_ADDRESS, address);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}
	
	public boolean deleteResolver(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
	public Cursor fetchAllResolvers() {
		return mDb.query(DATABASE_TABLE, null, null, null, null, null, KEY_DISPLAY);
	}
	
	public Cursor fetchResolver(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
				KEY_DISPLAY, KEY_ADDRESS}, KEY_ROWID + "=" + rowId, null,
				null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	public boolean updateResolver(long rowId, String display, String address) {
		ContentValues args = new ContentValues();
		args.put(KEY_DISPLAY, display);
		args.put(KEY_ADDRESS, address);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}
}
