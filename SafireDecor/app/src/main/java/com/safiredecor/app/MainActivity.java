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
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

public class MainActivity extends Activity {
    private static final long SPLASH_MS = 1750;
    private static final int GOLD = Color.rgb(141, 206, 96);
    private static final int INK = Color.rgb(10, 28, 12);
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
        handler.postDelayed(this::hideSplash, SPLASH_MS);
        if (state != null) web.restoreState(state); else web.loadUrl(home);
    }

    private View createSplash() {
        FrameLayout box = new FrameLayout(this);
        box.setBackgroundColor(Color.rgb(4, 16, 10));
        box.setElevation(dp(8));
        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.splash_bg);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        box.addView(bg, new FrameLayout.LayoutParams(-1, -1));
        SplashBar bar = new SplashBar(this);
        box.addView(bar, new FrameLayout.LayoutParams(-1, -1));
        bg.setAlpha(0f);
        bg.setScaleX(1.04f);
        bg.setScaleY(1.04f);
        bg.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(450).start();
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(SPLASH_MS);
        anim.setInterpolator(new DecelerateInterpolator(1.4f));
        anim.addUpdateListener(a -> bar.setProgress((float) a.getAnimatedValue()));
        anim.start();
        return box;
    }

    private void hideSplash() {
        if (splash == null || splash.getVisibility() != View.VISIBLE) return;
        splash.animate().alpha(0f).setDuration(250).withEndAction(() -> splash.setVisibility(View.GONE)).start();
    }

    /** Draws an animated loader exactly over the bar baked into splash_bg (940x1672, centerCrop). */
    private static class SplashBar extends View {
        private static final float IW = 940f, IH = 1672f;
        private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF r = new RectF();
        private float progress;
        SplashBar(Context c) {
            super(c);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            track.setColor(Color.rgb(24, 42, 22));
            fill.setColor(Color.rgb(169, 223, 121));
            fill.setShadowLayer(14f, 0f, 0f, Color.argb(200, 169, 223, 121));
        }
        void setProgress(float p) { progress = p; invalidate(); }
        @Override protected void onDraw(Canvas c) {
            float s = Math.max(getWidth() / IW, getHeight() / IH);
            float dx = (getWidth() - IW * s) / 2f, dy = (getHeight() - IH * s) / 2f;
            float left = dx + 293 * s, right = dx + 646 * s, top = dy + 1367 * s, bottom = dy + 1378 * s;
            float rad = (bottom - top) / 2f;
            r.set(left, top, right, bottom);
            c.drawRoundRect(r, rad, rad, track);
            float w = Math.max(bottom - top, (right - left) * (0.08f + 0.92f * progress));
            r.set(left, top + s, left + w, bottom - s);
            c.drawRoundRect(r, rad, rad, fill);
        }
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
        text.setOnClickListener(v -> { box.setVisibility(View.GONE); pageStarted = false; web.reload(); });
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
            @Override public void onReceivedError(WebView v, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) offline.setVisibility(View.VISIBLE);
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
