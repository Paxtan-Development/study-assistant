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


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.pcchin.dtpreference.DatePreference;
import com.pcchin.dtpreference.dialog.DatePreferenceDialog;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.preference.DefaultDialogPreference;
import com.pcchin.studyassistant.preference.DefaultDialogPreferenceDialog;
import com.pcchin.studyassistant.preference.PasswordPreference;
import com.pcchin.studyassistant.preference.PasswordPreferenceDialog;
import com.pcchin.studyassistant.preference.PreferenceString;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.ui.MainActivity;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ProjectSettingsFragment extends PreferenceFragmentCompat implements ExtendedFragment,
        PreferenceManager.OnPreferenceTreeClickListener,
        Preference.OnPreferenceChangeListener {

    private String currentPrefRoot = PreferenceString.PREF_MENU_ROOT;
    private String id2;
    private boolean isMember;

    private static final String ARG_ID = "projectID",
            ARG_ID2 = "ID2",
            ARG_IS_MEMBER = "isMember";

    private ProjectDatabase projectDatabase;
    public ProjectData project;

    // Mutually exclusive unless the project has both of those enabled
    private MemberData member;
    private RoleData role;

    /** Default constructor. **/
    public ProjectSettingsFragment() {
        // Default constructor.
    }

    /** Used in all instances when creating new project.
     * @param ID2 can be either the role ID or member ID depending on the project.
     * @param isMember determines whether ID2 is a member ID or a role ID. **/
    @NonNull
    public static ProjectSettingsFragment newInstance(String projectID, String ID2, boolean isMember) {
        ProjectSettingsFragment fragment = new ProjectSettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_ID2, ID2);
        args.putBoolean(ARG_IS_MEMBER, isMember);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && getArguments() != null) {
            String projectID = getArguments().getString(ARG_ID);
            id2 = getArguments().getString(ARG_ID2);
            isMember = getArguments().getBoolean(ARG_IS_MEMBER);
            projectDatabase = GeneralFunctions.getProjectDatabase(getActivity());
            project = projectDatabase.ProjectDao().searchByID(projectID);

            Object[] idValidity = UIFunctions.checkIdValidity(getActivity(), projectDatabase,
                    project, id2, isMember);
            member = (MemberData) idValidity[1];
            role = (RoleData) idValidity[2];

            // idValidity[0] is equivalent to hasError
            if ((boolean) idValidity[0]) {
                // Return to ProjectSelectFragment if any error is found
                projectDatabase.close();
                ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            }
        }
    }

    /** Retrieve the settings for the project. **/
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.p3_preference_list);
        PreferenceManager pManager = getPreferenceManager();
        pManager.setOnPreferenceTreeClickListener(ProjectSettingsFragment.this);
    }

    /** Delegates the preference click listeners to their own functions. **/
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // No PREF_FEATURES as they have no features available
        switch (currentPrefRoot) {
            case PreferenceString.PREF_MENU_ROOT:
                rootPreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_GENERAL:
                generalPreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_FEATURES:
                featurePreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_DATE:
                datePreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_SECURITY:
                securityPreferenceClick();
                break;
        }
        return false;
    }

    /** The tree click listeners for the root preferences. **/
    private void rootPreferenceClick(@NonNull Preference preference) {
        currentPrefRoot = preference.getKey();
        displayPreference(currentPrefRoot);
    }

    /** The tree click listeners for the general preferences. **/
    private void generalPreferenceClick(@NonNull Preference preference) {
        if (getActivity() != null) {
            String iconLocation = getActivity().getFilesDir().toString() + "/icons/project/"
                    + project.projectID + ".jpg";
            switch (preference.getKey()) {
                case PreferenceString.PREF_SET_ICON:
                case PreferenceString.PREF_UPDATE_ICON:
                    // Set icon, continued in MainActivity
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(MainActivity.INTENT_PROJECT_ID, project.projectID);
                    intent.putExtra(MainActivity.INTENT_IS_MEMBER, isMember);
                    intent.putExtra(MainActivity.INTENT_ID2, id2);
                    startActivityForResult(intent, MainActivity.SELECT_PROJECT_ICON);
                    break;
                case PreferenceString.PREF_REMOVE_ICON:
                    // Remove icon
                    File iconFile = new File(iconLocation);
                    if (iconFile.delete()) {
                        project.hasIcon = false;
                    } else {
                        Toast.makeText(getContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            updateProject();
        }
    }

    /** The tree click listeners for the feature preferences. **/
    private void featurePreferenceClick(@NonNull Preference preference) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_STATUS_ICON:
                switch(project.projectStatusIcon) {
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
                ((ListPreference) preference).setValue(project.associatedSubject);
                break;
        }
    }

    /** The tree click listeners for the date preferences. **/
    private void datePreferenceClick(@NonNull Preference preference) {
        switch(preference.getKey()) {
            case PreferenceString.PREF_REMOVE_EXPECTED_START:
                project.expectedStartDate = null;
                break;
            case PreferenceString.PREF_REMOVE_EXPECTED_END:
                project.expectedEndDate = null;
                break;
            case PreferenceString.PREF_REMOVE_ACTUAL_START:
                project.actualStartDate = null;
                break;
            case PreferenceString.PREF_REMOVE_ACTUAL_END:
                project.actualEndDate = null;
                break;
        }
        updateProject();
    }

    /** The tree click listeners for the security preferences. **/
    private void securityPreferenceClick() {
        project.projectPass = "";
        project.projectProtected = false;
        updateProject();
    }

    /** Delegates the show/hide of preferences to their own functions. **/
    private void displayPreference(@NonNull String key) {
        // No need to customize PREF_ROOT as there is nothing to customize
        switch (key) {
            case PreferenceString.PREF_MENU_ROOT:
                setPreferencesFromResource(R.xml.p3_preference_list, key);
                break;
            case PreferenceString.PREF_MENU_GENERAL:
                setPreferencesFromResource(R.xml.p3_general_preference_list, key);
                customizeGeneralPreference();
                break;
            case PreferenceString.PREF_MENU_FEATURES:
                setPreferencesFromResource(R.xml.p3_features_preference_list, key);
                customizeFeaturePreference();
                break;
            case PreferenceString.PREF_MENU_DATE:
                setPreferencesFromResource(R.xml.p3_date_preference_list, key);
                customizeDatePreference();
                break;
            case PreferenceString.PREF_MENU_SECURITY:
                setPreferencesFromResource(R.xml.p3_security_preference_list, key);
                customizeSecurityPreference();
                break;
        }
        // Recursively register listener (Inside Handler to reduce lag)
        new Handler().postDelayed(() -> {
            PreferenceScreen pScreen = getPreferenceManager().getPreferenceScreen();
            for (int i = 0; i < pScreen.getPreferenceCount(); i++) {
                Preference currentPref = pScreen.getPreference(i);
                if (!currentPref.getClass().equals(Preference.class)) {
                    // Don't set up listeners for plain preferences as they are handled through clicks
                    // This allows for the preferences to be less resource intensive
                    currentPref.setOnPreferenceChangeListener(ProjectSettingsFragment.this);
                }
            }
        }, 0);
    }

    /** Customize the general preferences. **/
    private void customizeGeneralPreference() {
        ((EditTextPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_UPDATE_TITLE)))
                .setText(project.projectTitle);
        ((EditTextPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_UPDATE_DESC)))
                .setText(project.description);
        if (project.hasIcon) {
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_SET_ICON))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_UPDATE_ICON))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_REMOVE_ICON))).setVisible(false);
        }
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_COMPLETED))).setChecked(!project.projectOngoing);
    }

    /** Customize the feature preferences. **/
    private void customizeFeaturePreference() {
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_MEMBERS))).setChecked(project.membersEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_ROLES))).setChecked(project.rolesEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_TASKS))).setChecked(project.taskEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_STATUS))).setChecked(project.statusEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference(PreferenceString.PREF_MERGE_TASK_STATUS))).setChecked(project.mergeTaskStatus);
        // Handler for setting related subject as database access is needed
        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                SubjectDatabase database = GeneralFunctions.getSubjectDatabase(getActivity());
                List<NotesSubject> subjectList = database.SubjectDao().getAll();
                CharSequence[] subjectNameList = new CharSequence[subjectList.size()];
                for (int i = 0; i < subjectList.size(); i++) {
                    subjectNameList[i] = subjectList.get(i).title;
                }
                ListPreference subjectPreference = Objects.requireNonNull(findPreference(PreferenceString.PREF_RELATED_SUBJECT));
                subjectPreference.setEntries(subjectNameList);
                subjectPreference.setEntryValues(subjectNameList);
                database.close();
            }
        }, 0);
    }

    /** Customize the date preferences. **/
    private void customizeDatePreference() {
        customizeDatePref(project.expectedStartDate, PreferenceString.PREF_SET_EXPECTED_START,
                PreferenceString.PREF_UPDATE_EXPECTED_START, PreferenceString.PREF_REMOVE_EXPECTED_START);
        customizeDatePref(project.expectedEndDate, PreferenceString.PREF_SET_EXPECTED_END,
                PreferenceString.PREF_UPDATE_EXPECTED_END, PreferenceString.PREF_REMOVE_EXPECTED_END);
        customizeDatePref(project.actualStartDate, PreferenceString.PREF_SET_ACTUAL_START,
                PreferenceString.PREF_UPDATE_ACTUAL_START, PreferenceString.PREF_REMOVE_ACTUAL_START);
        customizeDatePref(project.actualEndDate, PreferenceString.PREF_SET_ACTUAL_END,
                PreferenceString.PREF_UPDATE_ACTUAL_END, PreferenceString.PREF_REMOVE_ACTUAL_END);
    }

    /** Customize the preference of a specific date. **/
    private void customizeDatePref(Date date, String setPref,
                                   String updatePref, String deletePref) {
        if (date == null) {
            ((Preference) Objects.requireNonNull(findPreference(updatePref))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference(deletePref))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference(setPref))).setVisible(false);
        }
    }

    /** Customize the security preferences. **/
    private void customizeSecurityPreference() {
        if (project.projectPass.length() == 0) {
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_UPDATE_PASSWORD))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_REMOVE_PASSWORD))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference(PreferenceString.PREF_SET_PASSWORD))).setVisible(false);
        }
    }

    /** Delegates the value change of preferences to their own functions. **/
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // No need for PREF_ROOT as it is handled in the tree click listeners
        ProjectSettingsFragmentChange change = new ProjectSettingsFragmentChange(
                ProjectSettingsFragment.this, (MainActivity) getActivity());
        switch (currentPrefRoot) {
            case PreferenceString.PREF_MENU_GENERAL:
                change.generalPrefChanged(preference, newValue);
                break;
            case PreferenceString.PREF_MENU_FEATURES:
                change.featurePrefChanged(preference, newValue);
                break;
            case PreferenceString.PREF_MENU_DATE:
                change.datePrefChanged(preference, newValue);
                break;
            case PreferenceString.PREF_MENU_SECURITY:
                change.securityPrefChanged(preference, newValue, projectDatabase);
                break;
        }
        return false;
    }

    /** Display the preference dialogs of custom dialog preferences.
     * This is delegated to their respective functions. **/
    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Use instanceof to check if the preference is one of
        // DatePreference, TimePreference or DateTimePreference
        DialogFragment dialogFragment = null;
        if (preference instanceof DatePreference) {
            dialogFragment = getDatePreferenceDialog((DatePreference) preference);
        } else if (preference instanceof PasswordPreference) {
            dialogFragment = PasswordPreferenceDialog.newInstance(preference.getKey(), project.salt);
        } else if (preference instanceof DefaultDialogPreference) {
            dialogFragment = DefaultDialogPreferenceDialog.newInstance(preference.getKey())
                .setPreference((DefaultDialogPreference) preference);
        }
        if (dialogFragment != null) {
            // If it is one of our preferences, show it
            dialogFragment.setTargetFragment(ProjectSettingsFragment.this, 0);
            dialogFragment.show(getParentFragmentManager(), "ProjectSettingsFragment.1");
        } else {
            // Let super handle it
            super.onDisplayPreferenceDialog(preference);
        }
    }

    /** Updates the project and updates the current preference page. **/
    void updateProject() {
        projectDatabase.ProjectDao().update(project);
        displayPreference(currentPrefRoot);
    }

    /** Gets the dialog fragments for date preferences. **/
    private DatePreferenceDialog getDatePreferenceDialog(@NonNull DatePreference preference) {
        long currentDate = Calendar.getInstance().getTimeInMillis();
        switch (preference.getKey()) {
            case PreferenceString.PREF_SET_EXPECTED_START:
            case PreferenceString.PREF_UPDATE_EXPECTED_START:
                // Expected start date must be before expected end date
                if (project.expectedEndDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey());
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(),
                            -1, project.expectedEndDate.getTime())
                            .setInitialDate(project.expectedStartDate.getTime());
                }
            case PreferenceString.PREF_SET_EXPECTED_END:
            case PreferenceString.PREF_UPDATE_EXPECTED_END:
                // Expected end date must be after expected start date
                if (project.expectedStartDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey());
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(),
                            project.expectedStartDate.getTime(), -1)
                            .setInitialDate(project.expectedEndDate.getTime());
                }
            case PreferenceString.PREF_SET_ACTUAL_START:
            case PreferenceString.PREF_UPDATE_ACTUAL_START:
                // Actual start date must be in the past and before the actual end date
                if (project.actualEndDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey(), -1, currentDate);
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(), -1, project.actualEndDate.getTime())
                            .setInitialDate(project.actualEndDate.getTime());
                }
            case PreferenceString.PREF_SET_ACTUAL_END:
            case PreferenceString.PREF_UPDATE_ACTUAL_END:
                // Actual end date must be in the past and after the actual start date
                if (project.actualStartDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey(), -1, currentDate);
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(),
                            project.actualStartDate.getTime(), currentDate)
                            .setInitialDate(project.actualStartDate.getTime());
                }
        }
        return null;
    }

    /** Returns to
     * @see ProjectInfoFragment **/
    @Override
    public boolean onBackPressed() {
        if (currentPrefRoot.equals(PreferenceString.PREF_MENU_ROOT)) {
            // Returns to ProjectInfoFragment
            projectDatabase.close();
            if (getActivity() != null) {
                if (member == null) {
                    ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                            .newInstance(project.projectID, role.roleID, false, true));
                } else {
                    ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                            .newInstance(project.projectID, member.memberID, true, true));
                }
                return true;
            }
            return false;
        } else {
            // Returns to the main preferences menu
            displayPreference(PreferenceString.PREF_MENU_ROOT);
            currentPrefRoot = PreferenceString.PREF_MENU_ROOT;
            return true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        projectDatabase.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!projectDatabase.isOpen()) {
            projectDatabase = GeneralFunctions.getProjectDatabase(getActivity());
        }
    }
}
