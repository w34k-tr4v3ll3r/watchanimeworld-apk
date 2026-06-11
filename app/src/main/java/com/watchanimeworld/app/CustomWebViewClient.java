package com.watchanimeworld.app;

import android.content.Context;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

/**
 * Custom WebViewClient with integrated ad blocking
 * Intercepts all URL requests and filters out ads
 */
public class CustomWebViewClient extends WebViewClient {
    private static final String TAG = "CustomWebViewClient";
    private AdBlocker adBlocker;

    public CustomWebViewClient(Context context) {
        this.adBlocker = AdBlocker.getInstance(context);
    }

    /**
     * Override URL loading to check against ad blocker
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        if (adBlocker.shouldBlockUrl(url)) {
            Log.d(TAG, "Ad blocked: " + url);
            return true; // Block the request
        }

        view.loadUrl(url);
        return true;
    }

    /**
     * Legacy version for older Android versions
     */
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (adBlocker.shouldBlockUrl(url)) {
            Log.d(TAG, "Ad blocked: " + url);
            return true; // Block the request
        }

        view.loadUrl(url);
        return true;
    }

    /**
     * Intercept all resources (images, scripts, stylesheets, etc.)
     */
    @Override
    public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        if (adBlocker.shouldBlockUrl(url)) {
            Log.d(TAG, "Resource blocked: " + url);
            // Return empty response to block the resource
            return createEmptyResponse();
        }

        return super.shouldInterceptRequest(view, request);
    }

    /**
     * Legacy version for older Android versions
     */
    @Override
    public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (adBlocker.shouldBlockUrl(url)) {
            Log.d(TAG, "Resource blocked: " + url);
            return createEmptyResponse();
        }

        return super.shouldInterceptRequest(view, url);
    }

    /**
     * Create an empty response to block ads
     */
    private android.webkit.WebResourceResponse createEmptyResponse() {
        try {
            return new android.webkit.WebResourceResponse(
                    "text/plain",
                    "utf-8",
                    new java.io.ByteArrayInputStream("".getBytes())
            );
        } catch (Exception e) {
            Log.e(TAG, "Error creating empty response", e);
            return null;
        }
    }

    /**
     * Called when page loading finishes
     */
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        Log.d(TAG, "Page loaded: " + url);

        // Inject CSS to hide ad elements
        injectAdBlockingCSS(view);
    }

    /**
     * Inject CSS to hide common ad elements
     */
    private void injectAdBlockingCSS(WebView view) {
        String css = "javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.innerHTML = '" +
                // Hide common ad containers
                "[class*=\\\"ad\\\"] { display: none !important; } " +
                "[id*=\\\"ad\\\"] { display: none !important; } " +
                "[class*=\\\"advert\\\"] { display: none !important; } " +
                "[id*=\\\"advert\\\"] { display: none !important; } " +
                "[class*=\\\"banner\\\"] { display: none !important; } " +
                "[id*=\\\"banner\\\"] { display: none !important; } " +
                "[class*=\\\"advertisement\\\"] { display: none !important; } " +
                "[id*=\\\"advertisement\\\"] { display: none !important; } " +
                "[class*=\\\"promo\\\"] { display: none !important; } " +
                "[id*=\\\"promo\\\"] { display: none !important; } " +
                "[class*=\\\"popup\\\"] { display: none !important; } " +
                "[id*=\\\"popup\\\"] { display: none !important; } " +
                "[class*=\\\"modal\\\"] { display: none !important; } " +
                "[id*=\\\"modal\\\"] { display: none !important; } " +
                "iframe[src*=\\\"ads\\\"] { display: none !important; } " +
                "iframe[src*=\\\"ad\\\"] { display: none !important; } " +
                "iframe[src*=\\\"banner\\\"] { display: none !important; } " +
                ".adsbygoogle { display: none !important; } " +
                ".sponsor-box { display: none !important; } " +
                ".promotional { display: none !important; } " +
                "';" +
                "document.head.appendChild(style);" +
                "})();";

        view.evaluateJavascript(css, null);
    }

    /**
     * Called when there's an error loading the page
     */
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Log.e(TAG, "Error loading page: " + failingUrl + " - " + description);
    }
}
