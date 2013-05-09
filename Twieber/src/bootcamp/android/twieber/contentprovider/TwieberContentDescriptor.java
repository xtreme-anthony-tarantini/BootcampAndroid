package bootcamp.android.twieber.contentprovider;

import android.content.UriMatcher;
import android.net.Uri;

//import android.util.Log;

public class TwieberContentDescriptor {
	//private static final String TAG = TwieberContentDescriptor.class.getName();
	
	//Database URI constants
	public static final String AUTHORITY = "bootcamp.android.twieber.contentprovider";
	private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
	public static final UriMatcher URI_MATCHER = buildUriMatcher();
	
	//Creating the URIMatcher for the application
	private static UriMatcher buildUriMatcher(){
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = AUTHORITY;
		
		//Adding the URIs for the table and for a single row
		matcher.addURI(authority, TweetDesc.PATH, TweetDesc.PATH_TOKEN);
		matcher.addURI(authority, TweetDesc.PATH_FOR_ID, TweetDesc.PATH_FOR_ID_TOKEN);
		
		return matcher;
	}
	
	//Describing class for the Tweet model, contains all constants pertaining to this data object
	public static class TweetDesc {		
		public static final String DB_TABLE = "tweet";
		
		public static final String PATH = "tweets";
		public static final int PATH_TOKEN = 100;
		public static final String PATH_FOR_ID = "tweets/*";
		public static final int PATH_FOR_ID_TOKEN = 200;
		
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH).build();
		
		public static final String CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.twieber.app";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.twieber.app";
		
		//Describing class for Columns inside the Tweet model
		public static class Columns{
			public static final String KEY_TWEETID = "_id";
			public static final String KEY_HASHTAG = "hashtag";
			public static final String KEY_USERNAME = "username";
			public static final String KEY_IMAGE = "image";
			public static final String KEY_MESSAGE = "message";
			public static final String KEY_DATE = "date";
			public static final String KEY_SOURCE = "source";
		}
	}
}
