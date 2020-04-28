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
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/** Functions that export the subject as a .subject file. **/
public class ExportSubjectSubject {
    private Fragment fragment;
    private String notesSubject;
    private ArrayList<ArrayList<String>> notesArray;
    private int sortOrder;

    /** The constructor for the functions. **/
    public ExportSubjectSubject(Fragment fragment, String notesSubject,
                                ArrayList<ArrayList<String>> notesArray, int sortOrder) {
        this.fragment = fragment;
        this.notesSubject = notesSubject;
        this.notesArray = notesArray;
        this.sortOrder = sortOrder;
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
        DialogInterface.OnShowListener exportListener = dialogInterface -> {
            ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // Check if password is too short, must be 8 characters in length
                String responseText = "";
                if (inputText.getEditText() != null) responseText = inputText.getEditText().getText().toString();
                checkPasswordRequirement(dialogInterface, responseText, inputText);
            });
            ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view ->
                    dialogInterface.dismiss());
        };
        new AutoDismissDialog(fragment.getString(R.string.n2_password_export), inputText, exportListener)
                .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.6");
    }

    /** Check if the password set by the user fits the requirement. **/
    private void checkPasswordRequirement(DialogInterface dialogInterface, @NonNull String responseText,
                                          TextInputLayout inputText) {
        if (responseText.length() == 0 || responseText.length() >= 8) {
            // Set output file name
            String outputFileName = NotesSubjectFragment.DOWNLOAD_FOLDER + notesSubject
                    + ".subject";
            int count = 0;
            while (new File(outputFileName).exists()) {
                outputFileName = NotesSubjectFragment.DOWNLOAD_FOLDER + notesSubject
                        + "(" + count + ").subject";
            }

            // Check if the file can be created
            String finalOutputFileName = outputFileName;
            dialogInterface.dismiss();
            new Handler().post(() -> handleExportSubjectError(dialogInterface, finalOutputFileName,
                            responseText, responseText));
        } else {
            inputText.setErrorEnabled(true);
            inputText.setError(fragment.getString(R.string.error_password_short));
        }
    }

    /** Handles any errors that occur while exporting the .subject file. **/
    private void handleExportSubjectError(DialogInterface dialogInterface,
                                          String finalOutputFileName,
                                          String finalResponseText,
                                          String finalResponseText1) {
        try {
            // Get permission to read and write files
            File outputFile = new File(finalOutputFileName);
            if (outputFile.createNewFile()) {
                try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    exportSubjectFile(finalOutputFileName, finalResponseText, finalResponseText1, outputStream);
                }
            } else {
                Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File "
                        + finalOutputFileName + " cannot be created.");
                Toast.makeText(fragment.getContext(), R.string.n2_error_file_not_created,
                        Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File "
                    + finalOutputFileName + " not found, stack trace is");
            e.printStackTrace();
            dialogInterface.dismiss();
        } catch (IOException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: An IO Exception"
                    + " occurred on file " + finalOutputFileName + ", stack trace is");
            e.printStackTrace();
            dialogInterface.dismiss();
        }
    }

    /** Creates and export the .subject file. **/
    private void exportSubjectFile(String finalOutputFileName, String finalResponseText,
                                   @NonNull String finalResponseText1,
                                   @NonNull FileOutputStream outputStream)
            throws IOException {
        Toast.makeText(fragment.getContext(), R.string.n2_exporting_subject, Toast.LENGTH_SHORT).show();
        // Export the file
        // The length of the title is exported first, followed by the title.
        // Then, the subject's sort order is listed and the encrypted contents are stored.
        outputStream.write(ConverterFunctions.intToBytes(notesSubject.getBytes().length));
        outputStream.write(notesSubject.getBytes());
        outputStream.write(ConverterFunctions.intToBytes(sortOrder));
        if (finalResponseText1.length() >= 8) {
            outputStream.write(1);
            outputStream.write(SecurityFunctions.subjectEncrypt(notesSubject, finalResponseText, notesArray));
        } else {
            outputStream.write(0);
            outputStream.write(ConverterFunctions.doubleArrayToJson(notesArray).getBytes());
        }
        outputStream.flush();
        outputStream.close();
        Toast.makeText(fragment.getContext(), fragment.getString(R.string.subject_exported)
                        + finalOutputFileName, Toast.LENGTH_SHORT).show();
    }
}
