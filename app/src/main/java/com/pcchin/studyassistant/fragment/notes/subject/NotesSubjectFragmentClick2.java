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

package com.pcchin.studyassistant.fragment.notes.subject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.file.notes.exportsubj.ExportSubjectSubject;
import com.pcchin.studyassistant.file.notes.exportsubj.ExportSubjectZip;
import com.pcchin.studyassistant.fragment.notes.NotesSelectFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.NavViewFunctions;
import com.pcchin.studyassistant.utils.notes.NotesNotifyReceiver;

import java.util.ArrayList;

/** 2nd class for the functions for the onClickListeners in the fragment. **/
public class NotesSubjectFragmentClick2 {
    private NotesSubjectFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    public NotesSubjectFragmentClick2(NotesSubjectFragment fragment) {
        this.fragment = fragment;
    }

    /** Renames the subject to another one. **/
    public void onRenamePressed() {
        @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout)
                fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
        // End icon has been set in XML file
        if (popupView.getEditText() != null) {
            popupView.getEditText().setText(fragment.notesSubject);
        }

        DismissibleDialogFragment dismissibleDialog = new DismissibleDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.rename_subject)
                .setView(popupView)
                .create());
        dismissibleDialog.setPositiveButton(fragment.getString(R.string.rename),
                view -> setPositiveButton(dismissibleDialog, popupView));
        dismissibleDialog.setNegativeButton(fragment.getString(android.R.string.cancel), view -> dismissibleDialog.dismiss());
        dismissibleDialog.show(fragment.getParentFragmentManager(), "NotesSubjectFragment.3");
    }

    /** Sets the positive button for the renaming dialog. **/
    private void setPositiveButton(DismissibleDialogFragment dismissibleDialog, @NonNull TextInputLayout popupView) {
        if (popupView.getEditText() != null) {
            String popupInputText = popupView.getEditText().getText().toString();
            // Check if input is blank
            if (popupInputText.replaceAll("\\s+", "")
                    .length() == 0) {
                popupView.setErrorEnabled(true);
                popupView.setError(fragment.getString(R.string.n_error_subject_empty));
            } else if (fragment.subjectDatabase.SubjectDao().search(popupInputText) != null) {
                popupView.setErrorEnabled(true);
                popupView.setError(fragment.getString(R.string.error_subject_exists));
            } else {
                moveSubject((MainActivity) fragment.requireActivity(), dismissibleDialog, popupInputText);
            }
        }
    }

    /** Moves the subject to another name. **/
    private void moveSubject(MainActivity activity, @NonNull DismissibleDialogFragment dialogInterface,
                             String popupInputText) {
        // Move subject
        dialogInterface.dismiss();
        NotesSubject subject = fragment.subjectDatabase.SubjectDao().search(fragment.notesSubject);
        NotesSubject newSubject = new NotesSubject(popupInputText,
                subject.contents, subject.sortOrder);
        fragment.subjectDatabase.SubjectDao().insert(newSubject);
        fragment.subjectDatabase.SubjectDao().delete(subject);
        fragment.subjectDatabase.close();

        // Display new subject
        Toast.makeText(fragment.requireActivity(), R.string.n2_subject_renamed,
                Toast.LENGTH_SHORT).show();
        NavViewFunctions.updateNavView(activity);
        activity.displayFragment(NotesSubjectFragment
                .newInstance(popupInputText));
    }

    /** Export all the notes of the subject into a ZIP file,
     * askZipPassword() and exportSubject() separated for clarity. **/
    public void onExportPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(fragment.requireContext(), Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(fragment.requireContext(), R.string
                    .error_write_permission_denied, Toast.LENGTH_SHORT).show();
        } else {
            new DefaultDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                    .setTitle(R.string.n2_export_format)
                    .setItems(R.array.n_import_subject_format, (dialogInterface, i) ->
                            new Handler().post(() -> {
                                if (i == 0) new ExportSubjectZip(fragment, fragment.subjectDatabase,
                                        fragment.notesArray, fragment.notesSubject).askZipPassword();
                                else new ExportSubjectSubject(fragment, fragment.notesSubject,
                                        fragment.notesArray, fragment.subjectDatabase.SubjectDao()
                                        .search(fragment.notesSubject).sortOrder).exportSubject();
                            }))
                    .setNegativeButton(android.R.string.cancel, null)
                    .create())
                    .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.4");
        }
    }

    /** Deletes the current subject and returns to
     * @see NotesSelectFragment **/
    public void onDeletePressed() {
        new DefaultDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.del)
                .setMessage(R.string.n2_del_confirm)
                .setPositiveButton(R.string.del, (dialog, which) -> deleteSubject(fragment.requireActivity()))
                .setNegativeButton(android.R.string.cancel, null)
                .create())
                .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.7");
    }

    /** Deletes the subject from the database and cleans up any remaining resources. **/
    private void deleteSubject(Activity activity) {
        deletePhantomAlerts(activity);

        // Deletes subject from database
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(fragment.requireActivity());
        NotesSubject delTarget = database.SubjectDao().search(fragment.notesSubject);
        if (delTarget != null) {
            database.SubjectDao().delete(delTarget);
        }
        database.close();
        // Return to NotesSelectFragment
        Toast.makeText(fragment.requireContext(), R.string.n2_deleted, Toast.LENGTH_SHORT).show();
        NavViewFunctions.updateNavView((MainActivity) activity);
        fragment.subjectDatabase.close();
        ((MainActivity) activity).displayFragment(new NotesSelectFragment());
    }

    /** Deletes any alerts that belong to the subject. **/
    private void deletePhantomAlerts(Activity activity) {
        for (ArrayList<String> note: fragment.notesArray) {
            AlarmManager manager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            if (manager != null && note.size() >= 6 && note.get(5) != null
                    && note.get(0) != null && note.get(2) != null) {
                // Get PendingIntent for note alert
                Intent intent = new Intent(fragment.requireActivity(), NotesNotifyReceiver.class);
                intent.putExtra(ActivityConstants.INTENT_VALUE_TITLE, note.get(0));
                intent.putExtra(ActivityConstants.INTENT_VALUE_MESSAGE, note.get(2));
                intent.putExtra(ActivityConstants.INTENT_VALUE_REQUEST_CODE, note.get(5));
                PendingIntent alertIntent = PendingIntent.getBroadcast(
                        fragment.requireActivity(), Integer.parseInt(note.get(5)), intent, 0);

                manager.cancel(alertIntent);
            }
        }
    }
}
