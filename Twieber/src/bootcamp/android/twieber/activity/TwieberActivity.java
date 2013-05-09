package bootcamp.android.twieber.activity;

import bootcamp.android.twieber.R;
import bootcamp.android.twieber.service.TwieberService;
import bootcamp.android.twieber.manager.TwieberImageManager;
import bootcamp.android.twieber.adapter.TwieberCursorAdapter;
import bootcamp.android.twieber.contentprovider.TwieberContentDescriptor;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.AsyncTask;
//import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class TwieberActivity extends ListActivity {
	//private static final String TAG = TwieberActivity.class.getName();
	
	private static final int period = 30000;
	
	private UpdateReceiver receiver;
	private String hashTag;
	private Handler handler;
	private TwieberImageManager imageManager;
	private IntentFilter filter;
	private ProgressDialog progressDialog;
	private Boolean showDialog;
	private ListView listView;
	private Context context;
	
	public String maxID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twieber_list);
		initVariables();
		createReceiver();
		createHandler();
		setListener();
	}

	//Initializing instance variables for the application
	private void initVariables(){
		progressDialog = new ProgressDialog(TwieberActivity.this);
		imageManager = new TwieberImageManager(this);
		listView = getListView();
		hashTag = "bieber";
		showDialog = true;
		context = this;
		maxID = "0";
	}
	
	//Creates broadcast receiver for update prompt
	private void createReceiver(){
		filter = new IntentFilter(UpdateReceiver.UPDATE_LIST);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		receiver = new UpdateReceiver();
		registerReceiver(receiver, filter);
	}
	
	//Creates handler for recurring updates
	private void createHandler(){
		handler = new Handler();
		handler.post(regularUpdate);
	}
	
	//Sets listener for the list cells onClick
	private void setListener(){
		listView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int arg2, long arg3){
				Intent intent = new Intent(context, TwieberWebActivity.class);
				intent.setData(Uri.parse(((TextView) view.findViewById(R.id.message)).getTag().toString()));
				startActivity(intent);
			}
		});
	}
	
	
	//A runnable for the thirty second updates
	private Runnable regularUpdate = new Runnable(){
		@Override
		public void run(){
			if (showDialog) {
				progressDialog.setMessage("Loading tweets...");
				progressDialog.show();
				showDialog = false;
			}
			startTwieberService();
			handler.postDelayed(this, period);
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.twieber_menu, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		createAlert().show();
		return true;
	}
	
	//Creates the hashtag change popup
	private AlertDialog createAlert(){
		AlertDialog.Builder menuBuilder = new AlertDialog.Builder(this);		
		menuBuilder.setTitle("Hashtag Setter");
		menuBuilder.setMessage("Change Your Hashtag");
		final EditText input = new EditText(this);
		input.setSingleLine();
		input.setHint(hashTag);
		menuBuilder.setView(input);
		menuBuilder.setPositiveButton("Search", new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which){
				//concatenates all words
				String tmpTag = input.getText().toString().replace(" ", "");
				//Makes sure all character are either letters or numbers
				if (tmpTag.matches("[a-zA-Z0-9]+")){	
					hashTag = tmpTag;
					//Resets maxID to 0 for List, removes old Callbacks and creates new regular update
					maxID = "0";
					handler.removeCallbacks(regularUpdate);
					showDialog = true;
					handler.post(regularUpdate);
				}
			}
		});
		return menuBuilder.create();
	}
	
	//Creates intent and sends it off to the service for query
	private void startTwieberService(){
		Intent msgIntent = new Intent(this, TwieberService.class);
		msgIntent.putExtra(TwieberService.HASH_TAG_IN, hashTag);
		msgIntent.putExtra(TwieberService.MAX_ID_IN, maxID);
		//Log.d(TAG, "Starting Service");
		startService(msgIntent);
	}
	
	//Asynchronously loads the data from the database into a cursor
	private class LoadContent extends AsyncTask<Void, Integer, Cursor>{
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
		}
		
		@Override
		protected Cursor doInBackground(Void... params){
				String[] projection = new String[]{
						TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_USERNAME,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_DATE,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_MESSAGE,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_IMAGE,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_SOURCE};
				//Queries the database for a cursor with the correct data
				return context.getContentResolver().query(
						TwieberContentDescriptor.TweetDesc.CONTENT_URI, 
						projection,
						TwieberContentDescriptor.TweetDesc.Columns.KEY_HASHTAG + " = ?" ,
						new String[]{hashTag},
						TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID + " DESC " + "LIMIT 15");
		}
		
		@Override
		protected void onPostExecute(Cursor cursor){
			super.onPostExecute(cursor);
			//Sets the new cursor into a new adapter on the list
			setListAdapter(new TwieberCursorAdapter(context, cursor, imageManager));
	        if (progressDialog.isShowing()) {
	        	progressDialog.dismiss();
	        }
		}
	}
	
	//The broadcast receiver for communications with the service
	public class UpdateReceiver extends BroadcastReceiver {
		public static final String UPDATE_LIST = "fetch_completed";
		
		@Override
		public void onReceive(Context context, Intent intent){
			if (intent.getBooleanExtra(TwieberService.NEW_TWEETS_OUT, false)) {
				maxID = intent.getStringExtra(TwieberService.MAX_ID_OUT);
				//Asynchronously load data into cursor and return
				new LoadContent().execute();
			}
		}
	}
	
	//On Pause, stop all repeating tasks and unregister receiver
	@Override
	public void onPause(){
		super.onPause();
		try {
			handler.removeCallbacks(regularUpdate);
			unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//On Resume, update and set recurring updates, register receiver
	@Override
	public void onResume(){
		super.onResume();
		handler.post(regularUpdate);
		registerReceiver(receiver, filter);
	}
	
	//On Destroy, stop all repeating tasks and unregister receiver
	@Override
	public void onDestroy(){
		super.onDestroy();
		try {
			handler.removeCallbacks(regularUpdate);
			unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}