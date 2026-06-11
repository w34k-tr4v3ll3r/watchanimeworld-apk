package com.watchanimeworld.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.KeyEvent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.widget.Toast;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.ByteArrayInputStream;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String WEBSITE_URL = "https://watchanimeworld.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        // Check internet connectivity
        if (!isInternetConnected()) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }

        // Configure WebView settings
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setJavaScriptCanOpenWindowsAutomatically(false); // prevent automatic popups

        // Prevent new windows/popups
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                // Returning false prevents creating new windows (popups)
                return false;
            }
        });

        // Set WebViewClient to handle page loading and lightweight ad blocking + JS injection
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                try {
                    String url = request.getUrl().toString();
                    if (isAdUrl(url)) {
                        return new WebResourceResponse("text/plain", "utf-8",
                                new ByteArrayInputStream("".getBytes()));
                    }
                } catch (Exception e) {
                    // ignore and continue
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // Inject JS to remove common ad/pop-up elements from DOM
                String js = "(function(){" +
                        "try{" +
                        "var sel = ['[class*=\"ad\"]','[id*=\"ad\"]','.advertisement','.banner','.popup','.modal','iframe[src*=\"ads\"]'];" +
                        "sel.forEach(function(s){document.querySelectorAll(s).forEach(function(el){el.remove();});});" +
                        "var iframes = document.querySelectorAll('iframe');" +
                        "for(var i=0;i<iframes.length;i++){var src=iframes[i].src||''; if(src.indexOf('doubleclick')>-1||src.indexOf('googlesyndication')>-1||src.indexOf('ads')>-1){iframes[i].remove();}}" +
                        "}catch(e){}" +
                        "})();";

                view.evaluateJavascript(js, null);

                // Optional: run a second pass after a short delay to catch dynamically added elements
                view.postDelayed(() -> view.evaluateJavascript(js, null), 1000);
            }

            private boolean isAdUrl(String url) {
                String[] adDomains = {
                        "doubleclick.net", "googlesyndication.com", "pagead2.googlesyndication.com",
                        "adservice.google.com", "ads.", "adsystem."
                };
                for (String d : adDomains) if (url.contains(d)) return true;
                return false;
            }
        });

        // Load the website
        webView.loadUrl(WEBSITE_URL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isInternetConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
