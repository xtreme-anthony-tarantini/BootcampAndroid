package bootcamp.android.twieber.service;

import bootcamp.android.twieber.activity.TwieberActivity.UpdateReceiver;
import bootcamp.android.twieber.contentprovider.TwieberContentDescriptor;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.StatusLine;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.content.ContentValues;
import android.util.Log;
import android.database.Cursor;

public class TwieberService extends IntentService {
	private static final String TAG = TwieberService.class.getName();
		
	//Declaring constants for messaging with Activity
	public static final String HASH_TAG_IN = "hmsg";
	public static final String MAX_ID_IN = "imsg";
	public static final String NEW_TWEETS_OUT = "nmsg";
	public static final String MAX_ID_OUT = "amsg";
	
	//Variables for comparing whether an insert is needed into the database
	private String maxID;
	private String newMaxID;
	private Cursor cursor;
	
	//Constructor for the service
	public TwieberService() {
		super("TwieberService");
	}	
	@Override
	protected void onHandleIntent(Intent intent) {
		//Extract information from intent
		String msg = intent.getStringExtra(HASH_TAG_IN);
		maxID = intent.getStringExtra(MAX_ID_IN);
		//Create query
		String apiQuery = getQuery(msg);
		Log.d(TAG, apiQuery);
		StringBuilder tweetFeedBuilder = new StringBuilder();
		try{
			//Begin forming HTTP request
			HttpClient tweetClient = new DefaultHttpClient();
			HttpGet tweetGet = new HttpGet(apiQuery);
			HttpResponse tweetResponse = tweetClient.execute(tweetGet);
			StatusLine searchStatus = tweetResponse.getStatusLine();
			//If response received is OK
			if (searchStatus.getStatusCode() == 200){
				//Begin building actual data from response
				HttpEntity tweetEntity = tweetResponse.getEntity();
				InputStream tweetContent = tweetEntity.getContent();
				InputStreamReader tweetInput = new InputStreamReader(tweetContent);
				BufferedReader tweetReader = new BufferedReader(tweetInput);
				String lineIn;
				while((lineIn = tweetReader.readLine()) != null){
					tweetFeedBuilder.append(lineIn);
				}
				insertTweet(tweetFeedBuilder, msg);
			}
		} catch(Exception e){}
		updateHash(msg, newMaxID);
		sendIntent();
	}
	
	//Queries for latest maxID if not expired
	private String getQuery(String hashtag){
		String storedMaxID;
		String query = "http://search.twitter.com/search.json";
		String[] projection = new String[]{
				TwieberContentDescriptor.HashDesc.Columns.KEY_HASHID,
				TwieberContentDescriptor.HashDesc.Columns.KEY_HASHTAG,
				TwieberContentDescriptor.HashDesc.Columns.KEY_MAXID,
				TwieberContentDescriptor.HashDesc.Columns.KEY_DATE};
		cursor = this.getContentResolver().query(
				TwieberContentDescriptor.HashDesc.CONTENT_URI, 
				projection,
				TwieberContentDescriptor.HashDesc.Columns.KEY_HASHTAG + " = ?" ,
				new String[]{hashtag},
				null);
		if (cursor.moveToFirst()){
			storedMaxID = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.HashDesc.Columns.KEY_MAXID));
			query = query + "?since_id=" + storedMaxID + "&q=%23" + hashtag + "&rpp=15";
		} else {
			query = query + "?q=%23" + hashtag + "";
			insertHash(hashtag);
		}
		return query;
	}
	
	//Inserts a parent record for this hashtag
	private void insertHash(String hashtag){
		ContentValues values = new ContentValues();
		values.put(TwieberContentDescriptor.HashDesc.Columns.KEY_HASHTAG, hashtag);
		values.put(TwieberContentDescriptor.HashDesc.Columns.KEY_MAXID, "0");
		this.getContentResolver().insert(TwieberContentDescriptor.HashDesc.CONTENT_URI, values);
	}
	
	//Updates parent record for this hashtag
	private void updateHash(String hashtag, String maxID){
		Log.d(TAG, "newMaxID: " + maxID);
		ContentValues values = new ContentValues();
		values.put(TwieberContentDescriptor.HashDesc.Columns.KEY_MAXID, maxID);
		this.getContentResolver().update(TwieberContentDescriptor.HashDesc.CONTENT_URI, values, 
				TwieberContentDescriptor.HashDesc.Columns.KEY_HASHTAG + " = ?", 
				new String[]{hashtag});
	}
	
	//Insert tweet into the database
	private void insertTweet(StringBuilder tweetFeedBuilder, String msg){
		try {
			//Begin parsing the JSON response into pieces
			JSONObject resultObject = new JSONObject(tweetFeedBuilder.toString());
			newMaxID = resultObject.getString("max_id");
			JSONArray tweetArray = resultObject.getJSONArray("results");
			ContentValues values = new ContentValues();
			for (int t = 0; t < tweetArray.length(); t++){
				JSONObject tweetObject = tweetArray.getJSONObject(t);
				//If id of this tweet is > max_id of last request, insert it
				if (tweetObject.getString("id").compareTo(maxID) > 0){ 
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_USERNAME, tweetObject.getString("from_user"));
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_DATE, tweetObject.getString("created_at").replace(" +0000", ""));
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_IMAGE, tweetObject.getString("profile_image_url"));
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_HASHTAG, msg);
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_MESSAGE, tweetObject.getString("text"));
					String source = tweetObject.getString("source");
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_SOURCE, source = source.substring(source.indexOf("&gt;"), source.lastIndexOf("&lt;")).replace("&gt;", ""));
					values.put(TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID, tweetObject.getString("id"));
					this.getContentResolver().insert(TwieberContentDescriptor.TweetDesc.CONTENT_URI, values);
				}
			}
		} catch(Exception e){}
	}
	
	//Create and send intent back to activity
	private void sendIntent(){
		//Send intent back to Activity stating the fetch has been completed
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(UpdateReceiver.UPDATE_LIST);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		//Including new maxID for next fetch, and a boolean as to whether anything new was downloaded
		broadcastIntent.putExtra(NEW_TWEETS_OUT, maxID.compareTo(newMaxID) < 0);
		broadcastIntent.putExtra(MAX_ID_OUT, (maxID.compareTo(newMaxID) < 0)? newMaxID : maxID);
		sendBroadcast(broadcastIntent);
	}
}
