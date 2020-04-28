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

package com.pcchin.studyassistant.fragment.project.settings;

import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.NavViewFunctions;
import com.pcchin.studyassistant.preference.PreferenceString;
import com.pcchin.studyassistant.activity.MainActivity;

import java.util.Date;

/** Functions that are called when a preference is changed in
 * @see ProjectSettingsFragment **/
final class ProjectSettingsFragmentChange {
    private ProjectSettingsFragment fragment;
    private MainActivity activity;

    /** Constructor for the class as fragment needs to be passed on. **/
    ProjectSettingsFragmentChange(ProjectSettingsFragment fragment) {
        this.fragment = fragment;
        this.activity = (MainActivity) fragment.getActivity();
    }

    /** Detects the value change of general preferences. **/
    void generalPrefChanged(@NonNull Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_UPDATE_TITLE:
                // Set activity title
                activity.setTitle((String) newValue);
                fragment.project.projectTitle = (String) newValue;
                break;
            case PreferenceString.PREF_UPDATE_DESC:
                fragment.project.description = (String) newValue;
                break;
            case PreferenceString.PREF_COMPLETED:
                fragment.project.projectOngoing = !(boolean) newValue;
                break;
        }
        fragment.updateProject();
    }

    /** Detects the value change of feature preferences. **/
    void featurePrefChanged(@NonNull Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_MEMBERS:
                fragment.project.membersEnabled = (boolean) newValue;
                break;
            case PreferenceString.PREF_ROLES:
                fragment.project.rolesEnabled = (boolean) newValue;
                break;
            case PreferenceString.PREF_TASKS:
                fragment.project.taskEnabled = (boolean) newValue;
                break;
            case PreferenceString.PREF_STATUS:
                fragment.project.statusEnabled = (boolean) newValue;
                break;
            case PreferenceString.PREF_MERGE_TASK_STATUS:
                fragment.project.mergeTaskStatus = (boolean) newValue;
                break;
            case PreferenceString.PREF_STATUS_ICON:
                switch ((String) newValue) {
                    case "None":
                        fragment.project.projectStatusIcon = R.string.blank;
                        break;
                    case "Circle":
                        fragment.project.projectStatusIcon = R.drawable.status_ic_circle;
                        break;
                    case "Triangle":
                        fragment.project.projectStatusIcon = R.drawable.status_ic_triangle;
                        break;
                    case "Square":
                        fragment.project.projectStatusIcon = R.drawable.status_ic_square;
                        break;
                }
                break;
            case PreferenceString.PREF_RELATED_SUBJECT:
                fragment.project.associatedSubject = (String) newValue;
                break;
        }
        fragment.updateProject();
    }

    /** Detects the value change of date preferences. **/
    void datePrefChanged(@NonNull Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_SET_EXPECTED_START:
            case PreferenceString.PREF_UPDATE_EXPECTED_START:
                fragment.project.expectedStartDate = new Date((long) newValue);
                break;
            case PreferenceString.PREF_SET_EXPECTED_END:
            case PreferenceString.PREF_UPDATE_EXPECTED_END:
                fragment.project.expectedEndDate = new Date((long) newValue);
                break;
            case PreferenceString.PREF_SET_ACTUAL_START:
            case PreferenceString.PREF_UPDATE_ACTUAL_START:
                fragment.project.actualStartDate = new Date((long) newValue);
                break;
            case PreferenceString.PREF_SET_ACTUAL_END:
            case PreferenceString.PREF_UPDATE_ACTUAL_END:
                fragment.project.actualEndDate = new Date((long) newValue);
                break;
        }
        fragment.updateProject();
    }

    /** Detects the value change of security preferences. **/
    void securityPrefChanged(@NonNull Preference preference, Object newValue,
                             ProjectDatabase projectDatabase) {
        switch (preference.getKey()) {
            case PreferenceString.PREF_SET_PW:
                fragment.project.projectProtected = true;
            case PreferenceString.PREF_UPDATE_PW:
                // Project password is already hashed within the preference
                fragment.project.projectPass = (String) newValue;
                fragment.updateProject();
                break;
            case PreferenceString.PREF_DELETE_PROJECT:
                // Delete the project would kick you back to ProjectSelectFragment
                // Handler used here to reduce lag
                new Handler().postDelayed(() -> {
                    projectDatabase.ProjectDao().delete(fragment.project);
                    Toast.makeText(activity, R.string.p3_project_deleted, Toast.LENGTH_SHORT).show();
                    NavViewFunctions.updateNavView(activity);
                    activity.displayFragment(new ProjectSelectFragment());
                }, 0);
                break;
        }
    }
}
