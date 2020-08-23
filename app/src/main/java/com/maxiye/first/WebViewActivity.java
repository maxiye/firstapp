package com.maxiye.first;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Webview util
 * @author due
 */
public class WebViewActivity extends AppCompatActivity {

    private WebView webview;
    private ProgressBar webProgress;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.hide();
        Intent intent = getIntent();
        String url = intent.getStringExtra(ApplistActivity.EXTRA_URL);
        webview = findViewById(R.id.webView);
        webProgress = findViewById(R.id.webview_progress);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    // 加载完网页进度条消失
                    webProgress.setVisibility(View.GONE);
                } else {
                    // 开始加载网页时显示进度条
                    webProgress.setVisibility(View.VISIBLE);
                    // 设置进度值
                    webProgress.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }
        });
        webview.setDownloadListener((url1, userAgent, contentDisposition, mimetype, contentLength) -> {
            Uri uri = Uri.parse(url1);
            Intent intent1 = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent1);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webview != null && webview.canGoBack()) {
                webview.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (webview != null) {
            webview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            webview.clearHistory();

            ((ViewGroup) webview.getParent()).removeView(webview);
            webview.destroy();
            webview = null;
        }
        super.onDestroy();
    }
}
