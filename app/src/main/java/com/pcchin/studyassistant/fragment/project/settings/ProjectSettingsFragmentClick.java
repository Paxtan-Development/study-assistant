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

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.preference.PreferenceString;

import java.io.File;
import java.util.Objects;

/** Functions that are used to handle preference clicks in
 * @see ProjectSettingsFragment **/
final class ProjectSettingsFragmentClick {
    private ProjectSettingsFragment fragment;
    private MainActivity activity;

    /** Constructor used as fragment needs to be passed on. **/
    ProjectSettingsFragmentClick(ProjectSettingsFragment fragment) {
        this.fragment = fragment;
        this.activity = (MainActivity) fragment.requireActivity();
    }

    /** The tree click listeners for the root preferences. **/
    void rootPreferenceClick(@NonNull Preference preference) {
        fragment.currentPrefRoot = preference.getKey();
        fragment.displayPreference(fragment.currentPrefRoot);
    }

    /** The tree click listeners for the general preferences. **/
    void generalPreferenceClick(@NonNull Preference preference) {
        if (activity != null) {
            String iconLocation = DatabaseFunctions.getProjectIconPath(activity, fragment.project.projectID);
            switch (preference.getKey()) {
                case PreferenceString.PREF_SET_ICON:
                case PreferenceString.PREF_UPDATE_ICON:
                    fragment.startIconPicker();
                    break;
                case PreferenceString.PREF_REMOVE_ICON:
                    // Remove icon
                    File iconFile = new File(iconLocation);
                    if (iconFile.delete()) {
                        fragment.project.hasIcon = false;
                    } else {
                        Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            fragment.updateProject();
        }
    }

    /** The tree click listeners for the feature preferences. **/
    void featurePreferenceClick(@NonNull Preference preference) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_STATUS_ICON:
                switch(fragment.project.projectStatusIcon) {
                    case R.string.blank:
                        ((ListPreference) preference).setValue("None");
                        break;
                    case R.drawable.status_ic_circle:
                        ((ListPreference) preference).setValue("Circle");
                        break;
                    case R.drawable.status_ic_triangle:
                        ((ListPreference) preference).setValue("Triangle");
                        break;
                    case R.drawable.status_ic_square:
                        ((ListPreference) preference).setValue("Square");
                }
                break;
            case PreferenceString.PREF_RELATED_SUBJECT:
                ((ListPreference) preference).setValue(fragment.project.associatedSubject);
                break;
        }
    }

    /** The tree click listeners for the date preferences. **/
    void datePreferenceClick(@NonNull Preference preference) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_REMOVE_EXPECTED_START:
                fragment.project.expectedStartDate = null;
                break;
            case PreferenceString.PREF_REMOVE_EXPECTED_END:
                fragment.project.expectedEndDate = null;
                break;
            case PreferenceString.PREF_REMOVE_ACTUAL_START:
                fragment.project.actualStartDate = null;
                break;
            case PreferenceString.PREF_REMOVE_ACTUAL_END:
                fragment.project.actualEndDate = null;
                break;
        }
        fragment.updateProject();
    }

    /** The tree click listeners for the security preferences. **/
    void securityPreferenceClick(@NonNull Preference preference) {
        if (Objects.equals(preference.getKey(), PreferenceString.PREF_REMOVE_PW)) {
            fragment.project.projectPass = "";
            fragment.project.projectProtected = false;
            fragment.updateProject();
        }
    }
}
