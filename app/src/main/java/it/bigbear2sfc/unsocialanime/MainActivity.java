package it.bigbear2sfc.unsocialanime;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 2;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback;

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

        checkAndRequestPermissions();

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
            webView.loadUrl("https://socialanime.it/" + link);
        } else {
            // Carica la pagina principale
            webView.loadUrl("https://socialanime.it/community");
        }
    }

    private void getPermissNotify() {

    }

    private void checkAndRequestPermissions() {

        // Controlla e richiedi il permesso se necessario delle notifiche e dell'accesso ai file
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ShowToast("Permesso concesso: puoi inviare notifiche");
            } else {
                ShowToast("Permesso negato: non puoi inviare notifiche");
            }
        }

        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permesso concesso: puoi accedere ai file");
            } else {
                System.out.println("Permesso negato: non puoi accedere ai file");
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
        webView.getSettings().setUseWideViewPort(getIntent().getBooleanExtra("isSupportZoom", true));
        webView.getSettings().setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            webView.getSettings().setDisplayZoomControls(false);
            webView.getSettings().setSupportZoom(getIntent().getBooleanExtra("isSupportZoom", true));
            webView.getSettings().setBuiltInZoomControls(true);
        }

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        // Configura il WebChromeClient personalizzato
        webView.setWebChromeClient(new MyWebChromeClient());
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // Salva il callback per restituire il file selezionato
            MainActivity.this.filePathCallback = filePathCallback;

            // Avvia l'intent per selezionare un file
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*"); // Accetta tutti i tipi di file
            startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);

            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                // Ottieni l'URI del file selezionato
                results = new Uri[]{data.getData()};
            }
            // Restituisci il risultato al WebView
            filePathCallback.onReceiveValue(results);
            filePathCallback = null; // Resetta il callback
        }
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Infla il menu contestuale
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Gestisci la selezione del menu
        if (item.getTitle().equals("Opzione 1")) {
            return true;
        } else if (item.getTitle().equals("Opzione 2")) {
            return true;
        } else if (item.getTitle().equals("Opzione 3")) {
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scegli un'opzione")
                .setItems(new String[]{"Opzione 1", "Opzione 2", "Opzione 3"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Gestisci la selezione dell'utente
                        switch (which) {
                            case 0:
                                // Azione per l'opzione 1
                                break;
                            case 1:
                                // Azione per l'opzione 2
                                break;
                            case 2:
                                // Azione per l'opzione 3
                                break;
                        }
                    }
                });
        builder.create().show();
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