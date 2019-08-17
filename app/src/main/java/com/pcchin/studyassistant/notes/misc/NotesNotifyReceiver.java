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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.main.MainActivity;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

/** Notification receiver when the alert of a note is triggered. **/
public class NotesNotifyReceiver extends BroadcastReceiver {
    /** Displays a notification with the message of the note as the title. **/
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show notification
        String title = intent.getStringExtra(MainActivity.INTENT_VALUE_TITLE);
        String message = intent.getStringExtra(MainActivity.INTENT_VALUE_MESSAGE);
        String subjectTitle = intent.getStringExtra(MainActivity.INTENT_VALUE_SUBJECT);
        String requestCode = intent.getStringExtra(MainActivity.INTENT_VALUE_REQUEST_CODE);
        if (title == null || title.length() == 0) {
            title = context.getPackageName();
        }
        if (message == null || message.length() == 0) {
            message = context.getString(R.string.n_new_alert);
        }

        // Clear data from database
        SubjectDatabase database = Room.databaseBuilder(context, SubjectDatabase.class,
                MainActivity.DATABASE_NOTES)
                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                .allowMainThreadQueries().build();
        NotesSubject subject = database.SubjectDao().search(subjectTitle);
        if (subject != null) {
            ArrayList<ArrayList<String>> notesList = subject.contents;
            for (ArrayList<String> note: notesList) {
                if (note != null && note.size() >= 6 && Objects.equals(note.get(0), title) &&
                        Objects.equals(note.get(5), requestCode)) {
                    note.set(4, null);
                    note.set(5, null);
                }
            }
            database.SubjectDao().update(subject);
        }
        database.close();

        // Display a notification
        Intent startIntent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(MainActivity.INTENT_VALUE_START_FRAGMENT, true);
        intent.putExtra(MainActivity.INTENT_VALUE_SUBJECT, subjectTitle);
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
