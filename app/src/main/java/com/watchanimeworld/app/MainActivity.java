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
import android.graphics.Bitmap;
import android.os.Message;
import android.content.Intent;
import android.net.Uri;
import java.io.ByteArrayInputStream;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final String WEBSITE_URL = "https://watchanimeworld.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);

        // Initialize AdGuard manager (stub). When AdGuard SDK is configured this enables blocking.
        AdGuardManager.initialize(this);

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
        // Allow JS to open windows so onCreateWindow will be triggered; we'll capture them
        ws.setJavaScriptCanOpenWindowsAutomatically(true);

        // Intercept window.open / target="_blank" and load the URL in the same WebView
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView popupWebView = new WebView(view.getContext());
                popupWebView.getSettings().setJavaScriptEnabled(true);
                popupWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageStarted(WebView wv, String url, Bitmap favicon) {
                        // Load popup URL into the main WebView and destroy the temporary one
                        webView.post(() -> webView.loadUrl(url));
                        wv.destroy();
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(popupWebView);
                resultMsg.sendToTarget();
                return true; // We handled the request (no new window will be shown)
            }
        });

        // Force most links to open in the app; block/handle non-http schemes explicitly
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(view, url);
            }

            private boolean handleUrl(WebView view, String url) {
                if (url == null) return false;

                // Allow http(s) links to load inside the WebView
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }

                // Optionally handle other schemes you want to allow in external apps
                if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        // ignore if no app to handle
                    }
                    return true;
                }

                // Block everything else (prevents popup-like behavior for strange schemes)
                return true;
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                try {
                    String url = request.getUrl().toString();
                    Map<String, String> headers = request.getRequestHeaders();

                    // First ask AdGuardManager (when real SDK is integrated this will consult blocklists)
                    if (AdGuardManager.shouldBlockRequest(url, headers)) {
                        return new WebResourceResponse("text/plain", "utf-8",
                                new ByteArrayInputStream("".getBytes()));
                    }

                    // Fallback lightweight heuristic blocking
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

                // Continuous protection: use MutationObserver to remove dynamically added ad elements
                String mo = "(function(){try{var observer=new MutationObserver(function(m){m.forEach(function(r){r.addedNodes.forEach(function(n){if(n.querySelectorAll){['[class*=\\\"ad\\\"]','[id*=\\\"ad\\\"]','.advertisement','.banner','.popup','.modal'].forEach(function(s){if(n.matches&&n.matches(s))n.remove(); n.querySelectorAll&&n.querySelectorAll(s).forEach(function(el){el.remove();});});}});});});observer.observe(document,{childList:true,subtree:true});}catch(e){} })();";
                view.evaluateJavascript(mo, null);

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
