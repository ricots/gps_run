package com.acer.run_gps.gui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.acer.run_gps.R;

/**
 * Created by Fabian on 08.02.2016.
 */
public class PrivacyPolicy extends Activity {

    private WebView webView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.privacy_policy_activity);

        webView = (WebView) findViewById(R.id.webView);
        webView.loadUrl("http://www.ffeichta.com/runnergy/privacy-policy.html");
    }
}