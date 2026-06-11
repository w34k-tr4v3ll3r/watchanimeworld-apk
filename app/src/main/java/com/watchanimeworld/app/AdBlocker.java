package com.watchanimeworld.app;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Powerful Ad Blocker for WebView
 * Blocks ads by filtering URLs against a comprehensive blocklist
 */
public class AdBlocker {
    private static final String TAG = "AdBlocker";
    private static AdBlocker instance;
    private Set<String> blockedDomains;
    private Set<String> blockedPatterns;
    private boolean isInitialized = false;

    // Hardcoded list of major ad networks and tracking domains
    private static final String[] DEFAULT_BLOCKED_DOMAINS = {
            // Google Ads
            "googlesyndication.com",
            "googleadservices.com",
            "google-analytics.com",
            "analytics.google.com",
            "googletagmanager.com",

            // Facebook
            "facebook.com/tr",
            "facebook.com/ads",
            "fbcdn.net",

            // Common ad networks
            "doubleclick.net",
            "adnetwork.com",
            "adservice.com",
            "adserver.com",
            "ads.com",
            "adtech.de",
            "advertising.com",
            "admap.com",
            "admob.com",
            "adcolony.com",
            "appsflyer.com",
            "adjust.com",
            "amplitude.com",

            // Tracking services
            "mixpanel.com",
            "segment.com",
            "intercom.io",
            "drift.com",
            "kissmetrics.com",
            "quantserve.com",
            "scorecardresearch.com",
            "chartbeat.com",
            "getclicky.com",
            "statcounter.com",

            // Pop-ups and malicious sites
            "popads.net",
            "popcash.net",
            "popunder.net",
            "clicksor.com",
            "clickadu.com",
            "clickfair.com",
            "clickx.net",

            // Social media trackers
            "addthis.com",
            "shareaholic.com",
            "disqus.com",

            // Video ads
            "adsnative.com",
            "outbrain.com",
            "taboola.com",

            // Crypto and scams
            "coinbase.com/ads",
            "binance.com/ads",

            // More tracking
            "hotjar.com",
            "crazyegg.com",
            "userreporting.com",
            "errorception.com",
            "bugsnag.com",
            "rollbar.com",
            "sentry.io",

            // Real estate and dating ads
            "redfin.com",
            "zillow.com",
            "match.com",
            "eharmony.com",

            // Streaming service ads
            "hulu.com/ads",
            "ads-server.netflix.com"
    };

    private AdBlocker(Context context) {
        blockedDomains = new HashSet<>();
        blockedPatterns = new HashSet<>();
        loadBlockList(context);
    }

    /**
     * Get singleton instance
     */
    public static synchronized AdBlocker getInstance(Context context) {
        if (instance == null) {
            instance = new AdBlocker(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Load blocked domains from default list
     */
    private void loadBlockList(Context context) {
        // Add default domains
        for (String domain : DEFAULT_BLOCKED_DOMAINS) {
            blockedDomains.add(domain.toLowerCase());
        }

        // Try to load from assets file if it exists
        try {
            loadFromAssets(context);
        } catch (Exception e) {
            Log.d(TAG, "Asset file not found, using default list only");
        }

        isInitialized = true;
        Log.d(TAG, "AdBlocker initialized with " + blockedDomains.size() + " blocked domains");
    }

    /**
     * Load additional blocked domains from assets
     */
    private void loadFromAssets(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("ad_block_list.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                blockedDomains.add(line.toLowerCase());
            }
        }
        reader.close();
        inputStream.close();
    }

    /**
     * Check if a URL should be blocked
     */
    public boolean shouldBlockUrl(String url) {
        if (!isInitialized || url == null) {
            return false;
        }

        String urlLower = url.toLowerCase();

        // Check if URL contains any blocked domain
        for (String domain : blockedDomains) {
            if (urlLower.contains(domain)) {
                Log.d(TAG, "Blocked ad URL: " + url);
                return true;
            }
        }

        // Check for common ad patterns
        if (urlLower.contains("/ads/") ||
                urlLower.contains("/ad?") ||
                urlLower.contains("/advertisement") ||
                urlLower.contains("/banner") ||
                urlLower.contains("/popup") ||
                urlLower.contains("/click") ||
                urlLower.contains("/track") ||
                urlLower.contains("/pixel") ||
                urlLower.contains("/beacon") ||
                urlLower.contains("/advert") ||
                urlLower.contains("/promo") ||
                urlLower.contains("/sponsor")) {
            Log.d(TAG, "Blocked pattern URL: " + url);
            return true;
        }

        return false;
    }

    /**
     * Add custom domain to block list
     */
    public void addBlockedDomain(String domain) {
        if (domain != null) {
            blockedDomains.add(domain.toLowerCase());
        }
    }

    /**
     * Remove domain from block list
     */
    public void removeBlockedDomain(String domain) {
        if (domain != null) {
            blockedDomains.remove(domain.toLowerCase());
        }
    }

    /**
     * Clear all blocked domains
     */
    public void clear() {
        blockedDomains.clear();
        isInitialized = false;
    }

    /**
     * Get count of blocked domains
     */
    public int getBlockedDomainsCount() {
        return blockedDomains.size();
    }

    /**
     * Check if ad blocker is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
}
