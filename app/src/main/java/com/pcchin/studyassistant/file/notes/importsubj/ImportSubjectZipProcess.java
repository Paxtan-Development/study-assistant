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
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.utils.misc.RandomString;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.InflaterInputStream;

/** Continuation of ImportSubjectZip, functions which actually import and process the ZIP file,
 * separated for clarity. The ZIP file provided in this class either has a given password
 * or does not have a password. **/
class ImportSubjectZipProcess {
    private final MainActivity activity;
    private ArrayList<File> txtFileList;

    // HashMaps for processing .subj files
    // The key is the file name of the corresponding txt file
    private final HashMap<String, String> titleHashMap = new HashMap<>();
    private final HashMap<String, Date> lastEditedHashMap = new HashMap<>();
    private final HashMap<String, String> saltHashMap = new HashMap<>();
    private final HashMap<String, String> lockedPassHashMap = new HashMap<>();
    private final HashMap<String, Date> alertDateHashMap = new HashMap<>();
    private final HashMap<String, Integer> alertCodeHashMap = new HashMap<>();

    /** The constructor for the class as activity needs to be passed on. **/
    ImportSubjectZipProcess(MainActivity activity) {
        this.activity = activity;
    }

    /** Function used to import/convert a ZIP file,
     * separated from importZipConfirm(String path) for clarity.  **/
    void importZipFile(@NonNull ZipFile inputFile) throws ZipException {
        String tempInputDirPath = FileFunctions.generateValidFile(
                FileFunctions.getDownloadDir(activity) + ".tempZip", "");
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
            getZipFiles(FileFunctions.getFileName(inputFile.getFile().getName()), tempInputDir);
        } else {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Folder "+ tempInputDirPath
                    + " is not found");
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Gets the .subj and .txt files stored in the ZIP file and process them. **/
    private void getZipFiles(String fileName, @NonNull File tempInputDir) {
        File[] fileList = tempInputDir.listFiles();
        if (fileList != null) {
            txtFileList = new ArrayList<>();
            File subjFile = null;
            SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
            int subjectId = DatabaseFunctions.generateValidId(database, DatabaseFunctions.ID_TYPE.SUBJECT);
            database.close();
            // Populate subjFile and txtFileList
            for (File file : fileList) {
                if (file.getName().endsWith(".subj")) {
                    subjFile = file;
                } else if (file.getName().endsWith(".txt")) {
                    txtFileList.add(file);
                }
            }
            // Pass on to respective processing functions
            if (subjFile == null) {
                importZipWithoutSubj(subjectId, fileName);
            } else {
                importZipWithSubj(subjectId, fileName, subjFile);
            }
        }
    }

    /** Imports from a ZIP without a subj file.
     * The names of each note is assumed to be the title of the note,
     * and the title of the subject is assumed to be the file name of the subject. **/
    private void importZipWithoutSubj(int subjectId, String subjectTitle) {
        Random rand = new Random();
        RandomString randString = new RandomString(40);
        NotesSubject subject = new NotesSubject(subjectId, subjectTitle, NotesSubject.SORT_ALPHABETICAL_ASC);
        ArrayList<NotesContent> notesList = new ArrayList<>();
        // Get notesIdList from database
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        List<Integer> notesIdList = database.ContentDao().getAllNoteId();
        database.close();
        for (File txtFile: txtFileList) {
            NotesContent currentNote = generateNoteWithoutSubj(rand, randString, subjectId, notesIdList, txtFile);
            notesList.add(currentNote);
        }
        new ImportSubject(activity).importSubjectToDatabase(subject, notesList);
    }

    /** Generates a note if the .subj file is not present,
     * or if the note is not present within the .subj file itself.
     * The note ID that is generated will be added to noteIdList. **/
    @NonNull
    private NotesContent generateNoteWithoutSubj (@NonNull Random rand, RandomString randString,
                                                  int subjectId, @NonNull List<Integer> notesIdList,
                                                  File inputFile) {
        // Generate a valid noteId and add it to the list
        int noteId = rand.nextInt();
        while (notesIdList.contains(noteId)) noteId = rand.nextInt();
        notesIdList.add(noteId);
        // Reads the contents file and creates the file
        String fileName = FileFunctions.getFileName(inputFile.getName()),
                fileContents = getFileContents(inputFile);
        return new NotesContent(noteId, subjectId, fileName, fileContents,
                new Date(), randString.nextString());
    }

