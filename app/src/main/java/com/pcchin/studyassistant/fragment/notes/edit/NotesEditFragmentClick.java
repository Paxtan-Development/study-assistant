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

package com.pcchin.studyassistant.fragment.notes.edit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.utils.notes.NotesNotifyReceiver;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/** Functions used when the fragment is clicked. **/
public final class NotesEditFragmentClick {
    private NotesEditFragment fragment;
    private View fragmentView;
    private MainActivity activity;
    
    /** Constructor used as fragment needs to be passed on. **/
    public NotesEditFragmentClick(@NonNull NotesEditFragment fragment) {
        this.fragment = fragment;
        this.fragmentView = fragment.getView();
        this.activity = (MainActivity) fragment.requireActivity();
    }

    /** Changes the fragment.subject that the note will be saved to. **/
    public void onSubjPressed() {
        final Spinner subjListSpinner = new Spinner(activity);
        // Get all fragment.subject titles
        List<String> subjTitleList = new ArrayList<>();
        if (fragment.subjModified) {
            subjTitleList.add(fragment.targetNotesSubject);
        } else {
            subjTitleList.add(fragment.notesSubject);
        }
        List<NotesSubject> allSubjList = fragment.database.SubjectDao().getAll();
        for (NotesSubject subject : allSubjList) {
            if ((fragment.subjModified && !Objects.equals(subject.title, fragment.targetNotesSubject))
                    || (!fragment.subjModified && !Objects.equals(subject.title, fragment.notesSubject))) {
                subjTitleList.add(subject.title);
            }
        }
        showSubjDialog(subjListSpinner, subjTitleList);
    }
    
    /** Show the dialog for changing the subject of the note. **/
    private void showSubjDialog(@NonNull Spinner subjListSpinner, List<String> subjTitleList) {
        // Set spinner adaptor
        ArrayAdapter<String> subjAdaptor = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, subjTitleList);
        subjAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjListSpinner.setAdapter(subjAdaptor);

