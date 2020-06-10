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

package com.pcchin.studyassistant.file.notes.importsubj;

import android.annotation.SuppressLint;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;

import java.util.List;

/** Functions used to import subjects from .subject files and ZIP files.
 * Cannot be made static as MainActivity activity needs to be separated for clarity. **/
class ImportSubject {
    private final MainActivity activity;

    /** The constructor for the class as activity needs to be passed on.
     * Originally contained displayImportDialog(MainActivity activity) but moved for better structuring of files.
     * importZipConfirm(String path) and importSubjectFile(String path) separated for clarity. **/
    ImportSubject(MainActivity activity) {
        this.activity = activity;
    }

    /** Import a NotesSubject into the notes.
     * Duplicating titles for subject are checked within the function. **/
    void importSubjectToDatabase(@NonNull NotesSubject subject, List<NotesContent> notesList) {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        NotesSubject conflictSubject = database.SubjectDao().searchByTitle(subject.title);
        if (conflictSubject != null) {
            database.close();
            showConflictDialog(subject, conflictSubject, notesList);
        } else {
            database.SubjectDao().insert(subject);
            for (NotesContent note: notesList) {
                database.ContentDao().insert(note);
            }
            database.close();
            Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
            activity.safeOnBackPressed();
            activity.displayFragment(NotesSubjectFragment.newInstance(subject.subjectId));
        }
    }

    /** Show the subject conflict dialog. **/
    private void showConflictDialog(NotesSubject subject, NotesSubject conflictSubject, List<NotesContent> notesList) {
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.subject_conflict)
                        .setMessage(R.string.error_subject_same_name)
                        .create());
        dismissibleFragment.setPositiveButton(activity.getString(R.string.merge), view -> {
            dismissibleFragment.dismiss();
            mergeSubjects(conflictSubject, notesList);
        });
        dismissibleFragment.setNegativeButton(activity.getString(R.string.rename), view -> {
            dismissibleFragment.dismiss();
            showRenameDialog(subject, notesList);
        });
        dismissibleFragment.setNeutralButton(activity.getString(android.R.string.cancel),
                view -> dismissibleFragment.dismiss());
        dismissibleFragment.show(activity.getSupportFragmentManager(), "ImportSubject.4");
    }

    /** Display the renaming dialog for the conflicted subject.
     * Separated from importSubjectToDatabase for clarity. **/
    private void showRenameDialog(NotesSubject subject, List<NotesContent> notesList) {
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) inputLayout.getEditText().setText(subject.title);
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);

        AlertDialog renameDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.rename_subject)
                .setView(inputLayout)
                .create();
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(renameDialog);
        dismissibleFragment.setPositiveButton(activity.getString(android.R.string.ok),
                view -> setPositiveButton(subject, notesList, dismissibleFragment, inputLayout));
        dismissibleFragment.setNegativeButton(activity.getString(android.R.string.cancel),
                view -> dismissibleFragment.dismiss());
        dismissibleFragment.show(activity.getSupportFragmentManager(), "ImportSubject.5");
    }

    /** Sets the positive button for the renaming dialog. **/
    private void setPositiveButton(NotesSubject subject, List<NotesContent> notesList,
                                   DismissibleDialogFragment dismissibleFragment,
                                   @NonNull TextInputLayout inputLayout) {
        String inputText = "";
        if (inputLayout.getEditText() != null) {
            inputText = inputLayout.getEditText().getText().toString();
        }
        if (inputText.length() > 0) {
            SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
            if (database.SubjectDao().searchByTitle(inputText) == null) {
                // Import subject into notes
                subject.title = inputText;
                dismissibleFragment.dismiss();
                importSubjectToDatabase(subject, notesList);
            } else {
                inputLayout.setErrorEnabled(true);
                inputLayout.setError(activity.getString(R.string.error_subject_exists));
            }
            database.close();
        } else {
            Log.w(ActivityConstants.LOG_APP_NAME, "TextInputLayout Error: getEditText() for " +
                    "AlertDialog in ImportSubject.showRenameDialog not found.");
        }
    }

    /** Merge two conflicted subjects with the same name.
     * Sort order will inherit the original subject stored on the notes. **/
    private void mergeSubjects(NotesSubject conflictSubject, @NonNull List<NotesContent> notesList) {
        // The conflictSubject (The one that is already in the database) will be used as the base
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        // Change the subjectId for notesList and store it into the database
        for (NotesContent note: notesList) {
            note.subjectId = conflictSubject.subjectId;
            database.ContentDao().insert(note);
        }
        database.close();
        // Go to the fragment
        Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
        activity.safeOnBackPressed();
        activity.displayFragment(NotesSubjectFragment.newInstance(conflictSubject.subjectId));
    }
}
