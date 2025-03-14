package it.bigbear2sfc.unsocialanime;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

public class WebAppInterface {
    private static final String LINK_MAIN = "https://socialanime.it/community";

    private final MainActivity activity;
    private static final String CHANNEL_ID = "social_anime_channel";
    private String LastNotify = "";
    private final List<String> LinkNotifiche = new ArrayList<String>();

    // Costruttore
    public WebAppInterface(MainActivity activity) {
        this.activity = activity;

        createNotificationChannel();
    }

    // Metodo esposto a JavaScript
    @JavascriptInterface
    public void androidNotification(String notifica, boolean isJson) {

        if (isJson) {
            parseJsonWithGson(notifica);
        } else {
            if (Objects.equals(notifica, LastNotify)) return;
            LastNotify = notifica;

            this.activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Notifica ricevuta: " + notifica, Toast.LENGTH_SHORT).show();
            });

            int randomNumber = (int) (Math.random() * 21);
            createNotification("Nuova Notifica", notifica, LINK_MAIN, "", randomNumber + 100);
        }
    }

    @JavascriptInterface
    public void androidMenu() {

        this.activity.runOnUiThread(activity::showDialog);

    }

    // Crea un canale di notifica (obbligatorio per Android 8.0 e superiori)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "SocialAnime Notifiche";
            String description = "Canale per le notifiche di SocialAnime";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Registra il canale con il sistema
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification(String title, String message, String link, String iconUrl, int id) {

        if (!Objects.equals(link, LINK_MAIN)) {
            if (LinkNotifiche.contains(link)) return;
            LinkNotifiche.add(link);
        }

        NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("link", link); // Passa il link come extra
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, id, intent, PendingIntent.FLAG_IMMUTABLE);


        // Crea la notifica
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Icona predefinita
                .setContentTitle(title) // Titolo della notifica
                .setContentText(message) // Messaggio della notifica
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorità della notifica
                .setContentIntent(pendingIntent) // Aggiungi l'intent

                .setAutoCancel(true); // Chiudi la notifica quando viene cliccata

        // Se è presente un URL dell'icona, scarica l'immagine e impostala come icona personalizzata
        if (iconUrl != null && !iconUrl.isEmpty()) {
            downloadImageAndSetIcon(notificationBuilder, iconUrl, id, notificationManager);
        } else {
            notificationManager.notify(id, notificationBuilder.build());
        }
    }

    private void downloadImageAndSetIcon(NotificationCompat.Builder notificationBuilder, String iconUrl, int id, NotificationManager notificationManager) {
        Glide.with(activity)
                .asBitmap()
                .load(iconUrl)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                        notificationBuilder.setLargeIcon(bitmap);
                        notificationManager.notify(id, notificationBuilder.build());
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        notificationManager.notify(id, notificationBuilder.build());
                    }
                });
    }

    public void parseJsonWithGson(String jsonString) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Notifica>>() {
        }.getType();

        List<Notifica> notifiche = gson.fromJson(jsonString, listType);

        int id = 1;
        for (Notifica notifica : notifiche) {

            createNotification(notifica.getTitolo(),
                    notifica.getDescrizione(),
                    notifica.getLink(),
                    notifica.getIcona(),
                    notifica.getId());
            id++;

            /*System.out.println("ID: " + notifica.getId());
            System.out.println("Titolo: " + notifica.getTitolo());
            System.out.println("Descrizione: " + notifica.getDescrizione());
            System.out.println("Link: " + notifica.getLink());
            System.out.println("Data: " + notifica.getData());
            System.out.println("Icona: " + notifica.getIcona());
            System.out.println("-----------------------------");*/
        }
    }

    @GlideModule
    public class MyAppGlideModule extends AppGlideModule {
        @Override
        public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
            // Imposta la dimensione della cache su disco (es. 100 MB)
            int diskCacheSizeBytes = 100 * 1024 * 1024; // 100 MB
            builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
        }
    }
}