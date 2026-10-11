package com.safiredecor.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar bar;
    private TextView offline;
    private String home;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        home = getString(R.string.site_url);
        String host = Uri.parse(home).getHost();
        final String baseHost = host == null ? "" : host.replaceFirst("^www\\.", "");

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        offline = new TextView(this);
        offline.setText(R.string.offline);
        offline.setGravity(Gravity.CENTER);
        offline.setTextSize(18);
        offline.setTextColor(Color.parseColor("#16305C"));
        offline.setBackgroundColor(Color.parseColor("#F2EAD8"));
        offline.setVisibility(View.GONE);
        offline.setOnClickListener(v -> { offline.setVisibility(View.GONE); web.reload(); });

        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        root.addView(bar, new FrameLayout.LayoutParams(-1, 8));
        root.addView(offline, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView v, int p) {
                bar.setProgress(p);
                bar.setVisibility(p < 100 ? View.VISIBLE : View.GONE);
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                String h = u.getHost();
                String scheme = u.getScheme();
                boolean internal = ("http".equals(scheme) || "https".equals(scheme))
                        && h != null && h.endsWith(baseHost);
                if (internal) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true; // tel:, whatsapp, instagram, external sites open outside
            }
            @Override public void onPageStarted(WebView v, String url, Bitmap f) { bar.setVisibility(View.VISIBLE); }
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) offline.setVisibility(View.VISIBLE);
            }
        });

        if (state != null) web.restoreState(state); else web.loadUrl(home);
    }

    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
