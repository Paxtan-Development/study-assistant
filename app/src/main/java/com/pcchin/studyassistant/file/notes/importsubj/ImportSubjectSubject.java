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

import android.annotation.SuppressLint;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.InflaterInputStream;

import io.sentry.Sentry;
import io.sentry.event.EventBuilder;

/** Functions for importing a subject from a .subject file. Yes, that's why it has this name. **/
public class ImportSubjectSubject {
    private final MainActivity activity;

    /** The constructor for the class as activity needs to be passed on. **/
    public ImportSubjectSubject(MainActivity activity) {
        this.activity = activity;
    }

    /** Function used to import a subject using a .subject file.
     * Separated from constructor for clarity. **/
    public void importSubjectFile(String path) {
        // Check if file exists
        File targetFile = new File(path);
        if (targetFile.exists() && targetFile.isFile()) {
            try (FileInputStream inputStream = new FileInputStream(targetFile);
                 InflaterInputStream inflatedStream = new InflaterInputStream(inputStream)) {
                processSubjectFile(inflatedStream);
            } catch (IOException | ArrayIndexOutOfBoundsException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File " + path + " could not be read"
                        + " by FileInputStream. Stack trace is");
                Toast.makeText(activity, R.string.error_subject_import, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                Sentry.capture(e);
            }
        } else {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: File " + path + " is not found.");
            Toast.makeText(activity, "The file " + path + " appears to be missing.",
                    Toast.LENGTH_SHORT).show();
            Sentry.capture(new EventBuilder().withMessage("File Error: The following file is not " +
                    "found.").withExtra("path", path));
        }
    }

    /** Processes the .subject file. **/
    private void processSubjectFile(@NonNull InflaterInputStream inputStream)
            throws IOException, IndexOutOfBoundsException {
        // Length of title is first 4 bytes, title follows next
        int titleLength = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
        String title = new String(FileFunctions.getBytesFromFile(titleLength, inputStream));
        // Then the int containing the sort order is returned
        int sortOrder = ConverterFunctions.bytesToInt(FileFunctions.getBytesFromFile(4, inputStream));
        // Then whether the byte containing whether the subject is encrypted is returned
        byte subjectEncrypted = FileFunctions.getBytesFromFile(1, inputStream)[0];
        byte[] content;

        if (subjectEncrypted == 1) {
            // If the subject is encrypted, take the salt and then the content
            byte[] salt = FileFunctions.getBytesFromFile(32, inputStream);
            content = FileFunctions.getRemainingBytesFromFile(inputStream);
            inputStream.close();
            importEncryptedSubject(title, sortOrder, salt, content);
        } else {
            // Take the content directly
            content = FileFunctions.getRemainingBytesFromFile(inputStream);
            inputStream.close();
            importUnencryptedSubject(title, sortOrder, content);
        }
    }

    /** Imports an encrypted subject. **/
    private void importEncryptedSubject(String title, int sortOrder, byte[] salt, byte[] content) {
        // Subject is encrypted
        @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        // Set up input layout
        if (inputLayout.getEditText() != null) {
            inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        // Generate subject
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        int subjectId = DatabaseFunctions.generateValidId(database, DatabaseFunctions.SubjIdType.SUBJECT);
        database.close();
        NotesSubject subject = new NotesSubject(subjectId, title, sortOrder);
        // Show password dialog
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(new AlertDialog.Builder(activity)
                .setTitle(R.string.enter_password).setView(inputLayout).create());
        dismissibleFragment.setPositiveButton(activity.getString(android.R.string.ok), view -> {
            Toast.makeText(activity, R.string.importing_subject, Toast.LENGTH_SHORT).show();
            new Handler().post(() -> decryptSubject(dismissibleFragment, inputLayout, subject, salt, content));
        });
        dismissibleFragment.setNegativeButton(activity.getString(android.R.string.cancel), view ->
                dismissibleFragment.dismiss());
        dismissibleFragment.show(activity.getSupportFragmentManager(), "ImportSubject.3");
    }

    /** Decrypts and imports the subject. Done in a handler to prevent
     *  the main thread from being overloaded. **/
    private void decryptSubject(DismissibleDialogFragment dismissibleFragment, @NonNull TextInputLayout inputLayout,
                                NotesSubject subject, byte[] salt, byte[] content) {
        String password = "";
        if (inputLayout.getEditText() != null) {
            password = inputLayout.getEditText().getText().toString();
        }
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        List<Integer> notesIdList = database.ContentDao().getAllNoteId();
        database.close();
        List<NotesContent> subjectContents = SecurityFunctions
                .subjectDecrypt(notesIdList, subject, salt, password, content);
        if (subjectContents == null) {
            inputLayout.setErrorEnabled(true);
            inputLayout.setError(activity.getString(R.string.error_password_incorrect));
        } else {
            new ImportSubject(activity).importSubjectToDatabase(subject, subjectContents);
            dismissibleFragment.dismiss();
        }
    }

    /** Imports an unencrypted subject. **/
    private void importUnencryptedSubject(String title, int sortOrder, byte[] content) {
        // Subject is not encrypted, create NotesSubject
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        int subjectId = DatabaseFunctions.generateValidId(database, DatabaseFunctions.SubjIdType.SUBJECT);
        List<Integer> notesIdList = database.ContentDao().getAllNoteId();
        database.close();

        String contentString = new String(content);
        NotesSubject subject = new NotesSubject(subjectId, title, sortOrder);
        List<NotesContent> notesList = ConverterFunctions.stringToNotesList(notesIdList, subjectId, contentString);

        if (notesList == null) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: A .subject file could not be imported as its content is incorrect.");
            Toast.makeText(activity, R.string.error_subject_import, Toast.LENGTH_SHORT).show();
        } else {
            new ImportSubject(activity).importSubjectToDatabase(subject, notesList);
        }
    }
}
