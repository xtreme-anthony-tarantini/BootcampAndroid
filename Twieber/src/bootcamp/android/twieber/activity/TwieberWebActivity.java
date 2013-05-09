package bootcamp.android.twieber.activity;

import bootcamp.android.twieber.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
//import android.util.Log;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwieberWebActivity extends Activity {
	//private static final String TAG = TwieberWebActivity.class.getName();
	private WebView webView;
	private ProgressDialog progressDialog;
	
	//Called on create to navigate in-app to the webpage given from List
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_twieber_web);
        webView = (WebView) findViewById(R.id.web_view);
        webView.getSettings().setJavaScriptEnabled(true);
        progressDialog = ProgressDialog.show(this, "Navigating Twitter", "Loading...");
        webView.setWebViewClient(new TwieberWebViewClient());
        webView.loadUrl(this.getIntent().getDataString());
    }
    
    //This class is implemented to stop the app from opening the browser
    private class TwieberWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }
        public void onPageFinished(WebView view, String url){
        	if(progressDialog.isShowing()){
        		progressDialog.dismiss();
        	}
        }
    }
}
