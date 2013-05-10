package bootcamp.android.twieber.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class TwieberDatabase extends SQLiteOpenHelper {
	//private static final String TAG = TwieberDatabase.class.getName();
	
	//Database Constants
	public static final String DB_NAME = "twieber_database";
	public static final int DB_VERSION = 1;
	public static final String DB_CREATE_TWEET = "CREATE TABLE " + TwieberContentDescriptor.TweetDesc.DB_TABLE + " (" +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID + " INTEGER PRIMARY KEY NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_HASHTAG + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_USERNAME + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_IMAGE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_MESSAGE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_DATE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_SOURCE + " TEXT NOT NULL, " +
			"UNIQUE (" + TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID + ") ON CONFLICT IGNORE)";
	public static final String DB_CREATE_HASH = "CREATE TABLE " + TwieberContentDescriptor.HashDesc.DB_TABLE + " (" +
			TwieberContentDescriptor.HashDesc.Columns.KEY_HASHID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
			TwieberContentDescriptor.HashDesc.Columns.KEY_HASHTAG + " TEXT NOT NULL, " +
			TwieberContentDescriptor.HashDesc.Columns.KEY_MAXID + " TEXT NOT NULL, " +
			TwieberContentDescriptor.HashDesc.Columns.KEY_DATE + " INTEGER NOT NULL DEFAULT current_timestamp)";
			
	//Class Constructor
	public TwieberDatabase(final Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	//On Creation of Database -> Create new Table
	@Override
	public void onCreate(final SQLiteDatabase db){
		//Log.d(TAG, "onCreate");
		db.execSQL(DB_CREATE_TWEET);
		db.execSQL(DB_CREATE_HASH);
	}
	
	//On Upgrade of Database -> Drop old table, create new table
	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion){
		//Log.d(TAG, "onUpgrade");
		if (oldVersion < newVersion){
			db.execSQL("DROP TABLE IF EXISTS " + TwieberContentDescriptor.TweetDesc.DB_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + TwieberContentDescriptor.HashDesc.DB_TABLE);
			onCreate(db);
		}
	}
}
	