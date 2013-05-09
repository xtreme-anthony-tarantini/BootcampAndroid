package bootcamp.android.twieber.manager;

import bootcamp.android.twieber.R;

import java.net.URL;
import java.util.Stack;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.util.Log;
import android.widget.ImageView;

public class TwieberImageManager {
	//private static final String TAG = TwieberImageManager.class.getName();
	private HashMap<String, Bitmap> bitmapHash;
	private ImageQueue imageQueue = new ImageQueue();
	private Thread imageLoaderThread = new Thread(new ImageQueueManager());

	public TwieberImageManager(Context context) {
		bitmapHash = new HashMap<String, Bitmap>();
		imageLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
	}

	//Method called from adapter
	public void displayImage(String url, Activity activity, ImageView imageView) {
		if(bitmapHash.containsKey(url)) {
			imageView.setImageBitmap(bitmapHash.get(url));
		} else {
			queueImage(url, activity, imageView);
			imageView.setImageResource(R.drawable.twieber_missing);
		}
	}

	//If image wasn't found in Hash, put it in the download Thread
	private void queueImage(String url, Activity activity, ImageView imageView) {
		imageQueue.Clean(imageView);
		ProfileImage p=new ProfileImage(url, imageView);
		
		//Make sure all interactions with queue are synchronized
		synchronized(imageQueue.imageRefs) {
			imageQueue.imageRefs.push(p);
			imageQueue.imageRefs.notifyAll();
		}
		
		if(imageLoaderThread.getState() == Thread.State.NEW){
			imageLoaderThread.start();
		}
	}
	
	//Actual download of the image
	private Bitmap fetchBitmap(String url) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(new URL(url).openConnection().getInputStream());
			return bitmap;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	//Image Model
	private class ProfileImage {
		public String url;
		public ImageView imageView;

		public ProfileImage(String url, ImageView imageView) {
			this.url=url;
			this.imageView=imageView;
		}
	}
	
	//Queue for holding pending tasks on the download thread
	private class ImageQueue {
		private Stack<ProfileImage> imageRefs = new Stack<ProfileImage>();

		//Removes all queued downloads for this image already in the stack
		public void Clean(ImageView view) {
			for(int i = 0 ;i < imageRefs.size();) {
				if(imageRefs.get(i).imageView == view){
					imageRefs.remove(i);
				} else {
					i++;
				}
	
			}
		}
	}

	//Helper class for the Thread and Queue interaction
	private class ImageQueueManager implements Runnable {
		@Override
		public void run() {
			try {
				while(true) {
					// Thread waits until there are images in the queue to be retrieved
					if(imageQueue.imageRefs.size() == 0) {
						synchronized(imageQueue.imageRefs) {
							imageQueue.imageRefs.wait();
						}
					}

					// When we have images to be loaded
					if(imageQueue.imageRefs.size() != 0) {
						ProfileImage imageToLoad;
						synchronized(imageQueue.imageRefs) {
							imageToLoad = imageQueue.imageRefs.pop();
						}
						Bitmap bmp;
						if (bitmapHash.containsKey(imageToLoad.url)){
							bmp = bitmapHash.get(imageToLoad.url);
						} else {
							bmp = fetchBitmap(imageToLoad.url);
						}
						bitmapHash.put(imageToLoad.url, bmp);
						Object tag = imageToLoad.imageView.getTag();

						// Make sure we have the right view - thread safety defender
						if(tag != null && ((String)tag).equals(imageToLoad.url)) {
							BitmapDisplayer bmpDisplayer = new BitmapDisplayer(bmp, imageToLoad.imageView);
							Activity a = (Activity)imageToLoad.imageView.getContext();
							//Puts a runnable on the UI thread to display image
							a.runOnUiThread(bmpDisplayer);
						}
					}	
					if(Thread.interrupted()){
						break;
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	//Used to display bitmap in the UI thread
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap bitmap, ImageView imageView) {
			this.bitmap=bitmap;
			this.imageView=imageView;
		}

		public void run() {
			if(bitmap != null) {
				imageView.setImageBitmap(bitmap);
			} else {
				imageView.setImageResource(R.drawable.twieber_missing);
			}
		}
	}
}
