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

package com.pcchin.studyassistant.file.notes.exportsubj;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.zip.DeflaterOutputStream;

/** Functions that export the subject as a .subject file. **/
public class ExportSubjectSubject {
    private final Fragment fragment;
    private final NotesSubject notesSubject;
    private final List<NotesContent> notesList;

    /** The constructor for the functions. **/
    public ExportSubjectSubject(Fragment fragment, NotesSubject notesSubject,
                                List<NotesContent> notesList) {
        this.fragment = fragment;
        this.notesSubject = notesSubject;
        this.notesList = notesList;
    }

    /** Export the subject as a password-protected byte[] as a .subject file,
     * separated from onExportSubject() for clarity. **/
    public void exportSubject() {
        @SuppressLint("InflateParams") TextInputLayout inputText = (TextInputLayout) fragment.getLayoutInflater()
                .inflate(R.layout.popup_edittext, null);
        if (inputText.getEditText() != null) inputText.getEditText().setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputText.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        inputText.setHint(fragment.getString(R.string.set_blank_password));

        // Display the dialog
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(
                new AlertDialog.Builder(fragment.requireContext())
                        .setTitle(R.string.n2_password_export)
                        .setView(inputText)
                        .create());
        dismissibleFragment.setPositiveButton(fragment.getString(android.R.string.ok), view -> {
            // Check if password is too short, must be 8 characters in length
            String responseText = "";
            if (inputText.getEditText() != null) responseText = inputText.getEditText().getText().toString();
            checkPasswordRequirement(dismissibleFragment, responseText, inputText);
        });
        dismissibleFragment.setNegativeButton(fragment.getString(android.R.string.cancel), view ->
                dismissibleFragment.dismiss());
        dismissibleFragment.show(fragment.getParentFragmentManager(), "NotesSubjectFragment.6");
    }

    /** Check if the password set by the user fits the requirement. **/
    private void checkPasswordRequirement(DismissibleDialogFragment dialog, @NonNull String responseText,
                                          TextInputLayout inputText) {
        if (responseText.length() == 0 || responseText.length() >= 8) {
            // Set output file name
            String outputFileName = FileFunctions.getDownloadDir(fragment.requireContext()) + notesSubject.title
                    + ".subject";
            int count = 0;
            while (new File(outputFileName).exists()) {
                count++;
                outputFileName = FileFunctions.getDownloadDir(fragment.requireContext()) + notesSubject.title
                        + "(" + count + ").subject";
            }

            // Check if the file can be created
            String finalOutputFileName = outputFileName;
            dialog.dismiss();
            new Handler().post(() -> handleExportSubjectError(dialog, finalOutputFileName,
                            responseText, responseText));
        } else {
            inputText.setErrorEnabled(true);
            inputText.setError(fragment.getString(R.string.error_password_short));
        }
    }

    /** Handles any errors that occur while exporting the .subject file. **/
    private void handleExportSubjectError(DismissibleDialogFragment dialog,
                                          String finalOutputFileName,
                                          String finalResponseText,
                                          String finalResponseText1) {
        try {
            // Get permission to read and write files
            File outputFile = new File(finalOutputFileName);
            if (outputFile.createNewFile()) {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile);
                     DeflaterOutputStream deflatedStream = new DeflaterOutputStream(outputStream)) {
                    exportSubjectFile(finalOutputFileName, finalResponseText, finalResponseText1, deflatedStream);
                }
            } else {
                Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File " + finalOutputFileName + " cannot be created.");
                Toast.makeText(fragment.requireContext(), R.string.n2_error_file_not_created,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File " + finalOutputFileName + " not found, stack trace is");
            e.printStackTrace();
            dialog.dismiss();
        } catch (IOException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: An IO Exception" + " occurred on file " + finalOutputFileName + ", stack trace is");
            e.printStackTrace();
            dialog.dismiss();
        }
    }

    /** Creates and export the .subject file. **/
    private void exportSubjectFile(String finalOutputFileName, String finalResponseText,
                                   @NonNull String finalResponseText1,
                                   @NonNull DeflaterOutputStream deflatedOutput)
            throws IOException {
        Toast.makeText(fragment.requireContext(), R.string.n2_exporting_subject, Toast.LENGTH_SHORT).show();
        // Export the file
        // The length of the title is exported first, followed by the title.
        // Then, the subject's sort order is listed and the encrypted contents are stored.
        // All the contents in the file are then compressed before exported
        deflatedOutput.write(ConverterFunctions.intToBytes(notesSubject.title.getBytes().length));
        deflatedOutput.write(notesSubject.title.getBytes());
        deflatedOutput.write(ConverterFunctions.intToBytes(notesSubject.sortOrder));
        if (finalResponseText1.length() >= 8) {
            deflatedOutput.write(1);
            // Create 32 bytes of salt
            byte[] salt = new byte[32];
            new Random().nextBytes(salt);
            deflatedOutput.write(SecurityFunctions.subjectEncrypt(finalResponseText, salt, notesList));
        } else {
            deflatedOutput.write(0);
            deflatedOutput.write(ConverterFunctions.notesListToString(notesList).getBytes());
        }
        deflatedOutput.flush();
        deflatedOutput.close();
        Toast.makeText(fragment.requireContext(), fragment.getString(R.string.subject_exported) + finalOutputFileName, Toast.LENGTH_SHORT).show();
    }
}
