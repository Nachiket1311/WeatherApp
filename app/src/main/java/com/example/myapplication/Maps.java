package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class Maps extends AppCompatActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        webView = findViewById(R.id.webview);

        // Enable JavaScript for the WebView
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        WebView.setWebContentsDebuggingEnabled(true);

        // Prevent WebView from opening the default browser
        webView.setWebViewClient(new WebViewClient());

        // Load the Windy Map
        loadWindyMap();
    }

    private void loadWindyMap() {
        // HTML code for loading Windy Maps and initializing Windy API
        String html = "<html>" +
                "<head>" +
                "<script src='https://unpkg.com/leaflet@1.4.0/dist/leaflet.js'></script>" +
                "<script src='https://api.windy.com/assets/map-forecast/libBoot.js'></script>" +
                "<style>html, body {margin: 0; padding: 0; height: 100%;}</style>" +
                "</head>" +
                "<body>" +
                "<div id='windy' style='width: 100%; height: 100%;'></div>" +
                "<script>" +
                "var options = {" +
                "  key: 'Q6gJJDpkDS67Esx41VgCznDWG9IcSxdh', " +  // Replace with your Windy API key
                "  lat: 50.4, " +  // Default latitude
                "  lon: 14.3, " +  // Default longitude
                "  zoom: 0, " +    // Initial zoom level
                "  overlay: 'wind' " +  // Default overlay (e.g., wind)
                "};" +
                "windyInit(options, function(windyAPI) {" +
                "  var map = windyAPI.map;" +
                "  var store = windyAPI.store;" +
                "  store.set('overlay', 'wind');  // Add wind layer" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";

        // Load the HTML content into the WebView
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
