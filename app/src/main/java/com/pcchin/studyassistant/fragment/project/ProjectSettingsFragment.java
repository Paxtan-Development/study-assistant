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

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.ui.MainActivity;

public class ProjectSettingsFragment extends PreferenceFragmentCompat implements ExtendedFragment, PreferenceManager.OnPreferenceTreeClickListener {
    private static final String PREF_ROOT = "pref_menu_root";
    private static final String PREF_GENERAL = "pref_menu_general";
    private static final String PREF_FEATURES = "pref_menu_features";
    private static final String PREF_DATE = "pref_menu_date";
    private static final String PREF_SECURITY = "pref_menu_security";
    private String currentPreference = PREF_ROOT;

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

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (currentPreference) {
            case PREF_ROOT:
                rootPreferenceClick(preference);
                break;
            case PREF_GENERAL:
                generalPreferenceClick(preference);
                break;
            case PREF_FEATURES:
                featuresPreferenceClick(preference);
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
        getPreferenceManager().getPreferenceScreen().removeAll();
        currentPreference = preference.getKey();
        switch (preference.getKey()) {
            case PREF_GENERAL:
                addPreferencesFromResource(R.xml.p3_general_preference_list);
                break;
            case PREF_FEATURES:
                addPreferencesFromResource(R.xml.p3_features_preference_list);
                break;
            case PREF_DATE:
                addPreferencesFromResource(R.xml.p3_date_preference_list);
                break;
            case PREF_SECURITY:
                addPreferencesFromResource(R.xml.p3_security_preference_list);
                break;
        }
    }

    /** The tree click listeners for the general preferences. **/
    private void generalPreferenceClick(Preference preference) {
        // TODO: Complete
    }

    /** The tree click listeners for the features preferences. **/
    private void featuresPreferenceClick(Preference preference) {
        // TODO: Complete
    }

    /** The tree click listeners for the date preferences. **/
    private void datePreferenceClick(Preference preference) {
        // TODO: Complete
    }

    /** The tree click listeners for the security preferences. **/
    private void securityPreferenceClick(Preference preference) {
        // TODO: Complete
    }

    /** Returns to
     * @see ProjectInfoFragment **/
    @Override
    public boolean onBackPressed() {
        if (currentPreference.equals(PREF_ROOT)) {
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
            getPreferenceManager().getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.p3_preference_list);
            currentPreference = PREF_ROOT;
            return true;
        }
    }
}
