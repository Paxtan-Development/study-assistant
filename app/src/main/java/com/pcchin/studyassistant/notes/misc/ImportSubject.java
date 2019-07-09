package com.pcchin.studyassistant.notes.misc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;

import com.obsez.android.lib.filechooser.ChooserDialog;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

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

    /** The function used to import subjects, either via a ZIP file or a .subject file.
     * Also serves as the constructor for the class as activity needs to be passed on
     * from function to function.
     * importZipConfirm(String path) and importSubjectFile(String path) separated for clarity. **/
    public ImportSubject(MainActivity activity) {
        this.activity = activity;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.import_from)
                .setItems(R.array.n_import_subject_format, (dialogInterface, i) -> {
                    // Set up file chooser
                    ChooserDialog importFileDialog = new ChooserDialog(activity)
                            .withStartFile("/storage/emulated/0");
                    if (i == 0) {
                        importFileDialog = importFileDialog.withFilter(true, "zip")
                                .withChosenListener((s, file) -> importZipConfirm(s));
                    } else {
                        importFileDialog = importFileDialog.withFilter(true, "subject")
                                .withChosenListener((s, file) -> importSubjectFile(s));
                    }
                    importFileDialog.build().show();
                })
                .create().show();
    }

    /** Function used to confirm whether the ZIP file is valid and the password is provided
     * before unzipping the ZIP file. Separated from constructor for clarity. **/
    private void importZipConfirm(String path) {
        try {
            if (new ZipFile(path).isValidZipFile()) {
                if (new ZipFile(path).isEncrypted()) {
                    @SuppressLint("InflateParams") LinearLayout inputLayout = (LinearLayout) activity
                            .getLayoutInflater().inflate(R.layout.popup_edittext, null);
                    EditText popupInput = inputLayout.findViewById(R.id.popup_input);
                    TextView popupError = inputLayout.findViewById(R.id.popup_error);

                    // Asks for password
                    AlertDialog passwordDialog = new AlertDialog.Builder(activity)
                            .setTitle(R.string.enter_password)
                            .setView(inputLayout)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
                    passwordDialog.setOnShowListener(dialogInterface -> {
                        passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnClickListener(view -> {
                                    String password = popupInput.getText().toString();
                                    try {
                                        ZipFile inputFile = new ZipFile(path, password.toCharArray());
                                        importZipFile(inputFile);
                                    } catch (ZipException e) {
                                        if (e.getType() == ZipException.Type.WRONG_PASSWORD) {
                                            Log.w("StudyAssistant", "File Error: Password for ZIP file "
                                                    + path + " incorrect.");
                                            popupError.setText(R.string.error_password_incorrect);
                                        } else {
                                            // Same way of handling the error as the
                                            // external catch block
                                            Log.e("StudyAssistant", "File Error: " +
                                                    "ZIP processing error occurred while "
                                                    + " importing a subject, stack trace is");
                                            Toast.makeText(activity, R.string.error_zip_import,
                                                    Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        }
                                    }
                                    passwordDialog.dismiss();
                                });
                        passwordDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                                .setOnClickListener(view -> passwordDialog.dismiss());
                    });
                    passwordDialog.show();
                } else {
                    // Error thrown in importZipFile will be handled by the external error handler
                    ZipFile inputFile = new ZipFile(path);
                    importZipFile(inputFile);
                }
            } else {
                Log.e("StudyAssistant", "File Error: ZIP file " + path + " is invalid.");
                Toast.makeText(activity, R.string.error_zip_corrupt, Toast.LENGTH_SHORT).show();
            }
        } catch (ZipException e) {
            Log.e("StudyAssistant", "File Error: ZIP processing error occurred while "
                    + " importing a subject, stack trace is");
            Toast.makeText(activity, R.string.error_zip_import, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /** Function used to import/convert a ZIP file,
     * separated from importZipConfirm(String path) for clarity.  **/
    private void importZipFile(ZipFile inputFile) throws ZipException {
        Toast.makeText(activity, R.string.importing_subject, Toast.LENGTH_SHORT).show();
        String tempInputDirPath = FileFunctions.generateValidFile(
                "/storage/emulated/0/Download/.tempZip", "");
        inputFile.extractAll(tempInputDirPath);

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
                        try {
                            Scanner scanner = new Scanner(file);
                            while (scanner.hasNext()) {
                                subjInfo.add(scanner.next());
                            }
                            scanner.close();
                        } catch (FileNotFoundException e) {
                            Log.e("StudyAssistant", "File Error: " + file.getAbsolutePath()
                                + " not found despite contained in file list of parent folder. " +
                                    "Stack trace is");
                            e.printStackTrace();
                        }
                    }
                }

                if (subjInfo.size() >= 2) {
                    title = subjInfo.get(0);
                    listOrder = Integer.valueOf(subjInfo.get(1));
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
                            Scanner currentFileScanner = new Scanner(file);
                            while (currentFileScanner.hasNext()) {
                                fileContents.append(currentFileScanner.next()).append("\n");
                            }

                            // Check if any notes match the info stored in the subject
                            boolean noteAdded = false;
                            for (int i = 0; i < notesInfo.size(); i++) {
                                if (Objects.equals(notesInfo.get(i).get(0), fileName)) {
                                    // Add note info to ArrayList
                                    ArrayList<String> currentNote = new ArrayList<>();
                                    currentNote.add(notesInfo.get(i).get(1));
                                    currentNote.add(GeneralFunctions.standardDateTimeFormat
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
                                tempArray.add(GeneralFunctions.standardDateTimeFormat.format(new Date()));
                                tempArray.add(fileContents.toString());

                                // For lock, alert time & request code respectively
                                for (int i = 0; i < 3; i++) {
                                    tempArray.add(null);
                                }

                                content.add(tempArray);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        Log.e("StudyAssistant", "File Error: ");
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
                Log.w("StudyAssistant", "File Error: Title of subject in ZIP file "
                        + inputFile.getFile().getAbsolutePath() + " invalid.");
                Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w("StudyAssistant", "File Error: Folder "+ tempInputDirPath
                    + " is not found");
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Function used to import a subject using a .subject file.
     * Separated from constructor for clarity. **/
    private void importSubjectFile(String path) {
        // Check if file exists
        File targetFile = new File(path);
        if (targetFile.exists() && targetFile.isFile()) {
            try {
                FileInputStream inputStream = new FileInputStream(targetFile);
                int fileSize = (int) inputStream.getChannel().size();
                // Length of title is first 4 bytes, title follows next
                int titleLength = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
                String title = new String(FileFunctions.getBytesFromFile(titleLength, inputStream));
                // Then the int containing the sort order is returned
                int sortOrder = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
                // Then the content of the subject is returned
                byte[] content = FileFunctions.getBytesFromFile(fileSize - titleLength - 8, inputStream);
                inputStream.close();

                @SuppressLint("InflateParams") LinearLayout inputLayout = (LinearLayout) activity
                        .getLayoutInflater().inflate(R.layout.popup_edittext, null);
                EditText popupInput = inputLayout.findViewById(R.id.popup_input);
                TextView popupError = inputLayout.findViewById(R.id.popup_error);

                AlertDialog importDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.enter_password)
                        .setView(inputLayout)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                importDialog.setOnShowListener(dialogInterface -> {
                    importDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        Toast.makeText(activity, R.string.importing_subject, Toast.LENGTH_SHORT).show();

                        String password = popupInput.getText().toString();
                        ArrayList<ArrayList<String>> subjectContents = SecurityFunctions
                                .subjectDecrypt(title, password, content);
                        if (subjectContents == null) {
                            popupError.setText(R.string.error_password_incorrect);
                        } else {
                            importSubjectToDatabase(title, subjectContents, sortOrder);
                            importDialog.dismiss();
                        }
                    });
                    importDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view ->
                            importDialog.dismiss());
                });
                importDialog.show();
            } catch (IOException e) {
                Log.e("StudyAssistant", "File Error: File " + path + " could not be read"
                        + " by FileInputStream. Stack trace is");
                Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        } else {
            Log.e("StudyAssistant", "File Error: File " + path + " is not found.");
            Toast.makeText(activity, "The file " + path + " appears to be missing.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Import a NotesSubject into the database.
     * Duplicating titles for subject are checked within the function. **/
    private void importSubjectToDatabase(String title, ArrayList<ArrayList<String>> contents,
                                         int sortOrder) {
        if (title.length() > 0) {
            SubjectDatabase database = Room.databaseBuilder(activity, SubjectDatabase.class,
                    "notesSubject")
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
            // Import to database
            if (database.SubjectDao().search(title) != null) {
                // Subject conflict
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.subject_conflict)
                        .setMessage(R.string.error_subject_same_name)
                        .setPositiveButton(R.string.merge, (dialogInterface, i) ->
                                mergeSubjects(title, contents))
                        .setNegativeButton(R.string.rename, (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                            showRenameDialog(title, contents, sortOrder);
                        })
                        .setNeutralButton(android.R.string.cancel,
                                (dialogInterface, i) -> dialogInterface.dismiss())
                        .create().show();
            } else {
                database.SubjectDao().insert(new NotesSubject(title, contents, sortOrder));
                Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(title));
            }
            database.close();
        } else {
            Log.w("StudyAssistant", "File Error: Title of subject in ZIP file  invalid.");
            Toast.makeText(activity, R.string.error_subject_title_invalid, Toast.LENGTH_SHORT).show();
        }
    }

    /** Display the renaming dialog for the conflicted subject.
     * Separated from importSubjectToDatabase(String title,
     * ArrayList<ArrayList<String>> contents, int sortOrder) for clarity. **/
    private void showRenameDialog(String title, ArrayList<ArrayList<String>> contents, int sortOrder) {
        @SuppressLint("InflateParams") LinearLayout inputLayout = (LinearLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        TextView popupInput = inputLayout.findViewById(R.id.popup_input);
        popupInput.setText(title);
        TextView popupError = inputLayout.findViewById(R.id.popup_error);
        SubjectDatabase database = Room.databaseBuilder(activity, SubjectDatabase.class,
                "notesSubject")
                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                .allowMainThreadQueries().build();

        AlertDialog renameDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.rename_subject)
                .setView(inputLayout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        renameDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String inputText = popupInput.getText().toString();
            if (database.SubjectDao().search(inputText) == null) {
                // Import subject into database
                database.SubjectDao().insert(new NotesSubject(inputText, contents, sortOrder));
                renameDialog.dismiss();
                Toast.makeText(activity, R.string.subject_imported, Toast.LENGTH_SHORT).show();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(inputText));
            } else {
                popupError.setText(R.string.error_subject_exists);
            }
        });
        renameDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setOnClickListener(view -> renameDialog.dismiss());
        renameDialog.setOnDismissListener(dialogInterface -> database.close());
        renameDialog.show();
    }

    /** Merge two conflicted subjects with the same name.
     * Notes that are exactly the same will not be re-imported,
     * sort order will inherit the original subject stored on the database. **/
    private void mergeSubjects(String title, ArrayList<ArrayList<String>> newContent) {
        SubjectDatabase database = Room.databaseBuilder(activity, SubjectDatabase.class,
                "notesSubject")
                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                .allowMainThreadQueries().build();
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
