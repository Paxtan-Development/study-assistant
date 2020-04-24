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

package com.pcchin.studyassistant.fragment.notes.view;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.NotesSelectFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;
import com.pcchin.studyassistant.utils.notes.NotesNotifyReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

/** The 2nd class of functions used when the fragment is clicked. **/
public class NotesViewFragmentClick2 {
    private NotesViewFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    public NotesViewFragmentClick2(NotesViewFragment fragment) {
        this.fragment = fragment;
    }

    /** Sets up the alert to notify users at a specific time,
     * separated selectTime(Calendar targetDateTime) for clarity. **/
    public void onAlertPressed() {
        // Select date
        if (fragment.getContext() != null) {
            Toast.makeText(fragment.getContext(), R.string.n3_set_date, Toast.LENGTH_SHORT).show();
            Calendar targetDateTime = Calendar.getInstance();
            Calendar currentCalendar = Calendar.getInstance();
            DatePickerDialog dateDialog = new DatePickerDialog(fragment.getContext(), (datePicker, i, i1, i2) -> {
                targetDateTime.set(Calendar.YEAR, i);
                targetDateTime.set(Calendar.MONTH, i1);
                targetDateTime.set(Calendar.DAY_OF_MONTH, i2);
                selectTime(targetDateTime);
            }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH));
            // Set minimum date so that alert cannot be created for the past
            // -10000 as minimum time cannot be now/in the future
            dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 10000);
            dateDialog.show();
        }
    }

    /** Allows the user to choose the time of the alert,
     * separated from onAlertPressed() for clarity. **/
    private void selectTime(Calendar targetDateTime) {
        Toast.makeText(fragment.getContext(), R.string.n3_set_time, Toast.LENGTH_SHORT).show();
        TimePickerDialog timeDialog = new TimePickerDialog(fragment.getContext(), (timePicker, i, i1) -> {
            // Get time from time picker
            targetDateTime.set(Calendar.HOUR_OF_DAY, i);
            targetDateTime.set(Calendar.MINUTE, i1);
            targetDateTime.set(Calendar.SECOND, 0);
            updateNoteAlertTime(targetDateTime);
        }, Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE),
                DateFormat.is24HourFormat(fragment.getContext()));
        timeDialog.show();
    }

    /** Pass the alert time to updateNoteAlert. **/
    private void updateNoteAlertTime(@NonNull Calendar targetDateTime) {
        if (targetDateTime.after(Calendar.getInstance())) {
            if (fragment.getActivity() != null) {
                // Set alert
                Random rand = new Random();
                int requestCode = rand.nextInt();
                while (requestCode == 0) {
                    // Unlikely, but it could happen
                    requestCode = rand.nextInt();
                }
                if (requestCode < 0) {
                    requestCode = -requestCode;
                }
                updateNoteAlert(fragment.getActivity(), targetDateTime, requestCode);
            }
        } else {
            // The time selected is in the past
            Toast.makeText(fragment.getContext(), R.string.n2_error_time_passed, Toast.LENGTH_SHORT).show();
        }
    }

    /** Updates the alert of the note. **/
    private void updateNoteAlert(@NonNull Activity activity, @NonNull Calendar targetDateTime, int requestCode) {
        AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getNotifyReceiverIntent(requestCode);

        FileFunctions.checkNoteIntegrity(fragment.notesInfo);
        fragment.notesInfo.set(4, ConverterFunctions.standardDateTimeFormat.format(targetDateTime.getTime()));
        fragment.notesInfo.set(5, String.valueOf(requestCode));
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);
        NotesSubject subject = database.SubjectDao().search(fragment.notesSubject);
        if (subject != null) {
            ArrayList<ArrayList<String>> updateArray = subject.contents;
            updateArray.set(fragment.notesOrder, fragment.notesInfo);
            database.SubjectDao().update(subject);
        }
        database.close();
        insertAlarm(manager, alarmIntent, targetDateTime);
    }

    /** Inserts the alarm and resets the menu. **/
    private void insertAlarm(AlarmManager manager, PendingIntent alarmIntent, Calendar targetDateTime) {
        if (manager != null) {
            Log.w(MainActivity.LOG_APP_NAME, ConverterFunctions.standardDateTimeFormat.format(targetDateTime.getTime()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // User may use note alert to do important things, so
                // they would need to be called when when idle
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC,
                        targetDateTime.getTimeInMillis(), alarmIntent);
            } else {
                manager.setExact(AlarmManager.RTC,
                        targetDateTime.getTimeInMillis(), alarmIntent);
            }
            fragment.hasAlert = true;
            Toast.makeText(fragment.getContext(), R.string.n3_alert_set, Toast.LENGTH_SHORT).show();
        }
        GeneralFunctions.reloadFragment(fragment);
    }

    /** Cancels the existing alert. **/
    public void onCancelAlertPressed() {
        if (fragment.getContext() != null) {
            AlarmManager manager = (AlarmManager) fragment.getContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent cancelIntent = getNotifyReceiverIntent(Integer.parseInt(fragment.notesInfo.get(5)));

            if (manager != null) {
                manager.cancel(cancelIntent);
                updateNoteAlertValues();
                fragment.hasAlert = false;
                Toast.makeText(fragment.getContext(), R.string.n3_alert_cancelled, Toast.LENGTH_SHORT).show();

            }
        }
    }

    /** Update the alert value of the note. **/
    private void updateNoteAlertValues() {
        if (fragment.getActivity() != null) {
            fragment.notesInfo.set(4, null);
            fragment.notesInfo.set(5, null);
            SubjectDatabase database = GeneralFunctions.getSubjectDatabase(fragment.getActivity());
            NotesSubject subject = database.SubjectDao().search(fragment.notesSubject);
            if (subject != null) {
                ArrayList<ArrayList<String>> updateArray = subject.contents;
                updateArray.set(fragment.notesOrder, fragment.notesInfo);
                database.SubjectDao().update(subject);
            }
            database.close();
            fragment.getActivity().invalidateOptionsMenu();
        }
    }

    /** Deletes the note from the subject. **/
    public void onDeletePressed() {
        new AutoDismissDialog(fragment.getString(R.string.del), fragment.getString(R.string.n3_del_confirm),
                new String[]{fragment.getString(R.string.del), fragment.getString(android.R.string.cancel), ""},
                new DialogInterface.OnClickListener[]{(dialog, which) -> deleteNote(),
                        (dialog, which) -> dialog.dismiss(), null})
                .show(fragment.getParentFragmentManager(), "NotesViewFragment.4");
    }

    /** Cleans up the subject and deletes the note from the database. **/
    private void deleteNote() {
        if (fragment.getActivity() != null) {
            // Delete listener
            AlarmManager manager = (AlarmManager) fragment.getActivity()
                    .getSystemService(Context.ALARM_SERVICE);
            if (manager != null && fragment.notesInfo.size() >= 5 && fragment.notesInfo.get(5) != null) {
                PendingIntent alertIntent = getNotifyReceiverIntent(
                        Integer.parseInt(fragment.notesInfo.get(5)));
                manager.cancel(alertIntent);
            }

            SubjectDatabase database = GeneralFunctions.getSubjectDatabase(fragment.getActivity());
            NotesSubject currentSubject = database.SubjectDao().search(fragment.notesSubject);
            // If the note does not have a subject, fall back to NotesSelectFragment
            if (fragment.notesSubject != null) deleteNoteFromDatabase(database, currentSubject, (MainActivity) fragment.getActivity());
            else ((MainActivity) fragment.getActivity()).displayFragment(new NotesSelectFragment());
            Toast.makeText(fragment.getContext(), R.string.n3_deleted, Toast.LENGTH_SHORT).show();
        }
    }

    /** Deletes the note from the database. **/
    private void deleteNoteFromDatabase(SubjectDatabase database, @NonNull NotesSubject currentSubject,
                                        MainActivity activity) {
        // Check if contents is valid
        ArrayList<ArrayList<String>> contents = currentSubject.contents;
        if (contents != null) {
            if (fragment.notesOrder < contents.size()) {
                contents.remove(fragment.notesOrder);
            }
        } else {
            contents = new ArrayList<>();
        }
        // Update value in notes
        currentSubject.contents = contents;
        database.SubjectDao().update(currentSubject);
        database.close();
        activity.displayFragment(NotesSubjectFragment.newInstance(fragment.notesSubject));
    }

    /** An intent that passes on the information of the note to the notification receiver.
     * @see NotesNotifyReceiver **/
    private PendingIntent getNotifyReceiverIntent(int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), NotesNotifyReceiver.class);
        intent.putExtra(MainActivity.INTENT_VALUE_TITLE, fragment.notesInfo.get(0));
        intent.putExtra(MainActivity.INTENT_VALUE_MESSAGE, fragment.notesInfo.get(2));
        intent.putExtra(MainActivity.INTENT_VALUE_SUBJECT, fragment.notesSubject);
        intent.putExtra(MainActivity.INTENT_VALUE_REQUEST_CODE, fragment.notesInfo.get(5));
        return PendingIntent.getBroadcast(fragment.getActivity(), requestCode, intent, 0);
    }
}
