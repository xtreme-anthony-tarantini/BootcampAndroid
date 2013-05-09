package bootcamp.android.twieber.adapter;

import bootcamp.android.twieber.R;
import bootcamp.android.twieber.manager.TwieberImageManager;
import bootcamp.android.twieber.contentprovider.TwieberContentDescriptor;

import android.app.Activity;

import android.content.Context;

import android.database.Cursor;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.CursorAdapter;

//import android.util.Log;

public class TwieberCursorAdapter extends CursorAdapter {
	//private static final String TAG = TwieberCursorAdapter.class.getName();
	private TwieberImageManager imageManager;
	private LayoutInflater layoutInflater;
	
	public TwieberCursorAdapter(Context context, Cursor cursor, TwieberImageManager imageManager) {
		super(context, cursor, false);
		this.imageManager = imageManager;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	//When a cell can be reused this method will be called to fill it
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		final String username = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_USERNAME));
		final String time = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_DATE));
		final String message = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_MESSAGE));
		final String image = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_IMAGE));
		final String source = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_SOURCE));
		final String id = cursor.getString(cursor.getColumnIndex(TwieberContentDescriptor.TweetDesc.Columns.KEY_TWEETID));
		holder.username.setText(username);
		holder.time.setText(time + " via " + source);
		holder.message.setText(message);
		//Set tag of message view to the link back to the tweet
		holder.message.setTag("http://twitter.com/" + username + "/status/" + id);
		holder.image.setTag(image);
		//Call ImageManager to queue download of profile image
		imageManager.displayImage(image, (Activity) context, holder.image);
	}

	//Inflate cell layout if reuse of old cell is not available
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = layoutInflater.inflate(R.layout.twieber_cell, null);
		ViewHolder holder = new ViewHolder();
		holder.username = (TextView)view.findViewById(R.id.username);
		holder.time = (TextView)view.findViewById(R.id.time);
		holder.message = (TextView)view.findViewById(R.id.message);
		holder.image = (ImageView)view.findViewById(R.id.avatar);
		holder.seperator = (ImageView)view.findViewById(R.id.seperator);
		view.setTag(holder);
		return view;
	}
	
	//Class used to create a holder cell that will be reused when no longer needed
	public static class ViewHolder{
		public TextView username;
		public TextView time;
		public TextView message;
		public ImageView image;
		public ImageView seperator;
	}

}