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
import android.content.DialogInterface;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

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
            try (FileInputStream inputStream = new FileInputStream(targetFile)) {
                processSubjectFile(inputStream, path);
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

    /** Processes the .subject file. **/
    private void processSubjectFile(@NonNull FileInputStream inputStream, String path) throws IOException {
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
            importEncryptedSubject(title, sortOrder, content);
        } else {
            importUnencryptedSubject(title, sortOrder, path, content);
        }
    }

    /** Imports an encrypted subject. **/
    private void importEncryptedSubject(String title, int sortOrder, byte[] content) {
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
                new Handler().post(() -> decryptSubject(dialogInterface, inputLayout, title, sortOrder, content));
            });
            ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view ->
                    dialogInterface.dismiss());
        };
        new AutoDismissDialog(activity.getString(R.string.enter_password), inputLayout, importListener)
                .show(activity.getSupportFragmentManager(), "ImportSubject.3");
    }

    /** Decrypts and imports the subject. Done in a handler to prevent
     *  the main thread from being overloaded. **/
    private void decryptSubject(DialogInterface dialogInterface, @NonNull TextInputLayout inputLayout,
                                String title, int sortOrder, byte[] content) {
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
            new ImportSubject(activity)
                    .importSubjectToDatabase(title, subjectContents, sortOrder);
            dialogInterface.dismiss();
        }
    }

    /** Imports an unencrypted subject. **/
    private void importUnencryptedSubject(String title, int sortOrder, String path, byte[] content) {
        // Subject is not encrypted
        String contentString = new String(content);
        if (ConverterFunctions.doubleJsonToArray(contentString) == null) {
            Log.w(MainActivity.LOG_APP_NAME, "File Error: The .subject file "
                    + path + " could not be imported as its content is incorrect.");
            Toast.makeText(activity, R.string.error_subject_import, Toast.LENGTH_SHORT).show();
        } else {
            new ImportSubject(activity).importSubjectToDatabase(title, ConverterFunctions
                    .doubleJsonToArray(contentString), sortOrder);
        }
    }
}
