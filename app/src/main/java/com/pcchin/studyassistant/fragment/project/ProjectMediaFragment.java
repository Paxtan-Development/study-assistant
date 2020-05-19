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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.util.ArrayList;

public class ProjectMediaFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_ID = "projectID";
    private static final String ARG_ID2 = "ID2";
    private static final String ARG_IS_MEMBER = "isMember";

    private ProjectDatabase projectDatabase;
    private ProjectData project;
    private String id2;
    private boolean isMember;

    private ArrayList<String> itemsSelected;

    // Mutually exclusive unless the project has both of those enabled
    private MemberData member;
    private RoleData role;

    /** Default constructor. **/
    public ProjectMediaFragment() {
        // Default constructor.
    }

    /** Used in all instances when creating new project.
     * @param ID2 can be either the role ID or member ID depending on the project.
     * @param isMember determines whether ID2 is a member ID or a role ID. **/
    @NonNull
    public static ProjectMediaFragment newInstance(String projectID, String ID2, boolean isMember) {
        ProjectMediaFragment fragment = new ProjectMediaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_ID2, ID2);
        args.putBoolean(ARG_IS_MEMBER, isMember);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the project media. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            String projectID = getArguments().getString(ARG_ID);
            id2 = getArguments().getString(ARG_ID2);
            isMember = getArguments().getBoolean(ARG_IS_MEMBER);
            projectDatabase = DatabaseFunctions.getProjectDatabase(requireActivity());
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

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_p4, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Sets up the layout nad displays the project media. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO: Populate view
        return inflater.inflate(R.layout.blank_list, container, false);
    }

    /** Closes the database if the fragment is paused. **/
    @Override
    public void onPause() {
        super.onPause();
        projectDatabase.close();
    }

    /** Reopens the database when the fragment is resumed. **/
    @Override
    public void onResume() {
        super.onResume();
        if (!projectDatabase.isOpen()) {
            projectDatabase = DatabaseFunctions.getProjectDatabase(requireActivity());
        }
    }

    /** Returns to
     * @see ProjectInfoFragment **/
    @Override
    public boolean onBackPressed() {
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
    }
}
