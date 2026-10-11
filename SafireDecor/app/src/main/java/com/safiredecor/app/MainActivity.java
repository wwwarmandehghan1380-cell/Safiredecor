package com.safiredecor.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {
    private static final int GOLD = Color.rgb(196, 154, 90);
    private static final int INK = Color.rgb(23, 19, 16);
    private static final int CREAM = Color.rgb(247, 241, 231);
    private final Handler handler = new Handler();
    private WebView web;
    private View splash;
    private View offline;
    private ProgressBar progress;
    private String home;
    private String baseHost;
    private boolean pageStarted;
    private long splashStarted;

    @SuppressLint("SetJavaScriptEnabled")
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        home = getString(R.string.site_url);
        String host = Uri.parse(home).getHost();
        baseHost = host == null ? "safiredecor.ir" : host.replaceFirst("^www\\.", "");

        FrameLayout root = new FrameLayout(this);
        web = new WebView(this);
        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(GOLD));
        progress.setBackgroundColor(Color.TRANSPARENT);
        progress.setVisibility(View.GONE);
        splash = createSplash();
        offline = createOffline();

        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(-1, dp(4));
        pp.gravity = Gravity.TOP;
        root.addView(progress, pp);
        root.addView(offline, new FrameLayout.LayoutParams(-1, -1));
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);

        configureWebView();
        splashStarted = System.currentTimeMillis();
        if (state != null) web.restoreState(state); else web.loadUrl(home);
    }

    private View createSplash() {
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(INK);
        box.setElevation(dp(8));
        TextView content = new TextView(this);
        content.setGravity(Gravity.CENTER);
        content.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        content.setTextColor(CREAM);
        content.setTypeface(Typeface.create("sans", Typeface.NORMAL));
        content.setTextSize(30);
        content.setLineSpacing(0, 1.2f);
        content.setText("سفیر دکور\n");
        content.append(getString(R.string.slogan));
        content.setTextColor(CREAM);
        content.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -2);
        cp.gravity = Gravity.CENTER;
        cp.setMargins(dp(28), 0, dp(28), dp(48));
        box.addView(content, cp);

        TextView mark = new TextView(this);
        mark.setText("س");
        mark.setGravity(Gravity.CENTER);
        mark.setTextColor(INK);
        mark.setTextSize(30);
        mark.setTypeface(Typeface.DEFAULT_BOLD);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(GOLD);
        mark.setBackground(circle);
        FrameLayout.LayoutParams mp = new FrameLayout.LayoutParams(dp(64), dp(64));
        mp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        mp.bottomMargin = dp(145);
        box.addView(mark, mp);

        ProgressBar loader = new ProgressBar(this);
        loader.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(GOLD));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(28), dp(28));
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        lp.bottomMargin = dp(76);
        box.addView(loader, lp);
        return box;
    }

    private View createOffline() {
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(CREAM);
        TextView text = new TextView(this);
        text.setText(getString(R.string.offline) + "\n\n" + getString(R.string.retry));
        text.setGravity(Gravity.CENTER);
        text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        text.setTextSize(17);
        text.setTextColor(INK);
        text.setOnClickListener(v -> { box.setVisibility(View.GONE); splash.setVisibility(View.VISIBLE); pageStarted = false; web.reload(); });
        box.addView(text, new FrameLayout.LayoutParams(-1, -1));
        box.setVisibility(View.GONE);
        return box;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        web.setBackgroundColor(Color.WHITE);
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value > 0 && value < 100 ? View.VISIBLE : View.GONE);
            }
        });
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        String ua = s.getUserAgentString();
        if (!ua.contains("Mobile")) s.setUserAgentString(ua + " Mobile");
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                Uri u = request.getUrl();
                String h = u.getHost();
                String scheme = u.getScheme();
                boolean internal = ("http".equals(scheme) || "https".equals(scheme)) && isSiteHost(h);
                if (internal) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                return true;
            }
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap icon) {
                pageStarted = true;
                offline.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView v, String url) {
                long wait = Math.max(0, 1100 - (System.currentTimeMillis() - splashStarted));
                handler.postDelayed(() -> { if (splash != null) splash.setVisibility(View.GONE); }, wait);
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) { splash.setVisibility(View.GONE); offline.setVisibility(View.VISIBLE); }
            }
            @Override public void onReceivedSslError(WebView v, SslErrorHandler h, android.net.http.SslError e) { h.cancel(); }
        });
    }

    private boolean isSiteHost(String host) {
        return host != null && (host.equals(baseHost) || host.endsWith("." + baseHost));
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }
    @Override public void onBackPressed() { if (web.canGoBack()) web.goBack(); else super.onBackPressed(); }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); web.destroy(); super.onDestroy(); }
}
