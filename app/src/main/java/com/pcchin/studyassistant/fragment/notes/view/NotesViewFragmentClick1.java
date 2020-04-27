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

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.NotesEditFragment;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.ArrayList;
import java.util.Objects;

/** The 1st class of functions used when the fragment is clicked. **/
public class NotesViewFragmentClick1 {
    private NotesViewFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    public NotesViewFragmentClick1(NotesViewFragment fragment) {
        this.fragment = fragment;
    }

    /** Edits the note.
     * @see NotesEditFragment **/
    public void onEditPressed() {
        if (fragment.getActivity() != null) {
            ((MainActivity) fragment.getActivity()).displayFragment(NotesEditFragment
                    .newInstance(fragment.notesSubject, fragment.notesOrder));
        }
    }

    /** Exports the note to a txt file. **/
    public void onExportPressed() {
        if (fragment.getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(fragment.getContext(), Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(fragment.getContext(), R.string
                    .error_write_permission_denied, Toast.LENGTH_SHORT).show();
        } else {
            new AutoDismissDialog(fragment.getString(R.string.data_export),
                    fragment.getString(R.string.n3_confirm_export_note),
                    new DialogInterface.OnClickListener[]{(dialogInterface, i) -> {
                dialogInterface.dismiss();
                String outputText = FileFunctions.generateValidFile("/storage/emulated/0/Download/"
                        + fragment.notesInfo.get(0), ".txt");
                FileFunctions.exportTxt(outputText, fragment.notesInfo.get(2));
                Toast.makeText(fragment.getContext(), fragment.getString(R.string.n3_note_exported) + outputText,
                        Toast.LENGTH_SHORT).show();
            }, (dialogInterface, i) -> dialogInterface.dismiss(), null})
                    .show(fragment.getParentFragmentManager(), "NotesViewFragment.1");
        }
    }

    /** Prevents the note from being able to be edited. **/
    public void onLockPressed() {
        if (fragment.getContext() != null) {
            @SuppressLint("InflateParams") TextInputLayout inputLayout =
                    (TextInputLayout) fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
            if (inputLayout.getEditText() != null) {
                inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            inputLayout.setHint(fragment.getString(R.string.set_blank_password));
            new AutoDismissDialog(fragment.getString(R.string.n3_lock_password), inputLayout,
                    new DialogInterface.OnClickListener[]{(dialogInterface, i) ->
                            getLockedNoteValue(inputLayout), (dialogInterface, i) -> dialogInterface.dismiss(), null})
                    .show(fragment.getParentFragmentManager(), "NotesViewFragment.2");
        }
    }

    /** Gets the value of the note and update it to be locked. **/
    private void getLockedNoteValue(@NonNull TextInputLayout inputLayout) {
        // Get values from notes
        String inputText = "";
        if (inputLayout.getEditText() != null) {
            inputText = inputLayout.getEditText().getText().toString();
        }
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(fragment.getActivity());
        NotesSubject subject = database.SubjectDao().search(fragment.notesSubject);
        ArrayList<ArrayList<String>> contents = subject.contents;
        writeNoteLock(database, subject, contents, inputText);
        database.close();
        fragment.isLocked = true;
        if (fragment.getActivity() != null) {
            fragment.getActivity().invalidateOptionsMenu();
        }
    }

    /** Updates the new values of the locked note to the database. **/
    private void writeNoteLock(SubjectDatabase database, NotesSubject subject,
                               ArrayList<ArrayList<String>> contents, String inputText) {
        if (contents != null && contents.size() > fragment.notesOrder) {
            FileFunctions.checkNoteIntegrity(contents.get(fragment.notesOrder));
            if (inputText.length() == 0) {
                contents.get(fragment.notesOrder).set(3, "");
            } else {
                contents.get(fragment.notesOrder).set(3, SecurityFunctions.notesHash(inputText));
            }
            subject.contents = contents;
            database.SubjectDao().update(subject);
            Toast.makeText(fragment.getContext(), R.string.n3_note_locked, Toast.LENGTH_SHORT).show();
        }
    }

    /** Unlocks the note. If there is no password, the note will be unlocked immediately.
     * Or else, a popup will display asking the user to enter the password. **/
    public void onUnlockPressed() {
        if (fragment.getContext() != null) {
            SubjectDatabase database = GeneralFunctions.getSubjectDatabase(fragment.getActivity());
            NotesSubject subject = database.SubjectDao().search(fragment.notesSubject);
            ArrayList<ArrayList<String>> contents = subject.contents;

            if (contents != null && contents.size() > fragment.notesOrder) {
                FileFunctions.checkNoteIntegrity(contents.get(fragment.notesOrder));
                // Unlocks immediately if no password
                if (contents.get(fragment.notesOrder).get(3) != null &&
                        contents.get(fragment.notesOrder).get(3).length() > 0)
                    setUnlockDialogLayout(database, subject, contents);
                else removeLock(contents, database, subject);
            }
        }
    }

    /** Sets the layout for the unlock dialog. **/
    private void setUnlockDialogLayout(SubjectDatabase database, NotesSubject subject,
                                       ArrayList<ArrayList<String>> contents) {
        @SuppressLint("InflateParams") TextInputLayout inputLayout =
                (TextInputLayout) fragment.getLayoutInflater()
                        .inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        DialogInterface.OnShowListener passwordListener = dialogInterface ->
                setUnlockDialogButtons((AlertDialog) dialogInterface, database,
                        subject, contents, inputLayout);
        // Asks user for password
        new AutoDismissDialog(fragment.getString(R.string.n3_unlock_password), inputLayout,
                passwordListener).show(fragment.getParentFragmentManager(), "NotesViewFragment.3");
    }

    /** Set the onClickListeners for the buttons on the unlock dialog. **/
    private void setUnlockDialogButtons(@NonNull AlertDialog dialogInterface, SubjectDatabase database,
                                        NotesSubject subject, ArrayList<ArrayList<String>> contents,
                                        TextInputLayout inputLayout) {
        dialogInterface.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String inputText = "";
            if (inputLayout.getEditText() != null) {
                inputText = inputLayout.getEditText().getText().toString();
            }
            if (Objects.equals(SecurityFunctions.notesHash(inputText),
                    contents.get(fragment.notesOrder).get(3))) {
                // Removes password
                dialogInterface.dismiss();
                removeLock(contents, database, subject);
            } else {
                // Show error dialog
                inputLayout.setErrorEnabled(true);
                inputLayout.setError(fragment.getString(R.string.error_password_incorrect));
            }
        });
        dialogInterface.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                view -> dialogInterface.dismiss());
    }

    /** Removes the lock for the note and refreshes the menu.  **/
    private void removeLock(@NonNull ArrayList<ArrayList<String>> contents,
                            @NonNull SubjectDatabase database,
                            @NonNull NotesSubject subject) {
        if (fragment.getActivity() != null) {
            contents.get(fragment.notesOrder).set(3, null);
            subject.contents = contents;
            database.SubjectDao().update(subject);
            database.close();
            Toast.makeText(fragment.getContext(), R.string.n3_note_unlocked, Toast.LENGTH_SHORT).show();
            fragment.isLocked = false;
            fragment.getActivity().invalidateOptionsMenu();
        }
    }
}
