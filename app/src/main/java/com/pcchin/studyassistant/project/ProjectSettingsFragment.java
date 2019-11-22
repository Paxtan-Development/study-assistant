/*
 * Copyright 2019 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.project;


import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;
import androidx.room.Room;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.display.ExtendedFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.project.preference.CheckPasswordPreference;
import com.pcchin.studyassistant.project.preference.ImagePreference;
import com.pcchin.studyassistant.project.preference.PasswordPreference;

public class ProjectSettingsFragment extends PreferenceFragmentCompat implements ExtendedFragment {
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
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4)
                    .allowMainThreadQueries().build();
            project = projectDatabase.ProjectDao().searchByID(projectID);

            Object[] idValidity = GeneralFunctions.checkIdValidity(getActivity(), projectDatabase,
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
        // Get ALL the possible preferences from the preference manager
        EditTextPreference
                pUpdateTitle = pManager.findPreference("pref_update_title"),
                pUpdateDesc = pManager.findPreference("pref_update_desc");
        SwitchPreference
                pCompleted = pManager.findPreference("pref_completed"),
                pMembers = pManager.findPreference("pref_members"),
                pRoles = pManager.findPreference("pref_roles"),
                pTasks = pManager.findPreference("pref_tasks"),
                pStatus = pManager.findPreference("pref_status"),
                pMergeTaskStatus = pManager.findPreference("pref_merge_task_status");
        ListPreference
                pStatusIcon = pManager.findPreference("pref_status_icon"),
                pRelatedSubject = pManager.findPreference("pref_related_subject");
        ImagePreference
                pSetIcon = pManager.findPreference("pref_set_icon"),
                pUpdateIcon = pManager.findPreference("pref_update_icon");
        PasswordPreference pSetPassword = pManager.findPreference("pref_set_password");
        CheckPasswordPreference pUpdatePassword = pManager.findPreference("pref_update_password");
        Preference pRemoveIcon = pManager.findPreference("pref_remove_icon"),
                pRemovePassword = pManager.findPreference("pref_remove_password"),
                pRemoveExpectedStart = pManager.findPreference("pref_remove_expected_start"),
                pRemoveExpectedEnd = pManager.findPreference("pref_remove_expected_end"),
                pRemoveActualStart = pManager.findPreference("pref_remove_actual_start"),
                pRemoveActualEnd = pManager.findPreference("pref_remove_actual_end"),
                pDeleteProject = pManager.findPreference("pref_del_project");
        // TODO: Customize settings
    }

    /** Returns to
     * @see ProjectInfoFragment **/
    @Override
    public boolean onBackPressed() {
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
    }
}
