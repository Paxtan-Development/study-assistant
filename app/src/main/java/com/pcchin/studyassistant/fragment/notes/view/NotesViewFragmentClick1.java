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
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.InputType;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import java.util.Objects;

/** The 1st class of functions used when the fragment is clicked. **/
public class NotesViewFragmentClick1 {
    private final NotesViewFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    public NotesViewFragmentClick1(NotesViewFragment fragment) {
        this.fragment = fragment;
    }

    /** Edits the note.
     * @see NotesEditFragment **/
    public void onEditPressed() {
        ((MainActivity) fragment.requireActivity()).displayFragment(NotesEditFragment
                .newInstance(fragment.note.subjectId, fragment.note.noteId));
    }

    /** Exports the note to a txt file. **/
    public void onExportPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(fragment.requireContext(), Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(fragment.requireContext(), R.string
                    .error_write_permission_denied, Toast.LENGTH_SHORT).show();
        } else {
            new DefaultDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                    .setTitle(R.string.data_export)
                    .setMessage(R.string.n3_confirm_export_note)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        String outputFilePath = FileFunctions.generateValidFile(
                                FileFunctions.getExternalDownloadDir(fragment.requireContext())
                                + fragment.note.noteTitle, ".txt");
                        FileFunctions.exportTxt(outputFilePath, fragment.note.noteContent);
                        Toast.makeText(fragment.requireContext(), fragment.getString(R.string.n3_note_exported)
                                        + outputFilePath, Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create())
                    .show(fragment.getParentFragmentManager(), "NotesViewFragment.1");
        }
    }

    /** Prevents the note from being able to be edited. **/
    public void onLockPressed() {
        @SuppressLint("InflateParams") TextInputLayout inputLayout =
                (TextInputLayout) fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        inputLayout.setHint(fragment.getString(R.string.set_blank_password));
        new DefaultDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.n3_lock_password)
                .setView(inputLayout)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> setNotePassword(inputLayout))
                .setNegativeButton(android.R.string.cancel, null)
                .create())
                .show(fragment.getParentFragmentManager(), "NotesViewFragment.2");
    }

    /** Gets the value of the note and update it to be locked. **/
    private void setNotePassword(@NonNull TextInputLayout inputLayout) {
        // Get values from notes
        String inputText = "";
        if (inputLayout.getEditText() != null) {
            inputText = inputLayout.getEditText().getText().toString();
        }
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(fragment.requireActivity());
        if (inputText.length() == 0) {
            // As lockedPass cannot be null and it would always be in Base 64 (With a = at the end)
            // NONE would not clash with any possible base64 passwords
            fragment.note.lockedPass = "NONE";
        } else {
            fragment.note.lockedPass = SecurityFunctions.passwordHash(fragment.note.lockedSalt, inputText);
        }
        database.ContentDao().update(fragment.note);
        Toast.makeText(fragment.requireContext(), R.string.n3_note_locked, Toast.LENGTH_SHORT).show();
        database.close();
        fragment.requireActivity().invalidateOptionsMenu();
    }

    /** Unlocks the note. If there is no password, the note will be unlocked immediately.
     * Or else, a popup will display asking the user to enter the password. **/
    public void onUnlockPressed() {
        if (!fragment.note.lockedPass.equals("NONE")) {
            setUnlockDialogLayout();
        } else {
            removeLock();
        }
    }

    /** Sets the layout for the unlock dialog. **/
    private void setUnlockDialogLayout() {
        @SuppressLint("InflateParams") TextInputLayout inputLayout =
                (TextInputLayout) fragment.getLayoutInflater()
                        .inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.n3_unlock_password)
                .setView(inputLayout)
                .create());
        dismissibleFragment.setPositiveButton(fragment.getString(android.R.string.ok),
                view -> setUnlockPositiveButton(dismissibleFragment, inputLayout));
        dismissibleFragment.setNegativeButton(fragment.getString(android.R.string.cancel),
                view -> dismissibleFragment.dismiss());
        dismissibleFragment.show(fragment.getParentFragmentManager(), "NotesViewFragment.3");
    }

    /** Sets the positive button for the unlock dialog. **/
    private void setUnlockPositiveButton(DismissibleDialogFragment dismissibleFragment,
                                         @NonNull TextInputLayout inputLayout) {
        String inputText = "";
        if (inputLayout.getEditText() != null) {
            inputText = inputLayout.getEditText().getText().toString();
        }
        if (Objects.equals(SecurityFunctions.passwordHash(inputText, fragment.note.lockedSalt),
                fragment.note.lockedPass)) {
            // Removes password
            dismissibleFragment.dismiss();
            removeLock();
        } else {
            // Show error dialog
            inputLayout.setErrorEnabled(true);
            inputLayout.setError(fragment.getString(R.string.error_password_incorrect));
        }
    }

    /** Removes the lock for the note and refreshes the menu.  **/
    private void removeLock() {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(fragment.getContext());
        fragment.note.lockedPass = "";
        database.ContentDao().update(fragment.note);
        database.close();
        Toast.makeText(fragment.requireActivity(), R.string.n3_note_unlocked, Toast.LENGTH_SHORT).show();
        fragment.requireActivity().invalidateOptionsMenu();
    }
}
