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

package com.pcchin.studyassistant.file.project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.fragment.project.settings.ProjectSettingsFragment;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.preference.PreferenceString;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/** A class used to import the image icon for a specific project.
 * As this class extends Thread, it should be used as such as well. **/
public class ImportProjectIcon extends Thread {
    private MainActivity activity;
    private ProjectDatabase database;
    private ProjectData project;
    private String id2;
    private boolean isMember;
    private String iconPath;

    /** The constructor for the class as activity needs to be passed on. **/
    public ImportProjectIcon(MainActivity activity, @NonNull Intent data) {
        this.activity = activity;
        this.database = GeneralFunctions.getProjectDatabase(activity);
        this.project = database.ProjectDao().searchByID(data.getStringExtra(ActivityConstants.INTENT_PROJECT_ID));
        this.id2 = data.getStringExtra(ActivityConstants.INTENT_ID2);
        this.isMember = data.getBooleanExtra(ActivityConstants.INTENT_IS_MEMBER, false);
        this.iconPath = FileFunctions.getRealPathFromUri(activity, data.getData());
    }

    /** Runs the import project icon program. **/
    @Override
    public void run() {
        // Checks for read permission before starting
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                copyIcon();
            } catch (IOException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, String.format("File Error: Unable to copy project" +
                        " icon from %s to internal directory.", iconPath));
                Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, R.string.error_read_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    /** Copy the icon selected for the project into the project's internal directory.  **/
    private void copyIcon() throws IOException {
        String targetFilePath = GeneralFunctions.getProjectIconPath(activity, project.projectID);
        FileFunctions.copyFile(new File(iconPath), new File(targetFilePath));
        // Saves the icon to the database
        project.hasIcon = true;
        database.ProjectDao().update(project);
        database.close();
        activity.runOnUiThread(() -> Toast.makeText(activity, R.string.p3_general_icon_updated, Toast.LENGTH_SHORT).show());
        // Go to the settings page if it is not at the settings for the imported project.
        if (!(activity.currentFragment instanceof ProjectSettingsFragment
                && Objects.equals(((ProjectSettingsFragment) activity.currentFragment)
                .project.projectID, project.projectID))) {
            activity.runOnUiThread(() -> {
                activity.safeOnBackPressed();
                activity.displayFragment(ProjectSettingsFragment.newInstance(project.projectID, id2, isMember));
                ((ProjectSettingsFragment) activity.currentFragment).displayPreference(PreferenceString.PREF_MENU_GENERAL);
            });
        }
    }
}
