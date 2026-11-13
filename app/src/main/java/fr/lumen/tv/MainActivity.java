package fr.lumen.tv;

import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

/** Affiche Lumen (hébergée sur le NAS) en plein écran et transmet la télécommande à la page. */
public class MainActivity extends Activity {
    private WebView web;
    private FrameLayout racine;
    private View videoPlein;
    private WebChromeClient.CustomViewCallback videoRappel;
    private boolean erreur = false;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final Runnable reessayer = new Runnable() {
        @Override public void run() { if (web != null && erreur) { erreur = false; web.loadUrl(BuildConfig.LUMEN_URL); } }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        racine = new FrameLayout(this);
        racine.setBackgroundColor(Color.parseColor("#0b0c0f"));
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#0b0c0f"));
        racine.addView(web, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(racine);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        // « AFT » dans le user-agent : Lumen active son mode télé.
        s.setUserAgentString(s.getUserAgentString() + " AFT LumenFireTV");

        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback cb) {
                videoPlein = view; videoRappel = cb;
                racine.addView(view, new FrameLayout.LayoutParams(-1, -1));
            }
            @Override public void onHideCustomView() {
                if (videoPlein != null) racine.removeView(videoPlein);
                videoPlein = null;
                if (videoRappel != null) videoRappel.onCustomViewHidden();
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) afficherErreur();
            }
            @Override public void onReceivedSslError(WebView v, android.webkit.SslErrorHandler h, SslError e) { h.cancel(); }
            @Override public void onPageFinished(WebView v, String url) { if (!url.startsWith("data:")) erreur = false; }
        });
        web.loadUrl(BuildConfig.LUMEN_URL);
    }

    private void afficherErreur() {
        erreur = true;
        String html = "<html><body style='background:#0b0c0f;color:#eee;font-family:sans-serif;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center'>"
                + "<div><h1>Lumen est injoignable</h1><p style='font-size:22px;opacity:.7'>Le NAS est peut-être éteint ou en train de démarrer.<br>Nouvelle tentative automatique…</p>"
                + "<p style='opacity:.5'>" + BuildConfig.LUMEN_URL + "</p></div></body></html>";
        web.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        main.postDelayed(reessayer, 5000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        masquerBarres();
        web.onResume();
        if (erreur) main.postDelayed(reessayer, 500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        web.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);
        if (focus) masquerBarres();
    }

    private void masquerBarres() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /** Envoie une touche à la page sous la forme d'un événement clavier standard. */
    private void touche(String key) {
        web.evaluateJavascript("document.dispatchEvent(new KeyboardEvent('keydown',{key:'" + key + "',bubbles:true,cancelable:true}))", null);
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent e) {
        switch (code) {
            case KeyEvent.KEYCODE_MENU: touche("ContextMenu"); return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE: touche("MediaPlayPause"); return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND: touche("MediaRewind"); return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD: touche("MediaFastForward"); return true;
            case KeyEvent.KEYCODE_BACK:
                if (videoPlein != null) { web.getWebChromeClient().onHideCustomView(); return true; }
                if (web.canGoBack()) { web.goBack(); return true; }
                return true; // on ne quitte jamais Lumen avec Retour
        }
        return super.onKeyDown(code, e);
    }

    @Override
    protected void onDestroy() {
        main.removeCallbacksAndMessages(null);
        web.destroy();
        super.onDestroy();
    }
}
