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

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.preference.PreferenceString;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/** Functions that are called to customize preferences in
 * @see ProjectSettingsFragment **/
final class ProjectPreferenceCustomize {
    private final ProjectSettingsFragment fragment;
    private final MainActivity activity;
    
    /** Constructor for the class as fragment needs to be passed on. **/
    ProjectPreferenceCustomize(ProjectSettingsFragment fragment) {
        this.fragment = fragment;
        this.activity = (MainActivity) fragment.requireActivity();
    }

    /** Customize the general preferences. **/
    void customizeGeneralPreference() {
        ((EditTextPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_UPDATE_TITLE)))
                .setText(fragment.project.projectTitle);
        ((EditTextPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_UPDATE_DESC)))
                .setText(fragment.project.description);
        if (fragment.project.hasIcon) {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_SET_ICON))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_UPDATE_ICON))).setVisible(false);
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_REMOVE_ICON))).setVisible(false);
        }
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_COMPLETED))).setChecked(!fragment.project.projectOngoing);
    }

    /** Customize the feature preferences. **/
    void customizeFeaturePreference() {
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_MEMBERS))).setChecked(fragment.project.membersEnabled);
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_ROLES))).setChecked(fragment.project.rolesEnabled);
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_TASKS))).setChecked(fragment.project.taskEnabled);
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_STATUS))).setChecked(fragment.project.statusEnabled);
        ((SwitchPreference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_MERGE_TASK_STATUS))).setChecked(fragment.project.mergeTaskStatus);
        // Handler for setting related subject as database access is needed
        new Handler().postDelayed(() -> {
            SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
            List<NotesSubject> subjectList = database.SubjectDao().getAll();
            database.close();
            CharSequence[] subjectNameList = new CharSequence[subjectList.size()];
            CharSequence[] subjectIdList = new CharSequence[subjectList.size()];
            for (int i = 0; i < subjectList.size(); i++) {
                subjectNameList[i] = subjectList.get(i).title;
                subjectIdList[i] = String.valueOf(subjectList.get(i).subjectId);
            }
            ListPreference subjectPreference = Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_SET_RELATED_SUBJECT));
            subjectPreference.setEntries(subjectNameList);
            subjectPreference.setEntryValues(subjectIdList);
        }, 0);
        if (fragment.project.associatedSubject == null) {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_REMOVE_RELATED_SUBJECT))).setVisible(false);
        }
    }

    /** Customize the date preferences. **/
    void customizeDatePreference() {
        customizeDatePref(fragment.project.expectedStartDate, PreferenceString.PREF_SET_EXPECTED_START,
                PreferenceString.PREF_UPDATE_EXPECTED_START, PreferenceString.PREF_REMOVE_EXPECTED_START);
        customizeDatePref(fragment.project.expectedEndDate, PreferenceString.PREF_SET_EXPECTED_END,
                PreferenceString.PREF_UPDATE_EXPECTED_END, PreferenceString.PREF_REMOVE_EXPECTED_END);
        customizeDatePref(fragment.project.actualStartDate, PreferenceString.PREF_SET_ACTUAL_START,
                PreferenceString.PREF_UPDATE_ACTUAL_START, PreferenceString.PREF_REMOVE_ACTUAL_START);
        customizeDatePref(fragment.project.actualEndDate, PreferenceString.PREF_SET_ACTUAL_END,
                PreferenceString.PREF_UPDATE_ACTUAL_END, PreferenceString.PREF_REMOVE_ACTUAL_END);
    }

    /** Customize the preference of a specific date. **/
    private void customizeDatePref(Date date, String setPref,
                                   String updatePref, String deletePref) {
        if (date == null) {
            ((Preference) Objects.requireNonNull(fragment.findPreference(updatePref))).setVisible(false);
            ((Preference) Objects.requireNonNull(fragment.findPreference(deletePref))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(fragment.findPreference(setPref))).setVisible(false);
        }
    }

    /** Customize the security preferences. **/
    void customizeSecurityPreference() {
        if (fragment.project.projectPass.length() == 0) {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_UPDATE_PW))).setVisible(false);
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_REMOVE_PW))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_SET_PW))).setVisible(false);
        }
        // The role for the member would be set to fragment.role so there is no other check for members
        if (fragment.role == null || !fragment.role.canDeleteProject) {
            ((Preference) Objects.requireNonNull(fragment.findPreference(PreferenceString.PREF_DELETE_PROJECT))).setVisible(false);
        }
    }
}
