package org.codesoup;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/* TODO -- might be used for any other app with oauth ? */

public class ActivityWebView extends Activity 
{	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        String url = getIntent().getStringExtra("url");
        // If authentication works, we'll get redirected to a url with a pattern like:  
        //
        //    http://YOUR_REGISTERED_REDIRECT_URI/?code=ACCESS_CODE
        //
        // We can override onPageStarted() in the web client and grab the code out.
        WebView webview = (WebView)findViewById(R.id.webview);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.stopLoading();
        webview.setWebViewClient(new WebViewClient() {
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                String fragment = "?code=";
                int start = url.indexOf(fragment);
                if (start > -1) {
                    String code = url.substring(start + fragment.length(), url.length());
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("code", code);                    
                    ActivityWebView.this.setResult(Activity.RESULT_OK, resultIntent);
                	ActivityWebView.this.finish();
                }
            }
        });
        webview.loadUrl(url);
    }
}
