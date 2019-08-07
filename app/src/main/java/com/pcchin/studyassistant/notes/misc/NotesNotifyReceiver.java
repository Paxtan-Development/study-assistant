/*
 * Copyright 2019 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.notes.misc;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

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
        Log.d("Test", "Alert notified");
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
        NotificationCompat.Builder notif = new NotificationCompat.Builder(context, context.getPackageName())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setLights(Color.BLUE, 2000, 0)
                .setVibrate(new long[]{0, 250, 250, 250, 250})
                .setAutoCancel(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notif.setChannelId(context.getString(R.string.notif_channel_notes_ID));
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(new Random().nextInt(100), notif.build());
    }
}
