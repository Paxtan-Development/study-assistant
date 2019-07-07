package com.pcchin.studyassistant.notes.misc;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.MainActivity;

import java.util.Random;

/** Notification receiver when the alert of a note is triggered. **/
public class NotesNotifyReceiver extends BroadcastReceiver {
    /** Displays a notification with the message of the note as the title. **/
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show notification
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");
        if (title == null || title.length() == 0) {
            title = context.getPackageName();
        }
        if (message == null || message.length() == 0) {
            message = context.getString(R.string.n_new_alert);
        }

        // Display a notification
        Intent startIntent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, startIntent, 0);
        Notification notif = new NotificationCompat.Builder(context, context.getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLights(Color.BLUE, 2000, 0)
                .setVibrate(new long[]{0, 250, 250, 250, 250})
                .setAutoCancel(true).build();
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(new Random().nextInt(100), notif);
    }
}
