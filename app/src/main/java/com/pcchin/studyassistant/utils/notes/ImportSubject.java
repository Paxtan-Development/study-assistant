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

package com.pcchin.studyassistant.utils.notes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

/** Functions used to import subjects from .subject files and ZIP files.
 * Cannot be made static as MainActivity activity needs to be separated for clarity. **/
public class ImportSubject {
    private final MainActivity activity;

    /** Displays the import dialog for whether to import from a ZIP or a .subject file.
     * Separated from the constructor as this function will startActivityForResult
     * before continuing on with the rest of the functions in the class. **/
    public static void displayImportDialog(MainActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(activity, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MainActivity.EXTERNAL_STORAGE_READ_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            new AutoDismissDialog(activity.getString(R.string.import_from), R.array.n_import_subject_format,
                    (dialogInterface, i) -> {
                        try {
                            // Set up file chooser
                            Intent fileSelectIntent = new Intent(Intent.ACTION_GET_CONTENT);
                            fileSelectIntent.setType("*/*");
                            if (i == 0) {
                                String[] mimeType = {"application/zip", "application/x-compressed",
                                        "application/x-zip-compressed"};
                                fileSelectIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
                                activity.startActivityForResult(Intent.createChooser(fileSelectIntent,
                                        activity.getString(R.string.select_file)), MainActivity.SELECT_ZIP_FILE);
                            } else {
                                Toast.makeText(activity, R.string.select_subject_file, Toast.LENGTH_SHORT).show();
                                activity.startActivityForResult(fileSelectIntent, MainActivity.SELECT_SUBJECT_FILE);
                            }
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(activity, R.string.error_file_manager_not_found, Toast.LENGTH_SHORT).show();
                            Log.e(MainActivity.LOG_APP_NAME, "File Error: This device appears to "
                                    + "not have a file manager. Stack trace is");
                            e.printStackTrace();
                        }
            }, new String[]{"", "", ""}, new DialogInterface.OnClickListener[]{null, null, null})
                    .show(activity.getSupportFragmentManager(), "ImportSubject.1");
        } else {
            Toast.makeText(activity, R.string.error_read_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    /** The constructor for the class as activity needs to be passed on.
     * Originally contained displayImportDialog(MainActivity activity) but moved for better structuring of files.
     * importZipConfirm(String path) and importSubjectFile(String path) separated for clarity. **/
    public ImportSubject(MainActivity activity) {
        this.activity = activity;
    }

    /** Function used to confirm whether the ZIP file is valid and the password is provided
     * before unzipping the ZIP file. Separated from constructor for clarity. **/
    public void importZipConfirm(String path) {
        try {
            if (new ZipFile(path).isValidZipFile()) {
                if (new ZipFile(path).isEncrypted()) {
                    @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout)
                            activity.getLayoutInflater().inflate(R.layout.popup_edittext, null);
                    if (inputLayout.getEditText() != null) {
                        inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

                    // Asks for password
                    DialogInterface.OnShowListener passwordListener = dialogInterface -> {
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnClickListener(view -> {
                                    String password = "";
                                    if (inputLayout.getEditText() != null) {
                                        password = inputLayout.getEditText().getText().toString();
                                    }
                                    if (password.length() >= 8) {
                                        try {
                                            ZipFile inputFile = new ZipFile(path, password.toCharArray());
                                            importZipFile(inputFile);
                                            dialogInterface.dismiss();
                                        } catch (ZipException e) {
                                            inputLayout.setErrorEnabled(true);
                                            inputLayout.setError(activity.getString(R.string
                                                    .error_password_incorrect));
                                        }
                                    } else {
                                        inputLayout.setErrorEnabled(true);
                                        inputLayout.setError(activity.getString(R.string
                                                .error_password_short));
                                    }
                                });
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                                .setOnClickListener(view -> dialogInterface.dismiss());
                    };
                    new AutoDismissDialog(activity.getString(R.string.enter_password), inputLayout,
                            new String[]{activity.getString(android.R.string.ok),
                                    activity.getString(android.R.string.cancel), null},
                            passwordListener).show(activity.getSupportFragmentManager(),
                            "ImportSubject.2");
                } else {
                    // Error thrown in importZipFile will be handled by the external error handler
                    ZipFile inputFile = new ZipFile(path);
                    importZipFile(inputFile);
                }
            } else {
                Log.e(MainActivity.LOG_APP_NAME, "File Error: ZIP file " + path + " is invalid.");
                Toast.makeText(activity, R.string.error_zip_corrupt, Toast.LENGTH_SHORT).show();
            }
        } catch (ZipException e) {
            Log.e(MainActivity.LOG_APP_NAME, "File Error: ZIP processing error occurred while "
                    + " importing a subject, stack trace is");
            Toast.makeText(activity, R.string.error_zip_import, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /** Function used to import/convert a ZIP file,
     * separated from importZipConfirm(String path) for clarity.  **/
    private void importZipFile(ZipFile inputFile) throws ZipException {
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
            File[] fileList = tempInputDir.listFiles();
            String title = "";
            int listOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
            ArrayList<ArrayList<String>> content = new ArrayList<>();

            // Get notes from .subj file, then from .txt files
            if (fileList != null) {
                // Get notes info from .subj file
                // First 2 strings are title and sort order.
                // Then the notes are stored in groups of 4:
                // 1) Relative path, 2) Title, 3) Notes lock, 4) Alert Date and Time, 5) Alert Code
                ArrayList<String> subjInfo = new ArrayList<>();
                ArrayList<ArrayList<String>> notesInfo = new ArrayList<>();
                for (File file: fileList) {
                    if (file.getAbsolutePath().endsWith(".subj")) {
                        try (Scanner scanner = new Scanner(file)) {
                            while (scanner.hasNext()) {
                                subjInfo.add(scanner.next());
                            }
                        } catch (FileNotFoundException e) {
                            Log.e(MainActivity.LOG_APP_NAME, "File Error: " + file.getAbsolutePath()
                                + " not found despite contained in file list of parent folder. " +
                                    "Stack trace is");
                            e.printStackTrace();
                        }
                    }
                }

                if (subjInfo.size() >= 2) {
                    title = subjInfo.get(0);
                    listOrder = Integer.parseInt(subjInfo.get(1));
                }
                for (int i = 3; i < subjInfo.size(); i+=5) {
                    ArrayList<String> currentNoteInfo = new ArrayList<>();
                    // Add notes info to notesInfo
                    for (int j = 0; j < 5; j++) {
                        if (i + j < subjInfo.size()) {
                            currentNoteInfo.add(subjInfo.get(i + j));
                        } else {
                            currentNoteInfo.add(null);
                        }
                    }
                    notesInfo.add(currentNoteInfo);
                }

                // Get notes content from each note
                for (File file : fileList) {
                    try {
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
                            boolean noteAdded = false;
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
                                    noteAdded = true;
                                }
                            }

                            if (!noteAdded) {
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
                        }
                    } catch (FileNotFoundException e) {
                        Log.e(MainActivity.LOG_APP_NAME, "File Error: ");
                        e.printStackTrace();
                    }
                }
            } else {
                title = tempInputDir.getName();
            }

            // Delete temp directory
            FileFunctions.deleteDir(tempInputDir);

            if (title.length() > 0) {
                importSubjectToDatabase(title, content, listOrder);
            } else {
                Log.w(MainActivity.LOG_APP_NAME, "File Error: Title of subject in ZIP file "
                        + inputFile.getFile().getAbsolutePath() + " invalid.");
                Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Folder "+ tempInputDirPath
                    + " is not found");
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Function used to import a subject using a .subject file.
     * Separated from constructor for clarity. **/
    public void importSubjectFile(String path) {
        // Check if file exists
        File targetFile = new File(path);
        if (targetFile.exists() && targetFile.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(targetFile)) {
                int fileSize = (int) inputStream.getChannel().size();
                // Length of title is first 4 bytes, title follows next
                int titleLength = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
                String title = new String(FileFunctions.getBytesFromFile(titleLength, inputStream));
                // Then the int containing the sort order is returned
                int sortOrder = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
                // Then whether the byte containing whether the subject is encrypted is returned
                byte subjectEncrypted = FileFunctions.getBytesFromFile(1, inputStream)[0];
                // Then the content of the subject is returned
                byte[] content = FileFunctions.getBytesFromFile(fileSize - titleLength - 8 - 1, inputStream);
                inputStream.close();

                if (subjectEncrypted == 1) {
                    // Subject is encrypted
                    @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout) activity
                            .getLayoutInflater().inflate(R.layout.popup_edittext, null);
                    if (inputLayout.getEditText() != null) {
                        inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

                    DialogInterface.OnShowListener importListener = dialogInterface -> {
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                            Toast.makeText(activity, R.string.importing_subject, Toast.LENGTH_SHORT).show();
                            new Handler().post(() -> {
                                // Everything is done in Handler to prevent main thread from being overloaded
                                String password = "";
                                if (inputLayout.getEditText() != null) {
                                    password = inputLayout.getEditText().getText().toString();
                                }
                                ArrayList<ArrayList<String>> subjectContents = SecurityFunctions
                                        .subjectDecrypt(title, password, content);
                                if (subjectContents == null) {
                                    inputLayout.setErrorEnabled(true);
                                    inputLayout.setError(activity.getString(R.string.error_password_incorrect));
                                } else {
                                    importSubjectToDatabase(title, subjectContents, sortOrder);
                                    dialogInterface.dismiss();
                                }
                            });
                        });
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view ->
                                dialogInterface.dismiss());
                    };
                    new AutoDismissDialog(activity.getString(R.string.enter_password), inputLayout,
                            new String[]{activity.getString(android.R.string.ok),
                                    activity.getString(android.R.string.cancel), ""}, importListener)
                            .show(activity.getSupportFragmentManager(), "ImportSubject.3");
                } else {
                    // Subject is not encrypted
                    String contentString = new String(content);
                    if (ConverterFunctions.doubleJsonToArray(contentString) == null) {
                        Log.w(MainActivity.LOG_APP_NAME, "File Error: The .subject file "
                        + path + " could not be imported as its content is incorrect.");
                        Toast.makeText(activity, R.string.error_subject_import, Toast.LENGTH_SHORT).show();
                    } else {
                        importSubjectToDatabase(title, ConverterFunctions
                                .doubleJsonToArray(contentString), sortOrder);
                    }
                }
            } catch (IOException e) {
                Log.e(MainActivity.LOG_APP_NAME, "File Error: File " + path + " could not be read"
                        + " by FileInputStream. Stack trace is");
                Toast.makeText(activity, R.string.error_subject_import, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.e(MainActivity.LOG_APP_NAME, "File Error: File " + path + " is not found.");
            Toast.makeText(activity, "The file " + path + " appears to be missing.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Import a NotesSubject into the notes.
     * Duplicating titles for subject are checked within the function. **/
    private void importSubjectToDatabase(String title, ArrayList<ArrayList<String>> contents,
                                         int sortOrder) {
        if (title.length() > 0) {
            SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);
            // Import to notes
            if (database.SubjectDao().search(title) != null) {
                // Subject conflict
                new AutoDismissDialog(activity.getString(R.string.subject_conflict),
                        activity.getString(R.string.error_subject_same_name),
                        new String[]{activity.getString(R.string.merge),
                                activity.getString(R.string.rename),
                                activity.getString(android.R.string.cancel)},
                        new DialogInterface.OnClickListener[]{(dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            mergeSubjects(title, contents);
                        }, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            showRenameDialog(title, contents, sortOrder);
                        }, (dialogInterface, i) -> dialogInterface.dismiss()})
                        .show(activity.getSupportFragmentManager(), "ImportSubject.4");
            } else {
                database.SubjectDao().insert(new NotesSubject(title, contents, sortOrder));
                Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(title));
            }
            database.close();
        } else {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: Title of subject in ZIP file  invalid.");
            Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    /** Display the renaming dialog for the conflicted subject.
     * Separated from importSubjectToDatabase(String title,
     * ArrayList<ArrayList<String>> contents, int sortOrder) for clarity. **/
    private void showRenameDialog(String title, ArrayList<ArrayList<String>> contents, int sortOrder) {
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setText(title);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);

        DialogInterface.OnShowListener renameListener = dialogInterface -> {
            ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                String inputText = "";
                if (inputLayout.getEditText() != null) {
                    inputText = inputLayout.getEditText().getText().toString();
                }
                if (inputText.length() > 0 && database.SubjectDao().search(inputText) == null) {
                    // Import subject into notes
                    database.SubjectDao().insert(new NotesSubject(inputText, contents, sortOrder));
                    dialogInterface.dismiss();
                    Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                    activity.safeOnBackPressed();
                    activity.displayFragment(NotesSubjectFragment.newInstance(inputText));
                } else if (inputText.length() > 0) {
                    inputLayout.setErrorEnabled(true);
                    inputLayout.setError(activity.getString(R.string.error_subject_exists));
                } else {
                    Log.w(MainActivity.LOG_APP_NAME, "TextInputLayout Error: getEditText() for " +
                            "AlertDialog in ImportSubject.showRenameDialog not found.");
                }
            });
            ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setOnClickListener(view -> dialogInterface.dismiss());
        };
        AutoDismissDialog renameDialog = new AutoDismissDialog(
                activity.getString(R.string.rename_subject), inputLayout,
                new String[]{activity.getString(android.R.string.ok),
                        activity.getString(android.R.string.cancel), ""}, renameListener);
        renameDialog.setDismissListener(dialogInterface -> database.close());
        renameDialog.show(activity.getSupportFragmentManager(), "ImportSubject.5");
    }

    /** Merge two conflicted subjects with the same name.
     * Notes that are exactly the same will not be re-imported,
     * sort order will inherit the original subject stored on the notes. **/
    private void mergeSubjects(String title, ArrayList<ArrayList<String>> newContent) {
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(activity);
        NotesSubject editSubject = database.SubjectDao().search(title);
        ArrayList<ArrayList<String>> oldContents = editSubject.contents;

        // Compare and add notes if necessary
        int oldContentsSize = oldContents.size();
        if (newContent != null && newContent.size() > 0) {
            for (ArrayList<String> note: newContent) {
                // Compare each note to the former notes in the old content
                boolean notePresent = false;
                // A for loop is used to prevent the new notes from being compared to
                for (int i = 0; i < oldContentsSize; i++) {
                    if (Objects.equals(note, oldContents.get(i))) {
                        notePresent = true;
                        break;
                    }
                }

                if (!notePresent) {
                    // Add note to oldContents
                    oldContents.add(note);
                }
            }
        }

        editSubject.contents = oldContents;
        database.SubjectDao().update(editSubject);
        database.close();
        // Go to the fragment
        Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
        activity.safeOnBackPressed();
        activity.displayFragment(NotesSubjectFragment.newInstance(title));
    }
}
