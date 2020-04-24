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
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.notessubject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.ArrayList;
import java.util.Objects;

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
    void importSubjectToDatabase(@NonNull String title, ArrayList<ArrayList<String>> contents,
                                         int sortOrder) {
        if (title.length() > 0) {
            SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);
            // Import to notes
            if (database.SubjectDao().search(title) != null) {
                // Subject conflict
                new AutoDismissDialog(activity.getString(R.string.subject_conflict),
                        activity.getString(R.string.error_subject_same_name),
                        new String[]{activity.getString(R.string.merge),
                                activity.getString(R.string.rename),
                                activity.getString(android.R.string.cancel)},
                        new DialogInterface.OnClickListener[]{(dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            mergeSubjects(title, contents);
                        }, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            showRenameDialog(title, contents, sortOrder);
                        }, (dialogInterface, i) -> dialogInterface.dismiss()})
                        .show(activity.getSupportFragmentManager(), "ImportSubject.4");
            } else {
                database.SubjectDao().insert(new NotesSubject(title, contents, sortOrder));
                Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(title));
            }
            database.close();
        } else {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Title of subject in ZIP file  invalid.");
            Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    /** Display the renaming dialog for the conflicted subject.
     * Separated from importSubjectToDatabase(String title,
     * ArrayList<ArrayList<String>> contents, int sortOrder) for clarity. **/
    private void showRenameDialog(String title, ArrayList<ArrayList<String>> contents, int sortOrder) {
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) inputLayout.getEditText().setText(title);
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);

        DialogInterface.OnShowListener renameListener =
                dialogInterface -> setRenameDialogPositiveListener(dialogInterface, inputLayout, database, contents, sortOrder);
        AutoDismissDialog renameDialog = new AutoDismissDialog(
                activity.getString(R.string.rename_subject), inputLayout,
                new String[]{activity.getString(android.R.string.ok),
                        activity.getString(android.R.string.cancel), ""}, renameListener);
        renameDialog.setDismissListener(dialogInterface -> database.close());
        renameDialog.show(activity.getSupportFragmentManager(), "ImportSubject.5");
    }

    /** Sets the onClickListener for the rename dialog. **/
    private void setRenameDialogPositiveListener(DialogInterface dialogInterface,
                                                 TextInputLayout inputLayout,
                                                 SubjectDatabase database,
                                                 ArrayList<ArrayList<String>> contents,
                                                 int sortOrder) {
        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String inputText = "";
            if (inputLayout.getEditText() != null) {
                inputText = inputLayout.getEditText().getText().toString();
            }
            if (inputText.length() > 0 && database.SubjectDao().search(inputText) == null) {
                // Import subject into notes
                database.SubjectDao().insert(new NotesSubject(inputText, contents, sortOrder));
                dialogInterface.dismiss();
                Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(inputText));
            } else if (inputText.length() > 0) {
                inputLayout.setErrorEnabled(true);
                inputLayout.setError(activity.getString(R.string.error_subject_exists));
            } else {
                Log.w(MainActivity.LOG_APP_NAME, "TextInputLayout Error: getEditText() for " +
                        "AlertDialog in ImportSubject.showRenameDialog not found.");
            }
        });
        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                .setOnClickListener(view -> dialogInterface.dismiss());
    }

    /** Merge two conflicted subjects with the same name.
     * Notes that are exactly the same will not be re-imported,
     * sort order will inherit the original subject stored on the notes. **/
    private void mergeSubjects(String title, ArrayList<ArrayList<String>> newContent) {
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);
        NotesSubject editSubject = database.SubjectDao().search(title);
        ArrayList<ArrayList<String>> oldContents = editSubject.contents;

        // Compare and add notes if necessary
        int oldContentsSize = oldContents.size();
        if (newContent != null && newContent.size() > 0) {
            for (ArrayList<String> note: newContent) {
                // Compare each note to the former notes in the old content
                boolean notePresent = false;
                // A for loop is used to prevent the new notes from being compared to
                for (int i = 0; i < oldContentsSize; i++) {
                    if (Objects.equals(note, oldContents.get(i))) {
                        notePresent = true;
                        break;
                    }
                }

                if (!notePresent) {
                    // Add note to oldContents
                    oldContents.add(note);
                }
            }
        }

        editSubject.contents = oldContents;
        database.SubjectDao().update(editSubject);
        database.close();
        // Go to the fragment
        Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
        activity.safeOnBackPressed();
        activity.displayFragment(NotesSubjectFragment.newInstance(title));
    }
}
