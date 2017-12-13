package com.maxiye.first;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebViewActivity extends AppCompatActivity {

    private WebView webview;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ActionBar ab = getSupportActionBar();
        assert ab != null;ab.hide();
        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.EXTRA_URL);
        webview = findViewById(R.id.webView1);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        webview.loadUrl(url);
    }
    /**
     * 简写toast
     *
     * @param msg 消息
     */
    @SuppressWarnings("unused")
    private void alert(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (webview != null && webview.canGoBack()) {
                    webview.goBack();
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            default:
                break;
        }
        return false;
    }
}
