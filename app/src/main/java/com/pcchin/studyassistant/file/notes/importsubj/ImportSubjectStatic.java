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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.activity.MainActivity;

/** Static functions used  in ImportSubject. **/
public final class ImportSubjectStatic {
    /** Displays the import dialog for whether to import from a ZIP or a .subject file.
     * Separated from the constructor as this function will startActivityForResult
     * before continuing on with the rest of the functions in the class. **/
    public static void displayImportDialog(MainActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(activity, Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ActivityConstants.EXTERNAL_STORAGE_READ_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            new AutoDismissDialog(activity.getString(R.string.import_from), R.array.n_import_subject_format,
                    (dialogInterface, i) -> initFileChooser(activity, i),
                    new String[]{"", "", ""}, new DialogInterface.OnClickListener[]{null, null, null})
                    .show(activity.getSupportFragmentManager(), "ImportSubject.1");
        } else {
            Toast.makeText(activity, R.string.error_read_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    /** Initializes the file chooser after the user picks an option from the import dialog. **/
    private static void initFileChooser(MainActivity activity, int i) {
        try {
            // Set up file chooser
            Intent fileSelectIntent = new Intent(Intent.ACTION_GET_CONTENT);
            fileSelectIntent.setType("*/*");
            if (i == 0) {
                String[] mimeType = {"application/zip", "application/x-compressed",
                        "application/x-zip-compressed"};
                fileSelectIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
                activity.startActivityForResult(Intent.createChooser(fileSelectIntent,
                        activity.getString(R.string.select_file)), ActivityConstants.SELECT_ZIP_FILE);
            } else {
                Toast.makeText(activity, R.string.select_subject_file, Toast.LENGTH_SHORT).show();
                activity.startActivityForResult(fileSelectIntent, ActivityConstants.SELECT_SUBJECT_FILE);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.error_file_manager_not_found, Toast.LENGTH_SHORT).show();
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: This device appears to "
                    + "not have a file manager. Stack trace is");
            e.printStackTrace();
        }
    }
}
