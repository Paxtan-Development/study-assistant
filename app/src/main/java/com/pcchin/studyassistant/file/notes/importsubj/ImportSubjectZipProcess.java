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

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.ui.MainActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

/** Continuation of ImportSubjectZip, functions which actually import and process the ZIP file,
 * separated for clarity. **/
class ImportSubjectZipProcess {
    private final MainActivity activity;
    private String title;
    private int listOrder;

    /** The constructor for the class as activity needs to be passed on. **/
    ImportSubjectZipProcess(MainActivity activity) {
        this.activity = activity;
    }

    /** Function used to import/convert a ZIP file,
     * separated from importZipConfirm(String path) for clarity.  **/
    void importZipFile(@NonNull ZipFile inputFile) throws ZipException {
        String tempInputDirPath = FileFunctions.generateValidFile(
                "/storage/emulated/0/Download/.tempZip", "");
        try {
            inputFile.extractAll(tempInputDirPath);
        } catch (ZipException e) {
            // Clears download directory before rethrowing error
            // New File used to prevent tempInputDir.exists() from returning false
            FileFunctions.deleteDir(new File(tempInputDirPath));
            throw e;
        }

        Toast.makeText(activity, R.string.importing_subject, Toast.LENGTH_SHORT).show();
        File tempInputDir = new File(tempInputDirPath);
        if (tempInputDir.exists() && tempInputDir.isDirectory()) {
            getZipFiles(inputFile, tempInputDir);
        } else {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Folder "+ tempInputDirPath
                    + " is not found");
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Gets the .subj and .txt files stored in the ZIP file and process them. **/
    private void getZipFiles(ZipFile inputFile, @NonNull File tempInputDir) {
        File[] fileList = tempInputDir.listFiles();
        title = "";
        listOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
        ArrayList<ArrayList<String>> content = new ArrayList<>();
        processFileList(fileList, tempInputDir, content);

        // Delete temp directory
        FileFunctions.deleteDir(tempInputDir);
        if (title.length() > 0) {
            new ImportSubject(activity).importSubjectToDatabase(title, content, listOrder);
        } else {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Title of subject in ZIP file "
                    + inputFile.getFile().getAbsolutePath() + " invalid.");
            Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    /** Gets all the files then process them. **/
    private void processFileList(File[] fileList, File tempInputDir,
                                 ArrayList<ArrayList<String>> content) {
        // Get notes from .subj file, then from .txt files
        if (fileList != null) {
            // Get notes info from .subj file
            // First 2 strings are title and sort order.
            // Then the notes are stored in groups of 4:
            // 1) Relative path, 2) Title, 3) Notes lock, 4) Alert Date and Time, 5) Alert Code
            ArrayList<String> subjInfo = new ArrayList<>();
            ArrayList<ArrayList<String>> notesInfo = new ArrayList<>();
            populateNoteArray(fileList, subjInfo);
            initNote(fileList, subjInfo, notesInfo, content);
        } else {
            title = tempInputDir.getName();
        }
    }

    /** Initializes each note. **/
    private void initNote(File[] fileList, @NonNull ArrayList<String> subjInfo,
                          ArrayList<ArrayList<String>> notesInfo, ArrayList<ArrayList<String>> content) {
        if (subjInfo.size() >= 2) {
            title = subjInfo.get(0);
            listOrder = Integer.parseInt(subjInfo.get(1));
        }
        for (int i = 3; i < subjInfo.size(); i+=5) {
            ArrayList<String> currentNoteInfo = new ArrayList<>();
            // Add notes info to notesInfo
            for (int j = 0; j < 5; j++) {
                if (i + j < subjInfo.size()) currentNoteInfo.add(subjInfo.get(i + j));
                else currentNoteInfo.add(null);
            }
            notesInfo.add(currentNoteInfo);
        }

        // Get notes content from each note
        for (File file : fileList) {
            try {
                processFile(file, notesInfo, content);
            } catch (FileNotFoundException e) {
                Log.e(MainActivity.LOG_APP_NAME, "File Error: ");
                e.printStackTrace();
            }
        }
    }

    /** Process each file available in the ZIP archive. **/
    private void processFile(@NonNull File file, ArrayList<ArrayList<String>> notesInfo,
                             ArrayList<ArrayList<String>> content) throws FileNotFoundException {
        String fileName = file.getName();
        if (fileName.endsWith(".txt")) {
            // Get text contents from file
            StringBuilder fileContents = new StringBuilder();
            // Try-by-resources
            try (Scanner currentFileScanner = new Scanner(file)) {
                while (currentFileScanner.hasNext()) {
                    fileContents.append(currentFileScanner.next()).append("\n");
                }
            }

            // Check if any notes match the info stored in the subject
            if (!checkNoteAdded(notesInfo, fileName,fileContents, content))
                addTempArray(fileName, fileContents, content);
        }
    }

    /** Adds the content of the file to the note via a temp array. **/
    private void addTempArray(@NonNull String fileName, @NonNull StringBuilder fileContents,
                              ArrayList<ArrayList<String>> content) {
        // Note is not mentioned in the .subj file, needs to be added manually
        ArrayList<String> tempArray = new ArrayList<>();
        // Removes the .txt extension
        tempArray.add(fileName.replace(fileName.substring(fileName.length() - 4),
                ""));
        tempArray.add(ConverterFunctions.standardDateTimeFormat.format(new Date()));
        tempArray.add(fileContents.toString());

        // For lock, alert time & request code respectively
        for (int i = 0; i < 3; i++) {
            tempArray.add(null);
        }

        content.add(tempArray);
    }

    /** Check whether a note with the specified title exists, and if yes, add it to the database. **/
    private boolean checkNoteAdded(@NonNull ArrayList<ArrayList<String>> notesInfo, String fileName,
                                   StringBuilder fileContents, ArrayList<ArrayList<String>> content) {
        for (int i = 0; i < notesInfo.size(); i++) {
            if (Objects.equals(notesInfo.get(i).get(0), fileName)) {
                // Add note info to ArrayList
                ArrayList<String> currentNote = new ArrayList<>();
                currentNote.add(notesInfo.get(i).get(1));
                currentNote.add(ConverterFunctions.standardDateTimeFormat
                        .format(new Date()));
                currentNote.add(fileContents.toString());

                // Add other attributes from .subj file
                for (int att = 2; att <= 4; att++) {
                    currentNote.add(notesInfo.get(i).get(att));
                }

                content.add(currentNote);
                return true;
            }
        }
        return false;
    }

    /** Populates all the notes into the ArrayList. **/
    private void populateNoteArray(@NonNull File[] fileList, ArrayList<String> subjInfo) {
        for (File file: fileList) {
            if (file.getAbsolutePath().endsWith(".subj")) {
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNext()) subjInfo.add(scanner.next());
                } catch (FileNotFoundException e) {
                    Log.e(MainActivity.LOG_APP_NAME, "File Error: " + file.getAbsolutePath()
                            + " not found despite contained in file list of parent folder. " +
                            "Stack trace is");
                    e.printStackTrace();
                }
            }
        }
    }
}
