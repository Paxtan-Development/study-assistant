/*
 * Copyright 2020 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.utils.notes;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.DatabaseFunctions;

import java.util.Random;

/** Notification receiver when the alert of a note is triggered. **/
public class NotesNotifyReceiver extends BroadcastReceiver {
    /** Displays a notification with the message of the note as the title. **/
    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        // Variables that are used in the notification
        String title = context.getPackageName(), message = context.getString(R.string.n_new_alert);
        int alertCode = 0;

        // Show notification
        int noteId = intent.getIntExtra(ActivityConstants.INTENT_VALUE_NOTE_ID, 0);
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(context);
        NotesContent note = database.ContentDao().search(noteId);
        if (note != null) {
            if (note.noteTitle != null && note.noteTitle.length() != 0) {
                title = note.noteTitle;
            }
            if (note.noteContent != null && note.noteContent.length() != 0) {
                message = note.noteContent;
            }
            if (note.alertCode != null) {
                alertCode = note.alertCode;
            }

            note.alertCode = null;
            note.alertDate = null;
        }

        // Display a notification
        Intent startIntent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ActivityConstants.INTENT_VALUE_START_FRAGMENT, true);
        intent.putExtra(ActivityConstants.INTENT_VALUE_NOTE_ID, noteId);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, alertCode, startIntent, 0);
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
