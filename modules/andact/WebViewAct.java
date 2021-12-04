package org.godotengine.godot;

import android.app.Activity;
import android.util.Log;
import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.View;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.os.Bundle;

public class WebViewAct extends Activity {
	
	public String url;

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    Intent intent = getIntent();
		url = intent.getStringExtra("url");

		webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
	    webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "Activity");
        
		webView.setWebViewClient(new WebViewClient() {
			@Override
		    public void onPageFinished(WebView view, String url) {
		        webView.loadUrl(
					"javascript:" +
					"var button = document.getElementById('ok');" +
					"button.onclick = function(){ Activity.JSFinish(); }"
				);

		    }
		});

        webView.loadUrl(url);
        setContentView(webView);

		webView.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
 				}
				return false;
 			}
		});
    }

	@JavascriptInterface
	public void JSFinish() {
		finish();
	}
}

