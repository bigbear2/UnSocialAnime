package it.bigbear2sfc.unsocialanime;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.Manifest;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private MainActivity activity;
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getPermissNotify();

        activity = this;
        webView = findViewById(R.id.webview);
        WebView.setWebContentsDebuggingEnabled(true);
        initWebView();


        // Imposta un WebViewClient personalizzato per iniettare JavaScript dopo il caricamento della pagina
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String jsCode = loadJSFromAssets("UnSocialAnime.js");
                if (jsCode != null) {
                    view.evaluateJavascript(jsCode, null);
                }
            }
        });

        // Carica il sito web
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("link")) {
            String link = intent.getStringExtra("link");
            webView.loadUrl("https://socialanime.it/" + link); // Carica il link nella WebView
        } else {
            // Carica la pagina principale
            webView.loadUrl("https://socialanime.it/community");
        }

    }

    private void getPermissNotify(){
        // Controlla e richiedi il permesso se necessario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }
    // Gestisci la risposta dell'utente
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ShowToast("Permesso concesso: puoi inviare notifiche");
            } else {
                ShowToast("Permesso negato: non puoi inviare notifiche");
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        webView.getSettings().setSaveFormData(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setGeolocationEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setUseWideViewPort(getIntent().getBooleanExtra("isSupportZoom", true));
        webView.getSettings().setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setSupportZoom(getIntent().getBooleanExtra("isSupportZoom", true));
            webView.getSettings().setBuiltInZoomControls(true);
        }

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        /*mWebView.setWebChromeClient(new MyWebChromeClient());//重写一下
        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.addJavascriptInterface(new CustomScriptInterface(), "HTMLOUT");*/


    }

    // Metodo per leggere il file JavaScript dalla cartella assets
    private String loadJSFromAssets(String fileName) {
        StringBuilder jsCode = new StringBuilder();
        try {
            InputStream inputStream = getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                jsCode.append(line);
                jsCode.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsCode.toString();
    }

    public void ShowToast(String message) {
        this.runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onBackPressed() {
        // Se la WebView può tornare indietro, torna indietro invece di chiudere l'app
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}