    /** Gets the contents of a text file and stores it as a String.
     * If the contents of the file is invalid, an empty String would be returned. **/
    @NonNull
    private String getFileContents(File inputFile) {
        StringBuilder fileContents = new StringBuilder();
        try (Scanner fileScanner = new Scanner(inputFile)) {
            while (fileScanner.hasNext()) {
                fileContents.append(fileScanner.next()).append("\n");
            }
            return fileContents.toString();
        } catch (IOException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: A txt file exported from " +
                    "a ZIP file could not be imported. The error is");
            e.printStackTrace();
            return "";
        }
    }

    /** Imports from a ZIP with a subj file.
     * If the format provided in the subj file is incorrect, it will fallback to
     * importZipWithoutSubj with fileName as the subject title. **/
    private void importZipWithSubj(int subjectId, String fileName, File subjFile) {
        // Reads the subj file and maps it to a couple of HashMaps
        try (FileInputStream infoStream = new FileInputStream(subjFile);
             InflaterInputStream inflatedInfoStream = new InflaterInputStream(infoStream);
             Scanner infoFileScanner = new Scanner(inflatedInfoStream)) {
            // First line is title while second line is sort order
            NotesSubject currentSubject = new NotesSubject(subjectId, infoFileScanner.nextLine(),
                    Integer.parseInt(infoFileScanner.nextLine()));
            parseSubjFile(infoFileScanner, currentSubject);
        } catch (NullPointerException | IOException | ParseException | NumberFormatException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: An error occurred while " +
                    "attempting to parse a .subj file, falling back to importZipWithoutSubj, error is");
            e.printStackTrace();
            importZipWithoutSubj(subjectId, fileName);
        }
    }

    /** Parses the .subj file in a ZIP file and stores the values to the database.
     * The line order in the subj file should be as follows:
     * 1. path of the txt file
     * 2. title of note
     * 3. The last edited date of the note, in ISO format
     * 4. The salt of the note
     * 5. The locked pass of the note
     * 6. The alert date of the note, in ISO format, can be null
     * 7. The alert code of the note, can be null **/
    private void parseSubjFile(@NonNull Scanner infoFileScanner, NotesSubject currentSubject)
            throws ParseException, NumberFormatException, NullPointerException {
        int index = 0;
        String currentLine, currentNotePath = "";
        while (infoFileScanner.hasNext()) {
            currentLine = infoFileScanner.nextLine();
            switch(index) {
                case 0:
                    currentNotePath = currentLine;
                    break;
                case 1:
                    titleHashMap.put(currentNotePath, currentLine);
                    break;
                case 2:
                    lastEditedHashMap.put(currentNotePath, ConverterFunctions.isoDateTimeFormat.parse(currentLine));
                    break;
                case 3:
                    saltHashMap.put(currentNotePath, currentLine);
                    break;
                case 4:
                    lockedPassHashMap.put(currentNotePath, currentLine);
                    break;
                case 5:
                    if (currentLine.equals("NULL")) alertDateHashMap.put(currentNotePath, null);
                    else alertDateHashMap.put(currentNotePath, ConverterFunctions.isoDateTimeFormat.parse(currentLine));
                    break;
                case 6:
                    if (currentLine.equals("NULL")) alertCodeHashMap.put(currentLine, null);
                    else alertCodeHashMap.put(currentNotePath, Integer.parseInt(currentLine));
                    break;
            }

            index++;
            if (index > 6) index = 0;
        }
        processTxtFilesWithSubj(currentSubject);
    }

    /** Process all txt files after tbe data in the .subj file is processed. **/
    private void processTxtFilesWithSubj(NotesSubject currentSubject) throws NullPointerException {
        Random rand = new Random();
        RandomString randString = new RandomString(40);
        ArrayList<NotesContent> notesList = new ArrayList<>();
        NotesContent currentNote;
        // Get all the existing note IDs
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        List<Integer> notesIdList = database.ContentDao().getAllNoteId();
        database.close();
        for (File txtFile: txtFileList) {
            // Check if a note title with the corresponding file exists
            // If not, fall back to generateNoteWihoutSubj
            String title = titleHashMap.get(txtFile.getName());
            if (title == null) {
                currentNote = generateNoteWithoutSubj(rand, randString, currentSubject.subjectId, notesIdList, txtFile);
            } else {
                currentNote = generateNoteWithSubj(rand, currentSubject.subjectId, notesIdList, txtFile);
            }
            notesList.add(currentNote);
        }
        new ImportSubject(activity).importSubjectToDatabase(currentSubject, notesList);
    }

    /** Generates a note whose subject is present. **/
    @NonNull
    private NotesContent generateNoteWithSubj(@NonNull Random rand, int subjectId,
                                              @NonNull List<Integer> notesIdList, @NonNull File txtFile)
            throws NullPointerException {
        // Generate the noteId
        String fileName = txtFile.getName();
        int noteId = rand.nextInt();
        while (notesIdList.contains(noteId)) noteId = rand.nextInt();
        return new NotesContent(noteId, subjectId, Objects.requireNonNull(titleHashMap.get(fileName)), getFileContents(txtFile),
                Objects.requireNonNull(lastEditedHashMap.get(fileName)), Objects.requireNonNull(saltHashMap.get(fileName)),
                Objects.requireNonNull(lockedPassHashMap.get(fileName)), alertDateHashMap.get(fileName),
                alertCodeHashMap.get(fileName));
    }
}
