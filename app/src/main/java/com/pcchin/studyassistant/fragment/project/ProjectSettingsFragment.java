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

package com.pcchin.studyassistant.fragment.project;


import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.room.Room;

import com.pcchin.dtpreference.DatePreference;
import com.pcchin.dtpreference.dialog.DatePreferenceDialog;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.preference.DefaultDialogPreference;
import com.pcchin.studyassistant.preference.DefaultDialogPreferenceDialog;
import com.pcchin.studyassistant.preference.PasswordPreference;
import com.pcchin.studyassistant.preference.PasswordPreferenceDialog;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ProjectSettingsFragment extends PreferenceFragmentCompat implements ExtendedFragment,
        PreferenceManager.OnPreferenceTreeClickListener,
        Preference.OnPreferenceChangeListener {
    private static final String PREF_ROOT = "pref_menu_root";
    private static final String PREF_GENERAL = "pref_menu_general";
    private static final String PREF_FEATURES = "pref_menu_features";
    private static final String PREF_DATE = "pref_menu_date";
    private static final String PREF_SECURITY = "pref_menu_security";
    private String currentPrefRoot = PREF_ROOT;

    private static final String ARG_ID = "projectID";
    private static final String ARG_ID2 = "ID2";
    private static final String ARG_IS_MEMBER = "isMember";

    private ProjectDatabase projectDatabase;
    private ProjectData project;

    // Mutually exclusive unless the project has both of those enabled
    private MemberData member;
    private RoleData role;

    /** Default constructor. **/
    public ProjectSettingsFragment() {
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
            String projectID = getArguments().getString(ARG_ID),
                    id2 = getArguments().getString(ARG_ID2);
            boolean isMember = getArguments().getBoolean(ARG_IS_MEMBER);
            projectDatabase = Room.databaseBuilder(getActivity(), ProjectDatabase.class,
                    MainActivity.DATABASE_PROJECT)
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                    .allowMainThreadQueries().build();
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
        pManager.setOnPreferenceTreeClickListener(this);
    }

    /** Delegates the preference click listeners to their own functions. **/
    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // No PREF_FEATURES as they have no features available
        switch (currentPrefRoot) {
            case PREF_ROOT:
                rootPreferenceClick(preference);
                break;
            case PREF_GENERAL:
                generalPreferenceClick(preference);
                break;
            case PREF_FEATURES:
                featurePreferenceClick(preference);
                break;
            case PREF_DATE:
                datePreferenceClick(preference);
                break;
            case PREF_SECURITY:
                securityPreferenceClick(preference);
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
        switch(preference.getKey()) {
            case "pref_set_icon":
            case "pref_update_icon":
                // TODO: Set icon
                break;
            case "pref_remove_icon":
                // TODO: Remove icon
                break;
        }
    }

    /** The tree click listeners for the feature preferences. **/
    private void featurePreferenceClick(Preference preference) {
        switch(preference.getKey()) {
            // TODO: Set default icon value
            case "pref_status_icon":
                switch(project.projectStatusIcon) {
                    case R.string.blank:
                        ((ListPreference) preference).setValue("None");
                        break;
                    case R.drawable.status_ic_circle:
                        ((ListPreference) preference).setValue("Circle");
                        break;
                }
                break;
            case "pref_related_subject":
                ((ListPreference) preference).setValue(project.associatedSubject);
                break;
        }
    }

    /** The tree click listeners for the date preferences. **/
    private void datePreferenceClick(Preference preference) {
        switch(preference.getKey()) {
            case "pref_remove_expected_start":
                // TODO: Remove expected start date
                break;
            case "pref_remove_expected_end":
                // TODO: Remove expected end date
                break;
            case "pref_remove_actual_start":
                // TODO: Remove actual start date
                break;
            case "pref_remove_actual_end":
                // TODO: Remove actual end date
                break;
        }
    }

    /** The tree click listeners for the security preferences. **/
    private void securityPreferenceClick(Preference preference) {
        // TODO: Remove password
    }

    /** Delegates the show/hide of preferences to their own functions. **/
    private void displayPreference(@NonNull String key) {
        // No need to customize PREF_ROOT as there is nothing to customize
        switch (key) {
            case PREF_ROOT:
                setPreferencesFromResource(R.xml.p3_preference_list, key);
                break;
            case PREF_GENERAL:
                setPreferencesFromResource(R.xml.p3_general_preference_list, key);
                customizeGeneralPreference();
                break;
            case PREF_FEATURES:
                setPreferencesFromResource(R.xml.p3_features_preference_list, key);
                customizeFeaturePreference();
                break;
            case PREF_DATE:
                setPreferencesFromResource(R.xml.p3_date_preference_list, key);
                customizeDatePreference();
                break;
            case PREF_SECURITY:
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
                    currentPref.setOnPreferenceChangeListener(this);
                }
            }
        }, 0);
    }

    /** Customize the general preferences. **/
    private void customizeGeneralPreference() {
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_update_title")))
                .setText(project.projectTitle);
        ((EditTextPreference) Objects.requireNonNull(findPreference("pref_update_desc")))
                .setText(project.description);
        if (project.hasIcon) {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_icon"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_icon"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_icon"))).setVisible(false);
        }
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_completed"))).setChecked(!project.projectOngoing);
    }

    /** Customize the feature preferences. **/
    private void customizeFeaturePreference() {
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_members"))).setChecked(project.membersEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_roles"))).setChecked(project.rolesEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_tasks"))).setChecked(project.taskEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_status"))).setChecked(project.statusEnabled);
        ((SwitchPreference) Objects.requireNonNull(findPreference("pref_merge_task_status"))).setChecked(project.mergeTaskStatus);
        // Handler for setting related subject as database access is needed
        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                SubjectDatabase database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                        MainActivity.DATABASE_NOTES).allowMainThreadQueries()
                        .addMigrations(NotesSubjectMigration.MIGRATION_1_2).build();
                List<NotesSubject> subjectList = database.SubjectDao().getAll();
                CharSequence[] subjectNameList = new CharSequence[subjectList.size()];
                for (int i = 0; i < subjectList.size(); i++) {
                    subjectNameList[i] = subjectList.get(i).title;
                }
                ListPreference subjectPreference = Objects.requireNonNull(findPreference("pref_related_subject"));
                subjectPreference.setEntries(subjectNameList);
                subjectPreference.setEntryValues(subjectNameList);
                database.close();
            }
        }, 0);
    }

    /** Customize the date preferences. **/
    private void customizeDatePreference() {
        if (project.expectedStartDate == null) {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_expected_start"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_expected_start"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_expected_start"))).setVisible(false);
        }
        if (project.expectedEndDate == null) {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_expected_end"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_expected_end"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_expected_end"))).setVisible(false);
        }
        if (project.actualStartDate == null) {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_actual_start"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_actual_start"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_actual_start"))).setVisible(false);
        }
        if (project.actualEndDate == null) {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_actual_end"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_actual_end"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_actual_end"))).setVisible(false);
        }
    }

    /** Customize the security preferences. **/
    private void customizeSecurityPreference() {
        if (project.projectPass.length() == 0) {
            ((Preference) Objects.requireNonNull(findPreference("pref_update_password"))).setVisible(false);
            ((Preference) Objects.requireNonNull(findPreference("pref_remove_password"))).setVisible(false);
        } else {
            ((Preference) Objects.requireNonNull(findPreference("pref_set_password"))).setVisible(false);
        }
    }

    /** Delegates the value change of preferences to their own functions. **/
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // No need for PREF_ROOT as it is handled in the tree click listeners
        switch (currentPrefRoot) {
            case PREF_GENERAL:
                generalPrefChanged(preference, newValue);
                break;
            case PREF_FEATURES:
                featurePrefChanged(preference, newValue);
                break;
            case PREF_DATE:
                datePrefChanged(preference, newValue);
                break;
            case PREF_SECURITY:
                securityPrefChanged(preference, newValue);
                break;
        }
        displayPreference(currentPrefRoot);
        return false;
    }

    /** Detects the value change of general preferences. **/
    private void generalPrefChanged(Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case "pref_update_title":
                // TODO: Update title
                break;
            case "pref_update_desc":
                // TODO: Update description
                break;
            case "pref_completed":
                // TODO: Update projectCompleted
                break;
        }
    }

    /** Detects the value change of feature preferences. **/
    private void featurePrefChanged(Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case "pref_members":
                // TODO: Update membersEnabled
                break;
            case "pref_roles":
                // TODO: Update rolesEnabled
                break;
            case "pref_tasks":
                // TODO: Update tasksEnabled
                break;
            case "pref_status":
                // TODO: Update statusEnabled
                break;
            case "pref_merge_task_status":
                // TODO: Update mergeTaskStatus
                break;
            case "pref_status_icon":
                // TODO: Update status icon
                break;
            case "pref_related_subject":
                // TODO: Update related subject
                break;
        }
    }

    /** Detects the value change of date preferences. **/
    private void datePrefChanged(Preference preference, Object newValue) {
        switch(preference.getKey()) {
            case "pref_set_expected_start":
            case "pref_update_expected_start":
                // TODO: Set expected start date
                break;
            case "pref_set_expected_end":
            case "pref_update_expected_end":
                // TODO: Set expected end date
                break;
            case "pref_set_actual_start":
            case "pref_update_actual_start":
                // TODO: Set actual start date
                break;
            case "pref_set_actual_end":
            case "pref_update_actual_end":
                // TODO: Set actual end date
                break;
        }
    }

    /** Detects the value change of security preferences. **/
    private void securityPrefChanged(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "pref_set_password":
            case "pref_update_password":
                // TODO: Save project password
                break;
            case "pref_del_project":
                // TODO: Delete project
                break;
        }
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
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(getParentFragmentManager(), "ProjectSettingsFragment.1");
        } else {
            // Let super handle it
            super.onDisplayPreferenceDialog(preference);
        }
    }

    /** Gets the dialog fragments for date preferences. **/
    private DatePreferenceDialog getDatePreferenceDialog(@NonNull DatePreference preference) {
        long currentDate = Calendar.getInstance().getTimeInMillis();
        switch (preference.getKey()) {
            case "pref_set_expected_start":
            case "pref_update_expected_start":
                // Expected start date must be before expected end date
                if (project.expectedEndDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey());
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(),
                            -1, project.expectedEndDate.getTime())
                            .setInitialDate(project.expectedStartDate.getTime());
                }
            case "pref_set_expected_end":
            case "pref_update_expected_end":
                // Expected end date must be after expected start date
                if (project.expectedStartDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey());
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(),
                            project.expectedStartDate.getTime(), -1)
                            .setInitialDate(project.expectedEndDate.getTime());
                }
            case "pref_set_actual_start":
            case "pref_update_actual_start":
                // Actual start date must be in the past and before the actual end date
                if (project.actualEndDate == null) {
                    return DatePreferenceDialog.newInstance(preference.getKey(), -1, currentDate);
                } else {
                    return DatePreferenceDialog.newInstance(preference.getKey(), -1, project.actualEndDate.getTime())
                            .setInitialDate(project.actualEndDate.getTime());
                }
            case "pref_set_actual_end":
            case "pref_update_actual_end":
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
        if (currentPrefRoot.equals(PREF_ROOT)) {
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
            displayPreference(PREF_ROOT);
            currentPrefRoot = PREF_ROOT;
            return true;
        }
    }
}
