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


import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.pcchin.dtpreference.DatePreference;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
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

public class ProjectSettingsFragment extends PreferenceFragmentCompat implements ExtendedFragment,
        PreferenceManager.OnPreferenceTreeClickListener,
        Preference.OnPreferenceChangeListener {
    public ProjectData project;
    String currentPrefRoot = PreferenceString.PREF_MENU_ROOT;
    private String id2;
    private boolean isMember;
    private static final String ARG_ID = "projectID", ARG_ID2 = "ID2", ARG_IS_MEMBER = "isMember";
    private ProjectDatabase projectDatabase;

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
        if (getArguments() != null) {
            String projectID = getArguments().getString(ARG_ID);
            id2 = getArguments().getString(ARG_ID2);
            isMember = getArguments().getBoolean(ARG_IS_MEMBER);
            projectDatabase = GeneralFunctions.getProjectDatabase(requireActivity());
            project = projectDatabase.ProjectDao().searchByID(projectID);

            Object[] idValidity = UIFunctions.checkIdValidity(requireActivity(), projectDatabase,
                    project, id2, isMember);
            member = (MemberData) idValidity[1];
            role = (RoleData) idValidity[2];

            // idValidity[0] is equivalent to hasError
            if ((boolean) idValidity[0]) {
                // Return to ProjectSelectFragment if any error is found
                projectDatabase.close();
                ((MainActivity) requireActivity()).displayFragment(new ProjectSelectFragment());
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
        ProjectSettingsFragmentClick click = new ProjectSettingsFragmentClick(ProjectSettingsFragment.this);
        switch (currentPrefRoot) {
            case PreferenceString.PREF_MENU_ROOT:
                click.rootPreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_GENERAL:
                click.generalPreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_FEATURES:
                click.featurePreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_DATE:
                click.datePreferenceClick(preference);
                break;
            case PreferenceString.PREF_MENU_SECURITY:
                click.securityPreferenceClick(preference);
                break;
        }
        return false;
    }

    /** Starts the image picker to pick an icon. **/
    void startIconPicker() {
        // Activity is used here instead of the fragment as the inline data handling requires a FragmentActivity
        ImagePicker.Companion.with(requireActivity())
                .cropSquare()
                .compress(1024)
                .start();
        ((MainActivity) requireActivity()).setProjectInfo(project.projectID, id2, isMember);
    }

    /** Delegates the show/hide of preferences to their own functions within
     * @see ProjectSettingsFragmentCustomize
     * No need to customize PREF_ROOT as there is nothing to customize. **/
    public void displayPreference(@NonNull String key) {
        ProjectSettingsFragmentCustomize customize = new ProjectSettingsFragmentCustomize(ProjectSettingsFragment.this);
        switch (key) {
            case PreferenceString.PREF_MENU_ROOT:
                setPreferencesFromResource(R.xml.p3_preference_list, key);
                break;
            case PreferenceString.PREF_MENU_GENERAL:
                setPreferencesFromResource(R.xml.p3_general_preference_list, key);
                customize.customizeGeneralPreference();
                break;
            case PreferenceString.PREF_MENU_FEATURES:
                setPreferencesFromResource(R.xml.p3_features_preference_list, key);
                customize.customizeFeaturePreference();
                break;
            case PreferenceString.PREF_MENU_DATE:
                setPreferencesFromResource(R.xml.p3_date_preference_list, key);
                customize.customizeDatePreference();
                break;
            case PreferenceString.PREF_MENU_SECURITY:
                setPreferencesFromResource(R.xml.p3_security_preference_list, key);
                customize.customizeSecurityPreference();
                break;
        }
        registerPrefListener();
    }

    /** Recursively register listeners for the preferences.
     * This is done inside a handler to reduce lag. **/
    private void registerPrefListener() {
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

    /** Delegates the value change of preferences to their own functions within
     * @see ProjectSettingsFragmentChange **/
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // No need for PREF_ROOT as it is handled in the tree click listeners
        ProjectSettingsFragmentChange change = new ProjectSettingsFragmentChange(ProjectSettingsFragment.this);
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
            dialogFragment = ProjectSettingsFragmentStatic.getDatePreferenceDialog(ProjectSettingsFragment.this, (DatePreference) preference);
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

    /** Returns to
     * @see ProjectInfoFragment **/
    @Override
    public boolean onBackPressed() {
        if (currentPrefRoot.equals(PreferenceString.PREF_MENU_ROOT)) {
            // Returns to ProjectInfoFragment
            projectDatabase.close();
            if (member == null) {
                ((MainActivity) requireActivity()).displayFragment(ProjectInfoFragment
                        .newInstance(project.projectID, role.roleID, false, true));
            } else {
                ((MainActivity) requireActivity()).displayFragment(ProjectInfoFragment
                        .newInstance(project.projectID, member.memberID, true, true));
            }
            return true;
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
            projectDatabase = GeneralFunctions.getProjectDatabase(requireActivity());
        }
    }
}
