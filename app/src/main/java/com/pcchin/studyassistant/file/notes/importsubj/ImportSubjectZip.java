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

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/** Functions used to import ZIP files in ImportSubject. **/
public class ImportSubjectZip {
    private final MainActivity activity;

    /** The constructor for the class as activity needs to be passed on. **/
    public ImportSubjectZip(MainActivity activity) {
        this.activity = activity;
    }

    /** Function used to confirm whether the ZIP file is valid and the password is provided
     * before unzipping the ZIP file. Separated from constructor for clarity. **/
    public void importZipConfirm(String path) {
        try {
            if (new ZipFile(path).isValidZipFile()) {
                displayZipDialog(path);
            } else {
                Log.e(ActivityConstants.LOG_APP_NAME, "File Error: ZIP file " + path + " is invalid.");
                Toast.makeText(activity, R.string.error_zip_corrupt, Toast.LENGTH_SHORT).show();
            }
        } catch (ZipException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: ZIP processing error occurred while "
                    + " importing a subject, stack trace is");
            Toast.makeText(activity, R.string.error_zip_import, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /** Displays the password dialog for importing ZIP files. **/
    private void displayZipDialog(String path) throws ZipException {
        if (new ZipFile(path).isEncrypted()) {
            @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout)
                    activity.getLayoutInflater().inflate(R.layout.popup_edittext, null);
            if (inputLayout.getEditText() != null) {
                inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);

            // Set up dialog
            DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(new AlertDialog.Builder(activity)
                    .setTitle(R.string.enter_password).setView(inputLayout).create());
            dismissibleFragment.setPositiveButton(activity.getString(android.R.string.ok),
                    view -> setPositiveButton(dismissibleFragment, path, inputLayout));
            dismissibleFragment.setNegativeButton(activity.getString(android.R.string.cancel),
                    view -> dismissibleFragment.dismiss());
            dismissibleFragment.show(activity.getSupportFragmentManager(), "ImportSubject.2");
        } else {
            // Error thrown in importZipFile will be handled by the external error handler
            ZipFile inputFile = new ZipFile(path);
            new ImportSubjectZipProcess(activity).importZipFile(inputFile);
        }
    }

    /** Sets the positive button for the zip password dialog. **/
    private void setPositiveButton(DismissibleDialogFragment dismissibleFragment, String path,
                                   @NonNull TextInputLayout inputLayout) {
        String password = "";
        if (inputLayout.getEditText() != null) {
            password = inputLayout.getEditText().getText().toString();
        }
        if (password.length() >= 8) {
            try {
                ZipFile inputFile = new ZipFile(path, password.toCharArray());
                new ImportSubjectZipProcess(activity).importZipFile(inputFile);
                dismissibleFragment.dismiss();
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
    }
}
