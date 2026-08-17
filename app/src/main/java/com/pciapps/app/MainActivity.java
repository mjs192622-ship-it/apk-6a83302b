package com.pciapps.app;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.View;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.widget.ProgressBar;
import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ServiceWorkerClient;
import android.webkit.ServiceWorkerController;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.provider.MediaStore;
import android.os.Environment;
import android.content.ContentValues;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {

    private WebView webView;
    private ProgressBar progressBar;
    private String fcmTokenForWebView = "";
    private static final String WEBSITE_URL = "https://id-preview--1236536e-1ecd-4398-92a6-5728456ccdc7.lovable.app/?__lovable_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9%2EeyJ1c2VyX2lkIjoiMGVpcXpuQ2NWWmNydVJTNW9jSDNyczlqUU9GMiIsInByb2plY3RfaWQiOiIxMjM2NTM2ZS0xZWNkLTQzOTgtOTJhNi01NzI4NDU2Y2NkYzciLCJhY2Nlc3NfdHlwZSI6InByb2plY3QiLCJpc3MiOiJsb3ZhYmxlLWFwaSIsInN1YiI6IjEyMzY1MzZlLTFlY2QtNDM5OC05MmE2LTU3Mjg0NTZjY2RjNyIsImF1ZCI6WyJsb3ZhYmxlLWFwcCJdLCJleHAiOjE3ODcwNjc5ODcsIm5iZiI6MTc4Njk4MTU4NywiaWF0IjoxNzg2OTgxNTg3fQ%2ETEOsrWLEpbFL8b1cSkoDUUIhzMQZWYC-nAFGMtVmU1_nC1FqICIeB3fBJAJSdyedmr5ZZUp4jFU2D4sh2eI00uaNP0cQRAunLR4BmGw0RsowDJsAS5J90ZWmN6kudyzTdlWuiSkeWag_4MI2b8POQ6nvAElJ6d-wXMy9EEVfSsK8Lf3Gr07skhNPddyPr8BHD9dNSc63dML6pht3v6X2j209mq4KfId0yUIphxuLwhy4SJI5dENKCMvBNfZLFA5-6cq4oqSlaAnxENCkpm9aRZPYT3v67nIDZBJQXD4p-xnLdx8qySFM_Dw5tk-aB6QAWXgQ5nwUTfhkuKE9-AN1F24QdYUwa5URnyqbFwiDrRJJDcw96GU6q7hBsXIIorh3Rew7g6HiCjNKjPbN-BOxbsIiJYXMKkH6OMXahWh_99t1sfcEXylfQLQSH_JkCtz3u-QVDDP6jTESiMVqg08WF9hvKNBphoPn3u1eAWYTjP9AuqUD4zFlJSadK0yRcnxEiP4GTnU4hTmWT7JtD-AW3lg9fScax3QO2f0BgLelk6wxfZDqaBxUtG027Q-QKYf-dLFfibnTwIeOir6JxfL7NOSCwXCgFw72kyOXpo3Y4PE_dtrpr0x--U2x1Uq_tTSsInd1O8MvOcpVvBgsdVilXh0JAgKlK-Kgyf4eoUd6Y4c";
    private SwipeRefreshLayout swipeRefresh;
    private ValueCallback<Uri[]> fileUploadCallback;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set system bar colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int systemBarColor = Color.parseColor("#4F46E5");
            getWindow().setStatusBarColor(systemBarColor);
            getWindow().setNavigationBarColor(systemBarColor);
        }
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setColorSchemeColors(Color.parseColor("#000000"));
        swipeRefresh.setOnRefreshListener(() -> {
            webView.reload();
        });
        checkAndShowRateDialog(5);
        
        setupWebView();
        
        // Enable Service Worker support (API 24+)
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            ServiceWorkerController swController = ServiceWorkerController.getInstance();
            swController.setServiceWorkerClient(new ServiceWorkerClient() {
                @Override
                public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                    return null;
                }
            });
        }

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Explicitly fetch and send FCM token at app startup
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        final String fcmToken = task.getResult();
                        fcmTokenForWebView = fcmToken;
                        android.util.Log.d("FCM_TOKEN", "Token received: " + fcmToken);

                        // Show token status via JavaScript in WebView (debug)
                        runOnUiThread(() -> {
                            if (webView != null) {
                                webView.evaluateJavascript(
                                    "window.FCM_TOKEN = '" + fcmToken + "'; window.dispatchEvent(new Event('fcm_token_ready')); console.log('FCM Token set');", null);
                            }
                        });

                        new Thread(() -> {
                            try {
                                // Endpoint is generated by PHP based on Website URL:
                                // User app  => /fcm_token.php
                                // Admin app => /admin/fcm_token.php
                                String endpoint = "https://id-preview--1236536e-1ecd-4398-92a6-5728456ccdc7.lovable.app/fcm_token.php";
                                android.util.Log.d("FCM_TOKEN", "Sending token to: " + endpoint);

                                java.net.URL tokenUrl = new java.net.URL(endpoint);
                                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) tokenUrl.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json");
                                conn.setRequestProperty("User-Agent", "BPWalletApp/1.0");
                                conn.setDoOutput(true);
                                conn.setConnectTimeout(15000);
                                conn.setReadTimeout(15000);
                                String body = "{\"token\":\"" + fcmToken + "\"}";
                                try (java.io.OutputStream os = conn.getOutputStream()) {
                                    os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                                }
                                int status = conn.getResponseCode();
                                android.util.Log.d("FCM_TOKEN", "POST response: HTTP " + status);
                                conn.disconnect();

                                // Fallback: GET request if POST fails
                                if (status < 200 || status >= 300) {
                                    android.util.Log.d("FCM_TOKEN", "POST failed, trying GET fallback...");
                                    String t = java.net.URLEncoder.encode(fcmToken, "UTF-8");
                                    java.net.URL fallbackUrl = new java.net.URL(endpoint + "?token=" + t);
                                    java.net.HttpURLConnection fallbackConn = (java.net.HttpURLConnection) fallbackUrl.openConnection();
                                    fallbackConn.setRequestMethod("GET");
                                    fallbackConn.setRequestProperty("User-Agent", "BPWalletApp/1.0");
                                    fallbackConn.setConnectTimeout(15000);
                                    fallbackConn.setReadTimeout(15000);
                                    int fbStatus = fallbackConn.getResponseCode();
                                    android.util.Log.d("FCM_TOKEN", "GET fallback response: HTTP " + fbStatus);
                                    fallbackConn.disconnect();
                                }
                            } catch (Exception e) {
                                android.util.Log.e("FCM_TOKEN", "Error sending token: " + e.getMessage(), e);
                            }
                        }).start();
                    } else {
                        android.util.Log.e("FCM_TOKEN", "Failed to get token: " + (task.getException() != null ? task.getException().getMessage() : "unknown error"));
                        // Show error as Toast so user can see
                        runOnUiThread(() -> {
                            String errMsg = task.getException() != null ? task.getException().getMessage() : "Unknown FCM error";
                            Toast.makeText(MainActivity.this, "FCM Error: " + errMsg, Toast.LENGTH_LONG).show();
                        });
                    }
                });
        } catch (Exception e) {
            android.util.Log.e("FCM_TOKEN", "Firebase init error: " + e.getMessage(), e);
            Toast.makeText(this, "Firebase Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Schedule background notification polling every 15 minutes
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
            NotificationWorker.class, 15, TimeUnit.MINUTES)
            .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "notification_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest);
        
        
        checkForUpdate();
        
        // Handle deep link intent
        handleIntent(getIntent());
        
        // Load directly; ConnectivityManager can be unreliable on some devices/VPNs.
        // WebView will show its own error page if the connection is actually unavailable.
        webView.loadUrl(WEBSITE_URL);
    }

    private void checkAndShowRateDialog(int targetLaunches) {
        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean ratedAlready = prefs.getBoolean("rated", false);
        if (ratedAlready) return;
        int launches = prefs.getInt("launch_count", 0) + 1;
        prefs.edit().putInt("launch_count", launches).apply();
        if (launches == targetLaunches) {
            new AlertDialog.Builder(this)
                .setTitle("Enjoying the app?")
                .setMessage("If you like the app, please take a moment to rate it. It won't take more than a minute. Thanks!")
                .setPositiveButton("Rate Now", (d, w) -> {
                    prefs.edit().putBoolean("rated", true).apply();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                    }
                })
                .setNeutralButton("Remind Later", null)
                .setNegativeButton("No Thanks", (d, w) -> prefs.edit().putBoolean("rated", true).apply())
                .show();
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDatabaseEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
        // Force dark mode following system setting
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            WebSettingsCompat.setForceDark(webView.getSettings(), WebSettingsCompat.FORCE_DARK_AUTO);
        }
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
            WebSettingsCompat.setForceDarkStrategy(webView.getSettings(), WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
        }

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                // Save cookies for background notification worker
                String cookies = CookieManager.getInstance().getCookie(WEBSITE_URL);
                if (cookies != null && !cookies.isEmpty()) {
                    getSharedPreferences("bp_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("session_cookies", cookies)
                        .putString("website_url", WEBSITE_URL)
                        .apply();
                }
                // Re-inject FCM token after every page load so website JS can POST it with user_id/session.
                if (fcmTokenForWebView != null && !fcmTokenForWebView.isEmpty()) {
                    view.evaluateJavascript("window.FCM_TOKEN = '" + fcmTokenForWebView + "'; window.dispatchEvent(new Event('fcm_token_ready'));", null);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleWebViewUrl(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && request != null && request.getUrl() != null) {
                    return handleWebViewUrl(view, request.getUrl().toString());
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) progressBar.setProgress(newProgress);
            }
            
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (fileUploadCallback != null) {
                    fileUploadCallback.onReceiveValue(null);
                }
                fileUploadCallback = filePathCallback;

                // Camera intent
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "camera_photo");
                cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

                // File chooser intent
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fileIntent.setType("*/*");

                // Combine into chooser
                Intent chooserIntent = Intent.createChooser(fileIntent, "Select file");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

                fileUploadLauncher.launch(chooserIntent);
                return true;
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Downloading file...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean handleWebViewUrl(WebView view, String url) {
        if (url == null || url.trim().isEmpty()) return false;
        String lower = url.toLowerCase();
        if (lower.startsWith("tel:") || lower.startsWith("mailto:") || lower.startsWith("sms:") || lower.startsWith("smsto:") || lower.startsWith("whatsapp:") || lower.startsWith("market:") || lower.startsWith("intent:")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                startActivity(intent);
                return true;
            } catch (Exception ignored) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
                    return true;
                } catch (Exception ignoredAgain) {
                    return true;
                }
            }
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return true;
        }
        try {
            java.net.URL baseUrl = new java.net.URL(WEBSITE_URL);
            java.net.URL targetUrl = new java.net.URL(url);
            if (targetUrl.getHost() != null && !targetUrl.getHost().equalsIgnoreCase(baseUrl.getHost())) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                startActivity(browserIntent);
                return true;
            }
        } catch (Exception e) { /* ignore, load in webview */ }
        view.loadUrl(url);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getData() != null) {
            String deepUrl = intent.getData().toString();
            // Custom URL scheme: convert myapp://path to website URL
            if (deepUrl.startsWith("httpsidpreview1236536e1ecd439892a65728456ccdc7lovableapplovabletokeneyjhbgcioijsuzi1niisinr5cci6ikpxvcj92eeyj1c2vyx2lkijoimgvpcxpuq2nwwmnydvjtnw9jsdnyczlquu9gmiisinbyb2ply3rfawqioiixmjm2ntm2zs0xzwnkltqzotgtotjhni01nzi4ndu2y2nkyzcilcjhy2nlc3nfdhlwzsi6inbyb2ply3qilcjpc3mioijsb3zhymxllwfwasisinn1yii6ijeymzy1mzzlltfly2qtndm5oc05mme2ltu3mjg0ntzjy2rjnyisimf1zci6wyjsb3zhymxllwfwccjdlcjlehaioje3odcwnjc5odcsim5izii6mtc4njk4mtu4nywiawf0ijoxnzg2otgxntg3fq2eteosrwlepbfl8b1cskoduuihzmqzwycnafgmtvmu1nc1fqicieb3fbjajsdyedmr5zzup4jfu2d4sh2ei00uanp0cqraunlr4bmgw0rsowdjsas5j90zwmn6kudyztdlwuiskewag4mi2b8poq6nvaelj6dwxmy9eevfssk8lf3gr07skhnpddypr8bhd9dnsc63dml6pht3v6x2j209mq4kfid0yuiphxulwhy4sji5denkcmvbnfzlfa56cq4oqslaanxenckpm9arzpyt3v67nidzbjqxd4pxnldx8qysfmdw5tkab6qawxgq5nwutfhkuke9an1f24qdyuwa5urnyqbfwidrrjjdcw96gu6q7hbsxiiorh3rew7g6hicjnkjpbnboxbsiijyxmkkh6omxahwh99t1sfcexylfqlqshjkctz3uqvddp6jtesimvqg08wf9hvknbphopn3u1eawytjp9auqud4zfljsadk0yrcnxeip4gtnu4htmwt7jtdaw3lg9fscax3qo2f0bglelk6wxfzdqabxutg027qqkyfdlffibntwieoir6jxfl7noscwxcgfw72kyoxpo3y4pedtrpr0xu2x1uqttssind1o8mvocpvvbgsdvilxh0jagklkkgyf4eoud6y4c://")) {
                String path = deepUrl.replace("httpsidpreview1236536e1ecd439892a65728456ccdc7lovableapplovabletokeneyjhbgcioijsuzi1niisinr5cci6ikpxvcj92eeyj1c2vyx2lkijoimgvpcxpuq2nwwmnydvjtnw9jsdnyczlquu9gmiisinbyb2ply3rfawqioiixmjm2ntm2zs0xzwnkltqzotgtotjhni01nzi4ndu2y2nkyzcilcjhy2nlc3nfdhlwzsi6inbyb2ply3qilcjpc3mioijsb3zhymxllwfwasisinn1yii6ijeymzy1mzzlltfly2qtndm5oc05mme2ltu3mjg0ntzjy2rjnyisimf1zci6wyjsb3zhymxllwfwccjdlcjlehaioje3odcwnjc5odcsim5izii6mtc4njk4mtu4nywiawf0ijoxnzg2otgxntg3fq2eteosrwlepbfl8b1cskoduuihzmqzwycnafgmtvmu1nc1fqicieb3fbjajsdyedmr5zzup4jfu2d4sh2ei00uanp0cqraunlr4bmgw0rsowdjsas5j90zwmn6kudyztdlwuiskewag4mi2b8poq6nvaelj6dwxmy9eevfssk8lf3gr07skhnpddypr8bhd9dnsc63dml6pht3v6x2j209mq4kfid0yuiphxulwhy4sji5denkcmvbnfzlfa56cq4oqslaanxenckpm9arzpyt3v67nidzbjqxd4pxnldx8qysfmdw5tkab6qawxgq5nwutfhkuke9an1f24qdyuwa5urnyqbfwidrrjjdcw96gu6q7hbsxiiorh3rew7g6hicjnkjpbnboxbsiijyxmkkh6omxahwh99t1sfcexylfqlqshjkctz3uqvddp6jtesimvqg08wf9hvknbphopn3u1eawytjp9auqud4zfljsadk0yrcnxeip4gtnu4htmwt7jtdaw3lg9fscax3qo2f0bglelk6wxfzdqabxutg027qqkyfdlffibntwieoir6jxfl7noscwxcgfw72kyoxpo3y4pedtrpr0xu2x1uqttssind1o8mvocpvvbgsdvilxh0jagklkkgyf4eoud6y4c://", "");
                String baseUrl = WEBSITE_URL.endsWith("/") ? WEBSITE_URL : WEBSITE_URL + "/";
                deepUrl = baseUrl + path;
            }
            if (deepUrl.startsWith("http")) {
                webView.loadUrl(deepUrl);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private final ActivityResultLauncher<Intent> fileUploadLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (fileUploadCallback == null) return;
            if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                fileUploadCallback.onReceiveValue(new Uri[]{result.getData().getData()});
            } else if (result.getResultCode() == RESULT_OK && cameraImageUri != null) {
                fileUploadCallback.onReceiveValue(new Uri[]{cameraImageUri});
            } else {
                fileUploadCallback.onReceiveValue(null);
            }
            fileUploadCallback = null;
        });

    private void checkForUpdate() {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("https://id-preview--1236536e-1ecd-4398-92a6-5728456ccdc7.lovable.app/?__lovable_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9%2EeyJ1c2VyX2lkIjoiMGVpcXpuQ2NWWmNydVJTNW9jSDNyczlqUU9GMiIsInByb2plY3RfaWQiOiIxMjM2NTM2ZS0xZWNkLTQzOTgtOTJhNi01NzI4NDU2Y2NkYzciLCJhY2Nlc3NfdHlwZSI6InByb2plY3QiLCJpc3MiOiJsb3ZhYmxlLWFwaSIsInN1YiI6IjEyMzY1MzZlLTFlY2QtNDM5OC05MmE2LTU3Mjg0NTZjY2RjNyIsImF1ZCI6WyJsb3ZhYmxlLWFwcCJdLCJleHAiOjE3ODcwNjc5ODcsIm5iZiI6MTc4Njk4MTU4NywiaWF0IjoxNzg2OTgxNTg3fQ%2ETEOsrWLEpbFL8b1cSkoDUUIhzMQZWYC-nAFGMtVmU1_nC1FqICIeB3fBJAJSdyedmr5ZZUp4jFU2D4sh2eI00uaNP0cQRAunLR4BmGw0RsowDJsAS5J90ZWmN6kudyzTdlWuiSkeWag_4MI2b8POQ6nvAElJ6d-wXMy9EEVfSsK8Lf3Gr07skhNPddyPr8BHD9dNSc63dML6pht3v6X2j209mq4KfId0yUIphxuLwhy4SJI5dENKCMvBNfZLFA5-6cq4oqSlaAnxENCkpm9aRZPYT3v67nIDZBJQXD4p-xnLdx8qySFM_Dw5tk-aB6QAWXgQ5nwUTfhkuKE9-AN1F24QdYUwa5URnyqbFwiDrRJJDcw96GU6q7hBsXIIorh3Rew7g6HiCjNKjPbN-BOxbsIiJYXMKkH6OMXahWh_99t1sfcEXylfQLQSH_JkCtz3u-QVDDP6jTESiMVqg08WF9hvKNBphoPn3u1eAWYTjP9AuqUD4zFlJSadK0yRcnxEiP4GTnU4hTmWT7JtD-AW3lg9fScax3QO2f0BgLelk6wxfZDqaBxUtG027Q-QKYf-dLFfibnTwIeOir6JxfL7NOSCwXCgFw72kyOXpo3Y4PE_dtrpr0x--U2x1Uq_tTSsInd1O8MvOcpVvBgsdVilXh0JAgKlK-Kgyf4eoUd6Y4c");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                
                JSONObject json = new JSONObject(sb.toString());
                String latestVersion = json.optString("latest_version", "");
                final String updateUrl = json.optString("update_url", "");
                
                if (!latestVersion.isEmpty() && !latestVersion.equals("1.0.0") && !updateUrl.isEmpty()) {
                    runOnUiThread(() -> {
                        new android.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("Update Available")
                            .setMessage("A new version (" + latestVersion + ") is available. Would you like to update?")
                            .setPositiveButton("Update", (d, w) -> {
                                startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(updateUrl)));
                            })
                            .setNegativeButton("Later", null)
                            .show();
                    });
                }
            } catch (Exception e) { /* silent fail */ }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Share").setIcon(android.R.drawable.ic_menu_share)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menu.add(0, 999, 0, "Privacy Policy");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            return true;
        }
        if (item.getItemId() == 999) {
            startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://id-preview--1236536e-1ecd-4398-92a6-5728456ccdc7.lovable.app/?__lovable_token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9%2EeyJ1c2VyX2lkIjoiMGVpcXpuQ2NWWmNydVJTNW9jSDNyczlqUU9GMiIsInByb2plY3RfaWQiOiIxMjM2NTM2ZS0xZWNkLTQzOTgtOTJhNi01NzI4NDU2Y2NkYzciLCJhY2Nlc3NfdHlwZSI6InByb2plY3QiLCJpc3MiOiJsb3ZhYmxlLWFwaSIsInN1YiI6IjEyMzY1MzZlLTFlY2QtNDM5OC05MmE2LTU3Mjg0NTZjY2RjNyIsImF1ZCI6WyJsb3ZhYmxlLWFwcCJdLCJleHAiOjE3ODcwNjc5ODcsIm5iZiI6MTc4Njk4MTU4NywiaWF0IjoxNzg2OTgxNTg3fQ%2ETEOsrWLEpbFL8b1cSkoDUUIhzMQZWYC-nAFGMtVmU1_nC1FqICIeB3fBJAJSdyedmr5ZZUp4jFU2D4sh2eI00uaNP0cQRAunLR4BmGw0RsowDJsAS5J90ZWmN6kudyzTdlWuiSkeWag_4MI2b8POQ6nvAElJ6d-wXMy9EEVfSsK8Lf3Gr07skhNPddyPr8BHD9dNSc63dML6pht3v6X2j209mq4KfId0yUIphxuLwhy4SJI5dENKCMvBNfZLFA5-6cq4oqSlaAnxENCkpm9aRZPYT3v67nIDZBJQXD4p-xnLdx8qySFM_Dw5tk-aB6QAWXgQ5nwUTfhkuKE9-AN1F24QdYUwa5URnyqbFwiDrRJJDcw96GU6q7hBsXIIorh3Rew7g6HiCjNKjPbN-BOxbsIiJYXMKkH6OMXahWh_99t1sfcEXylfQLQSH_JkCtz3u-QVDDP6jTESiMVqg08WF9hvKNBphoPn3u1eAWYTjP9AuqUD4zFlJSadK0yRcnxEiP4GTnU4hTmWT7JtD-AW3lg9fScax3QO2f0BgLelk6")));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }
}