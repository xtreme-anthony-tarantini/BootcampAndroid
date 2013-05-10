package bootcamp.android.twieber.contentprovider;

import android.content.ContentValues;
import android.content.ContentProvider;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
//import android.util.Log;

public class TwieberContentProvider extends ContentProvider {
	//private static final String TAG = TwieberContentProvider.class.getName();
	
	//Private reference to database object
	private TwieberDatabase twieberDB;
	
	//On creation of provider -> get new database for this context
	@Override
	public boolean onCreate() {
		twieberDB = new TwieberDatabase(getContext());
		return true;
	}

	//Returns the MIME type of the data at requested URI
	@Override
	public String getType(Uri uri) {
		final int match = TwieberContentDescriptor.URI_MATCHER.match(uri);
		switch(match){
		case TwieberContentDescriptor.TweetDesc.PATH_TOKEN:
			return TwieberContentDescriptor.TweetDesc.CONTENT_TYPE_DIR;
		case TwieberContentDescriptor.TweetDesc.PATH_FOR_ID_TOKEN:
			return TwieberContentDescriptor.TweetDesc.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException ("URI " + uri + " is not supported.");
		}
		
	}

	//Inserts the given values into the database at the given URI
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase insertDB = twieberDB.getWritableDatabase();
		int token = TwieberContentDescriptor.URI_MATCHER.match(uri);
		long id;
		switch(token){
		case TwieberContentDescriptor.TweetDesc.PATH_TOKEN:
			id = insertDB.insert(TwieberContentDescriptor.TweetDesc.DB_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return TwieberContentDescriptor.TweetDesc.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		case TwieberContentDescriptor.HashDesc.PATH_TOKEN:
			id = insertDB.insert(TwieberContentDescriptor.HashDesc.DB_TABLE, null, values);
			getContext().getContentResolver().notifyChange(uri, null);
			return TwieberContentDescriptor.HashDesc.CONTENT_URI.buildUpon().appendPath(String.valueOf(id)).build();
		default:
			throw new UnsupportedOperationException("URI: " + uri + " not supported.");
		}
	}

	//Queries the database at the given URI with the given information
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteDatabase queryDB = twieberDB.getReadableDatabase();
		final int match =  TwieberContentDescriptor.URI_MATCHER.match(uri);
		SQLiteQueryBuilder builder;
		switch(match){
		case TwieberContentDescriptor.TweetDesc.PATH_TOKEN:
			builder = new SQLiteQueryBuilder();
			builder.setTables(TwieberContentDescriptor.TweetDesc.DB_TABLE);
			return builder.query(queryDB, projection, selection, selectionArgs, null, null, sortOrder);
		case TwieberContentDescriptor.HashDesc.PATH_TOKEN:
			builder = new SQLiteQueryBuilder();
			builder.setTables(TwieberContentDescriptor.HashDesc.DB_TABLE);
			return builder.query(queryDB, projection, selection, selectionArgs, null, null, sortOrder);
		default:
			return null;
		}
	}
	
	//Delete not needed in this application
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	//Update maxID and date in the HashTable
	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
		SQLiteDatabase updateDB = twieberDB.getWritableDatabase();
		final int match = TwieberContentDescriptor.URI_MATCHER.match(uri);
		long id;
		switch(match){
		case TwieberContentDescriptor.HashDesc.PATH_TOKEN:
			id = updateDB.update(TwieberContentDescriptor.HashDesc.DB_TABLE, values, whereClause, whereArgs);
			getContext().getContentResolver().notifyChange(uri, null);
			return (int) id;
		default:
			return 0;
		}
	}

}
