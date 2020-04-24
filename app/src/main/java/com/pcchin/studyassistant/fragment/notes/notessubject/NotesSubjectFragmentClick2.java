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

package com.pcchin.studyassistant.fragment.notes.notessubject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.file.notes.exportsubj.ExportSubjectSubject;
import com.pcchin.studyassistant.file.notes.exportsubj.ExportSubjectZip;
import com.pcchin.studyassistant.fragment.notes.NotesSelectFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;
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
        if (fragment.getActivity() != null) {
            @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout)
                    fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
            // End icon has been set in XML file
            if (popupView.getEditText() != null) {
                popupView.getEditText().setText(fragment.notesSubject);
            }
            DialogInterface.OnShowListener nListener = dialogInterface ->
                    setRenameDialogButton((AlertDialog) dialogInterface, popupView);
            new AutoDismissDialog(fragment.getString(R.string.rename_subject), popupView,
                    new String[]{fragment.getString(R.string.rename),
                            fragment.getString(android.R.string.cancel), ""}, nListener)
                    .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.3");
        }
    }

    /** Sets the onClickListeners for the buttons in the renaming dialog. **/
    private void setRenameDialogButton(@NonNull AlertDialog dialogInterface, TextInputLayout popupView) {
        dialogInterface.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
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
                    moveSubject((MainActivity) fragment.getActivity(), dialogInterface, popupInputText);
                }
            }
        });
        dialogInterface.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setOnClickListener(view -> dialogInterface.dismiss());
    }

    /** Moves the subject to another name. **/
    private void moveSubject(MainActivity activity, @NonNull DialogInterface dialogInterface,
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
        Toast.makeText(fragment.getActivity(), R.string.n2_subject_renamed,
                Toast.LENGTH_SHORT).show();
        UIFunctions.updateNavView(activity);
        activity.displayFragment(NotesSubjectFragment
                .newInstance(popupInputText));
    }

    /** Export all the notes of the subject into a ZIP file,
     * askZipPassword() and exportSubject() separated for clarity. **/
    public void onExportPressed() {
        if (fragment.getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(fragment.getContext(), Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(fragment.getContext(), R.string
                    .error_write_permission_denied, Toast.LENGTH_SHORT).show();
        } else {
            new AutoDismissDialog(fragment.getString(R.string.n2_export_format),
                    R.array.n_import_subject_format, (dialogInterface, i) ->
                    new Handler().post(() -> {
                        if (i == 0) new ExportSubjectZip(fragment, fragment.subjectDatabase, 
                                fragment.notesArray, fragment.notesSubject).askZipPassword();
                        else new ExportSubjectSubject(fragment, fragment.notesSubject, 
                                fragment.notesArray, fragment.subjectDatabase.SubjectDao()
                                .search(fragment.notesSubject).sortOrder).exportSubject();
                    }), new String[]{"", fragment.getString(android.R.string.cancel), ""},
                    new DialogInterface.OnClickListener[]{null, null, null})
                    .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.4");
        }
    }

    /** Deletes the current subject and returns to
     * @see NotesSelectFragment **/
    public void onDeletePressed() {
        new AutoDismissDialog(fragment.getString(R.string.del), fragment.getString(R.string.n2_del_confirm),
                new String[]{fragment.getString(R.string.del), fragment.getString(android.R.string.cancel), ""},
                new DialogInterface.OnClickListener[]{(dialog, which) -> deleteSubject(fragment.getActivity()),
                        (dialog, which) -> dialog.dismiss(), null})
                .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.7");
    }

    /** Deletes the subject from the database and cleans up any remaining resources. **/
    private void deleteSubject(Activity activity) {
        deletePhantomAlerts(activity);

        // Deletes subject from database
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(fragment.getActivity());
        NotesSubject delTarget = database.SubjectDao().search(fragment.notesSubject);
        if (delTarget != null) {
            database.SubjectDao().delete(delTarget);
        }
        database.close();
        // Return to NotesSelectFragment
        Toast.makeText(fragment.getContext(), R.string.n2_deleted, Toast.LENGTH_SHORT).show();
        UIFunctions.updateNavView((MainActivity) activity);
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
                Intent intent = new Intent(fragment.getActivity(), NotesNotifyReceiver.class);
                intent.putExtra(MainActivity.INTENT_VALUE_TITLE, note.get(0));
                intent.putExtra(MainActivity.INTENT_VALUE_MESSAGE, note.get(2));
                intent.putExtra(MainActivity.INTENT_VALUE_REQUEST_CODE, note.get(5));
                PendingIntent alertIntent = PendingIntent.getBroadcast(
                        fragment.getActivity(), Integer.parseInt(note.get(5)), intent, 0);

                manager.cancel(alertIntent);
            }
        }
    }
}
