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

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

/** Functions that export the subject as a ZIP file. **/
public class ExportSubjectZip {
    private final Fragment fragment;
    private final List<NotesContent> notesList;
    private final NotesSubject notesSubject;

    /** The constructor for the functions. **/
    public ExportSubjectZip(Fragment fragment, List<NotesContent> notesList, NotesSubject notesSubject) {
        this.fragment = fragment;
        this.notesList = notesList;
        this.notesSubject = notesSubject;
    }

    /** Asks users whether to export the ZIP file with a password.
     * Separated from onExportPressed() for clarity,
     * exportZip() separated for clarity. **/
    public void askZipPassword() {
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout)
                fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        inputLayout.setHint(fragment.getString(R.string.set_blank_password));
        // Set up the dismissible dialog
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(
                new AlertDialog.Builder(fragment.requireContext()).setTitle(R.string.enter_password)
                        .setView(inputLayout).create());
        dismissibleFragment.setPositiveButton(fragment.getString(android.R.string.ok), view -> {
            String inputText = "";
            if (inputLayout.getEditText() != null) inputText = inputLayout.getEditText().getText().toString();
            if (inputText.length() == 0 || inputText.length() >= 8) {
                exportSubjectZip(inputText);
            } else {
                inputLayout.setErrorEnabled(true);
                inputLayout.setError(fragment.getString(R.string.error_password_short));
            }
            dismissibleFragment.dismiss();
        });
        dismissibleFragment.setNegativeButton(fragment.getString(android.R.string.cancel), view -> dismissibleFragment.dismiss());
        dismissibleFragment.show(fragment.getParentFragmentManager(), "NotesSubjectFragment.5");
    }

    /** Export the subject to a ZIP file.
     * Separated from onExportPressed() for clarity. **/
    private void exportSubjectZip(String password) {
        if (fragment != null) {
            // Generate valid paths for temp storage folder
            String tempExportFolder = FileFunctions.generateValidFile(fragment.requireContext()
                    .getFilesDir().getAbsolutePath() + "/temp/tempZip", "");
            try {
                createZipFile(tempExportFolder, password);
            } catch (ZipException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, "File error: ZIP processing error occurred while " +
                        "exporting a subject. Stack trace is ");
                e.printStackTrace();
                Toast.makeText(fragment.requireContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Creates the zip file that is exported. **/
    private void createZipFile(String tempExportFolder, @NonNull String password) throws ZipException {
        // Creates ZIP file
        String exportFilePath = FileFunctions.generateValidFile(
                FileFunctions.getDownloadDir(fragment.requireContext()) + notesSubject, ".zip");
        ZipFile exportFile;
        if (password.length() >= 8) {
            exportFile = new ZipFile(exportFilePath, password.toCharArray());
        } else {
            exportFile = new ZipFile(exportFilePath);
        }

        if (new File(tempExportFolder).mkdir()) {
            populateZipFile(tempExportFolder, password, exportFile, exportFilePath);
        } else {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: Folder " + tempExportFolder
                    + " cannot be created.");
            Toast.makeText(fragment.requireContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
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
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Temporary folder "
                    + tempExportFolder + " could not be deleted.");
        }
        Toast.makeText(fragment.requireContext(), fragment.getString(R.string.subject_exported)
                + exportFilePath, Toast.LENGTH_SHORT).show();
    }

    /** Export all notes in the subject to their individual files. **/
    private void exportAllNotes(String tempExportFolder, ArrayList<File> exportFilesList) {
        // Export all the note's data to a text .subj file
        String infoTempOutputPath = FileFunctions.generateValidFile(
                tempExportFolder + notesSubject.title, ".subj");
        try (FileOutputStream infoTempStream = new FileOutputStream(infoTempOutputPath);
             DeflaterOutputStream deflatedInfoTemp = new DeflaterOutputStream(infoTempStream)) {
            StringBuilder infoStringBuilder = new StringBuilder(notesSubject.title + "\n" + notesSubject.sortOrder + "\n");
            exportNote(infoStringBuilder, tempExportFolder, exportFilesList);
            deflatedInfoTemp.write(infoStringBuilder.toString().getBytes());
            deflatedInfoTemp.flush();
            deflatedInfoTemp.close();

            // Rename temp info output path to .subj file
            exportFilesList.add(new File(infoTempOutputPath));
        } catch (IOException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Writing subject " + notesSubject.title
                    + " failed. Stack trace is");
            e.printStackTrace();
        }
    }

    /** Export a note to a txt file and add its info to the .subj file. **/
    private void exportNote(StringBuilder infoStringBuilder, String tempExportFolder,
                            ArrayList<File> exportFilesList) {
        for (int i = 0; i < notesList.size(); i++) {
            NotesContent currentNote = notesList.get(i);
            // Export the note to the output folder
            String currentPath = FileFunctions.generateValidFile(
                    tempExportFolder + "/" + currentNote.noteTitle,
                    ".txt");
            FileFunctions.exportTxt(currentPath, currentNote.noteContent);
            exportFilesList.add(new File(currentPath));

            // Record info about the note to the .subj file
            // The format is in:
            // title, lastEdited, salt, lockedPass, alertDate, alertCode
            // The content is already in the txt files
            infoStringBuilder.append(new File(currentPath).getName()).append("\n")
                    .append(currentNote.noteTitle).append("\n")
                    .append(ConverterFunctions.isoDateTimeFormat.format(currentNote.lastEdited))
                    .append("\n").append(currentNote.lockedSalt).append("\n");
            appendNullableFields(infoStringBuilder, currentNote);
        }
    }

    /** Converts null to NULL for fields that may be null. **/
    private void appendNullableFields(StringBuilder infoStringBuilder, @NonNull NotesContent currentNote) {
        if (currentNote.alertDate == null) {
            infoStringBuilder.append("NULL\n");
        } else {
            infoStringBuilder.append(ConverterFunctions.isoDateTimeFormat.format(currentNote.alertDate));
        }
        if (currentNote.alertCode == null) {
            infoStringBuilder.append("NULL\n");
        } else {
            infoStringBuilder.append("\n").append(currentNote.alertCode).append("\n");
        }
    }
}
