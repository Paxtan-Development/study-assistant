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
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/** Functions that export the subject as a ZIP file. **/
public class ExportSubjectZip {
    private Fragment fragment;
    private SubjectDatabase subjectDatabase;
    private ArrayList<ArrayList<String>> notesArray;
    private String notesSubject;

    /** The constructor for the functions. **/
    public ExportSubjectZip(Fragment fragment, SubjectDatabase subjectDatabase,
                            ArrayList<ArrayList<String>> notesArray, String notesSubject) {
        this.fragment = fragment;
        this.subjectDatabase = subjectDatabase;
        this.notesArray = notesArray;
        this.notesSubject = notesSubject;
    }

    /** Asks users whether to export the ZIP file with a password.
     * Separated from onExportPressed() for clarity,
     * exportZip() separated for clarity. **/
    public void askZipPassword() {
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout)
                fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        inputLayout.setHint(fragment.getString(R.string.set_blank_password));

        DialogInterface.OnShowListener passwordListener =
                dialogInterface -> setPositiveBtn(dialogInterface, inputLayout);
        new AutoDismissDialog(fragment.getString(R.string.enter_password), inputLayout, passwordListener)
                .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.5");
    }

    /** Sets the positive button on the zip password dialog. **/
    private void setPositiveBtn(DialogInterface dialogInterface, TextInputLayout inputLayout) {
        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener(view -> {
                    String inputText = "";
                    if (inputLayout.getEditText() != null) {
                        inputText = inputLayout.getEditText().getText().toString();
                    }
                    if (inputText.length() == 0 || inputText.length() >= 8) {
                        exportSubjectZip(inputText);
                    } else {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError(fragment.getString(R.string.error_password_short));
                    }
                    dialogInterface.dismiss();
                });
        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                .setOnClickListener(view -> dialogInterface.dismiss());
    }

    /** Export the subject to a ZIP file.
     * Separated from onExportPressed() for clarity. **/
    private void exportSubjectZip(String password) {
        if (fragment != null && fragment.getContext() != null) {
            // Generate valid paths for temp storage folder
            String tempExportFolder = FileFunctions.generateValidFile(fragment.getContext()
                    .getFilesDir().getAbsolutePath() + "/tempZip", "");
            try {
                createZipFile(tempExportFolder, password);
            } catch (ZipException e) {
                Log.e(MainActivity.LOG_APP_NAME, "File error: ZIP processing error occurred while " +
                        "exporting a subject. Stack trace is ");
                e.printStackTrace();
                Toast.makeText(fragment.getContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Creates the zip file that is exported. **/
    private void createZipFile(String tempExportFolder, @NonNull String password) throws ZipException {
        // Creates ZIP file
        String exportFilePath = FileFunctions.generateValidFile(
                NotesSubjectFragment.DOWNLOAD_FOLDER + notesSubject, ".zip");
        ZipFile exportFile;
        if (password.length() >= 8) {
            exportFile = new ZipFile(exportFilePath, password.toCharArray());
        } else {
            exportFile = new ZipFile(exportFilePath);
        }

        if (new File(tempExportFolder).mkdir()) {
            populateZipFile(tempExportFolder, password, exportFile, exportFilePath);
        } else {
            Log.e(MainActivity.LOG_APP_NAME, "File Error: Folder " + tempExportFolder
                    + " cannot be created.");
            Toast.makeText(fragment.getContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Populates the zip file that is exported with the notes. **/
    private void populateZipFile(String tempExportFolder, @NonNull String password,
                                 ZipFile exportFile, String exportFilePath) throws ZipException {
        ArrayList<File> exportFilesList = new ArrayList<>();
        exportAllNotes(tempExportFolder, exportFilesList);
        if (password.length() >= 8) {
            // Encrypts ZIP file with password
            ZipParameters passwordParams = new ZipParameters();
            passwordParams.setEncryptFiles(true);
            passwordParams.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            exportFile.addFiles(exportFilesList, passwordParams);
        } else if (exportFilesList.size() > 0) {
            // Adds files to ZIP file
            exportFile.addFiles(exportFilesList);
        }

        // Delete temp folder
        if (!FileFunctions.deleteDir(new File(tempExportFolder))) {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Temporary folder "
                    + tempExportFolder + " could not be deleted.");
        }
        Toast.makeText(fragment.getContext(), fragment.getString(R.string.subject_exported)
                + exportFilePath, Toast.LENGTH_SHORT).show();
    }

    /** Export all notes in the subject to their individual files. **/
    private void exportAllNotes(String tempExportFolder, ArrayList<File> exportFilesList) {
        // Export all the note's data to a text .subj file
        String infoTempOutputPath = FileFunctions.generateValidFile(
                tempExportFolder + notesSubject, ".subj");
        try (FileWriter infoTempOutput = new FileWriter(infoTempOutputPath)) {
            NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
            infoTempOutput.write(notesSubject + "\n" + subject.sortOrder + "\n");
            exportNote(tempExportFolder, exportFilesList, infoTempOutput);
            infoTempOutput.flush();
            infoTempOutput.close();

            // Rename temp info output path to .subj file
            exportFilesList.add(new File(infoTempOutputPath));
        } catch (IOException e) {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Writing subject " + notesSubject
                    + " failed. Stack trace is");
            e.printStackTrace();
        }
    }

    /** Export a note to a txt file and add its info to the .subj file. **/
    private void exportNote(String tempExportFolder,
                            ArrayList<File> exportFilesList,
                            FileWriter infoTempOutput) throws IOException {
        for (int i = 0; i < notesArray.size(); i++) {
            // Export the note to the output folder
            String currentPath = FileFunctions.generateValidFile(
                    tempExportFolder + "/" + notesArray.get(i).get(0),
                    ".txt");
            FileFunctions.exportTxt(currentPath, notesArray.get(i).get(2));
            exportFilesList.add(new File(currentPath));

            // Record info about the note to the .subj file
            FileFunctions.checkNoteIntegrity(notesArray.get(i));
            for (int j = 0; j < notesArray.get(i).size(); j++) {
                // Convert null to "NULL" string for them to be understood when read
                if (notesArray.get(i).get(j) == null) {
                    notesArray.get(i).set(j, "NULL");
                }
            }
            infoTempOutput.write(new File(currentPath).getName()
                    + "\n" + notesArray.get(i).get(0) + "\n"
                    + notesArray.get(i).get(3) + "\n" + notesArray.get(i).get(4) + "\n"
                    + notesArray.get(i).get(5) + "\n");
        }
    }
}