        new DefaultDialogFragment(new AlertDialog.Builder(activity)
                .setTitle(R.string.n_change_subj)
                .setView(subjListSpinner)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    fragment.subjModified = true;
                    fragment.targetNotesSubject = subjListSpinner.getSelectedItem().toString();
                    activity.setTitle(fragment.targetNotesSubject);
                    fragment.targetSubjContents = fragment.database.SubjectDao()
                            .search(fragment.targetNotesSubject).contents;
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create())
                .show(fragment.getParentFragmentManager(), "NotesEditFragment.1");
    }

    /** Saves the note to the fragment.subject selected. **/
    public void onSavePressed() {
        // Check if title is empty
        if (fragmentView != null && ((EditText) fragmentView
                .findViewById(R.id.n4_title)).getText().toString()
                .replaceAll("\\s+", "").length() > 0) {
            // Save original as ArrayList
            ArrayList<String> previousNote;
            if (fragment.hasParent) {
                previousNote = fragment.subjContents.get(fragment.notesOrder);
            } else {
                previousNote = new ArrayList<>();
            }
            createUpdatedNote(previousNote);
        } else {
            Toast.makeText(activity, R.string.n2_error_note_title_empty, Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates an updated note. **/
    private void createUpdatedNote(ArrayList<String> previousNote) {
        ArrayList<String> updatedNote = new ArrayList<>();
        FileFunctions.checkNoteIntegrity(updatedNote);
        updatedNote.set(0, ((EditText) fragmentView.findViewById(R.id.n4_title)).getText().toString());
        updatedNote.set(1, ConverterFunctions.standardDateTimeFormat.format(new Date()));
        updatedNote.set(2, ((EditText) fragmentView.findViewById(R.id.n4_edit)).getText().toString());

        AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        if (manager != null && previousNote.size() >= 6 && previousNote.get(5) != null) {
            updateNoteNotif(manager, previousNote, updatedNote);
        }

        // Toast at start as different objects have different displayFragments
        Toast.makeText(activity, R.string.n4_note_saved, Toast.LENGTH_SHORT).show();
        if (fragment.subjModified && !Objects.equals(fragment.targetNotesSubject, fragment.notesSubject)) {
            modifyNoteSubject(updatedNote);
        } else {
            modifyNote(updatedNote);
        }
        fragment.database.close();
    }
    
    /** Updates the notification alert of the note. **/
    private void updateNoteNotif(@NonNull AlarmManager manager, @NonNull ArrayList<String> previousNote,
                                 @NonNull ArrayList<String> updatedNote) {
        // Delete old notification
        Intent previousIntent = new Intent(activity, NotesNotifyReceiver.class);
        previousIntent.putExtra(ActivityConstants.INTENT_VALUE_TITLE, previousNote.get(0));
        previousIntent.putExtra(ActivityConstants.INTENT_VALUE_MESSAGE, previousNote.get(2));
        previousIntent.putExtra(ActivityConstants.INTENT_VALUE_SUBJECT, fragment.notesSubject);
        previousIntent.putExtra(ActivityConstants.INTENT_VALUE_REQUEST_CODE, previousNote.get(5));
        manager.cancel(PendingIntent.getBroadcast(activity, Integer.parseInt(
                previousNote.get(5)), previousIntent, 0));

        // Set new notification
        Intent newIntent = new Intent(activity, NotesNotifyReceiver.class);
        newIntent.putExtra(ActivityConstants.INTENT_VALUE_TITLE, updatedNote.get(0));
        newIntent.putExtra(ActivityConstants.INTENT_VALUE_MESSAGE, updatedNote.get(2));
        newIntent.putExtra(ActivityConstants.INTENT_VALUE_SUBJECT, fragment.targetNotesSubject);
        newIntent.putExtra(ActivityConstants.INTENT_VALUE_REQUEST_CODE, previousNote.get(5));
        try {
            updateNoteAlert(manager, newIntent, previousNote, updatedNote);
        } catch (ParseException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Parse Error: Date " + previousNote.get(4)
                    + " could not be parsed under standard date time format.");
        }
    }
    
    /** Updates the alert of the note. **/
    private void updateNoteAlert(AlarmManager manager, Intent newIntent,
                                 @NonNull ArrayList<String> previousNote, ArrayList<String> updatedNote) throws ParseException {
        Date targetDate = ConverterFunctions.standardDateTimeFormat.parse(previousNote.get(4));
        if (targetDate != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                manager.setExactAndAllowWhileIdle(AlarmManager.RTC, targetDate.getTime(),
                        PendingIntent.getBroadcast(activity,
                                Integer.parseInt(previousNote.get(5)), newIntent, 0));
            } else {
                manager.setExact(AlarmManager.RTC, targetDate.getTime(), PendingIntent
                        .getBroadcast(activity,
                                Integer.parseInt(previousNote.get(5)), newIntent, 0));
            }
            // Updates value to note
            updatedNote.set(4, ConverterFunctions.standardDateTimeFormat.format(targetDate));
            updatedNote.set(5, previousNote.get(5));
        }
    }
    
    /** Modifies a note whose subject have been changed. **/
    private void modifyNoteSubject(ArrayList<String> updatedNote) {
        if (fragment.hasParent) {
            // Delete original
            fragment.subjContents.remove(fragment.notesOrder);
            fragment.subject.contents = fragment.subjContents;
            fragment.database.SubjectDao().update(fragment.subject);
        }
        // Add new note to new fragment.subject
        fragment.targetSubjContents.add(updatedNote);
        NotesSubject targetSubject = fragment.database.SubjectDao().search(fragment.targetNotesSubject);
        if (targetSubject != null) {
            targetSubject.contents = fragment.targetSubjContents;
        }
        fragment.database.SubjectDao().update(targetSubject);

        // Go to NotesViewFragment
        activity.displayNotes(fragment.targetNotesSubject, fragment.targetSubjContents.size());
        activity.pager.setCurrentItem(fragment.targetSubjContents.size() - 1);
    }
    
    /** Modifies an existing note with the updated version. **/
    private void modifyNote(ArrayList<String> updatedNote) {
        if (fragment.hasParent) {
            // Modify original
            fragment.subjContents.set(fragment.notesOrder, updatedNote);
            fragment.subject.contents = fragment.subjContents;
            fragment.database.SubjectDao().update(fragment.subject);
            activity.displayNotes(fragment.notesSubject, fragment.subjContents.size());
            activity.pager.setCurrentItem(fragment.notesOrder);
        } else {
            // Add new note
            fragment.subjContents.add(updatedNote);
            fragment.subject.contents = fragment.subjContents;
            fragment.database.SubjectDao().update(fragment.subject);
            activity.displayNotes(fragment.notesSubject, fragment.subjContents.size());
            activity.pager.setCurrentItem(fragment.subjContents.size() - 1);
        }
    }

    /** Cancel all the changes and return to
     * @see NotesSubjectFragment **/
    public void onCancelPressed() {
        // Go back to NotesViewFragment of fragment.subject
        if (fragment.hasParent) {
            activity.displayNotes(fragment.notesSubject, fragment.subjContents.size());
            activity.pager.setCurrentItem(fragment.notesOrder, false);
        } else {
            activity.displayFragment(NotesSubjectFragment
                    .newInstance(fragment.notesSubject));
        }
    }
}
