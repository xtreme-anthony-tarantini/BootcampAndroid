package bootcamp.android.twieber.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//import android.util.Log;

public class TwieberDatabase extends SQLiteOpenHelper {
	//private static final String TAG = TwieberDatabase.class.getName();
	
	//Database Constants
	public static final String DB_NAME = "twieber";
	public static final int DB_VERSION = 1;
	public static final String DB_CREATE = "CREATE TABLE " + TwieberContentDescriptor.TweetDesc.DB_TABLE + " (" +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID + " INTEGER PRIMARY KEY NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_HASHTAG + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_USERNAME + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_IMAGE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_MESSAGE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_DATE + " TEXT NOT NULL, " +
			TwieberContentDescriptor.TweetDesc.Columns.KEY_SOURCE + " TEXT NOT NULL, " +
			"UNIQUE (" + TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID + ") ON CONFLICT IGNORE)";
	
	//Class Constructor
	public TwieberDatabase(final Context context){
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	//On Creation of Database -> Create new Table
	@Override
	public void onCreate(final SQLiteDatabase db){
		//Log.d(TAG, "onCreate");
		db.execSQL(DB_CREATE);
	}
	
	//On Upgrade of Database -> Drop old table, create new table
	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion){
		//Log.d(TAG, "onUpgrade");
		if (oldVersion < newVersion){
			db.execSQL("DROP TABLE IF EXISTS " + TwieberContentDescriptor.TweetDesc.DB_TABLE);
			onCreate(db);
		}
	}
}
	