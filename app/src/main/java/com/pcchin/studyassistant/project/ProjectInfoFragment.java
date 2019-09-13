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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;

public class ProjectInfoFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_ID = "projectID";
    private static final String ARG_ID2 = "ID2";
    private static final String ARG_IS_MEMBER = "isMember";
    private static final String ARG_UPDATE_NAV_VIEW = "updateNavView";
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    // Mutually exclusive unless the project has both of those enabled
    private MemberData member;
    private RoleData role;

    /** Default constructor. **/
    public ProjectInfoFragment() {
    }

    /** Used in all instances when creating new project.
     * @param ID2 can be either the role ID or member ID depending on the project.
     * @param isMember determines whether ID2 is a member ID or a role ID. If ID2 is none.
     * @param updateNavView determines whether the navigation view will be displayed. **/
    public static ProjectInfoFragment newInstance(String projectID, String ID2, boolean isMember,
                                                  boolean updateNavView) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_ID2, ID2);
        args.putBoolean(ARG_IS_MEMBER, isMember);
        args.putBoolean(ARG_UPDATE_NAV_VIEW, updateNavView);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the project info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && getArguments() != null) {
            String projectID = getArguments().getString(ARG_ID), id2 = getArguments().getString(ARG_ID2);
            boolean isMember = getArguments().getBoolean(ARG_IS_MEMBER),
                    updateNavView = getArguments().getBoolean(ARG_UPDATE_NAV_VIEW);
            boolean hasError = false;

            projectDatabase = Room.databaseBuilder(getActivity(), ProjectDatabase.class,
                    MainActivity.DATABASE_PROJECT)
                    .fallbackToDestructiveMigrationFrom(1)
                    .allowMainThreadQueries().build();
            project = projectDatabase.ProjectDao().searchByID(projectID);
            if (project == null) {
                // Project is somehow missing
                Toast.makeText(getActivity(), R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
                hasError = true;
            } else if (isMember) {
                member = projectDatabase.MemberDao().searchByID(id2);
                if (member == null) {
                    // Member is somehow missing
                    Toast.makeText(getActivity(), R.string.p_error_member_not_found, Toast.LENGTH_SHORT).show();
                    hasError = true;
                } else if (project.rolesEnabled) {
                    // Get the associated role if needed
                    role = projectDatabase.RoleDao().searchByID(member.role);
                    if (role == null) {
                        // Role is somehow missing
                        Toast.makeText(getActivity(), R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                        hasError = true;
                    }
                }
            } else {
                // We can safely assume that members are disabled
                // Get the associated role if needed
                role = projectDatabase.RoleDao().searchByID(id2);
                if (role == null) {
                    // Role is somehow missing
                    Toast.makeText(getActivity(), R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                    hasError = true;
                }
            }

            if (hasError) {
                // Return to ProjectSelectFragment (Same as onBackPressed) if any error is found
                onBackPressed();
            } else if (updateNavView) {
                // Set up navigation menu for members and roles respectively only if requested
                if (isMember) {
                    GeneralFunctions.updateBottomNavView((MainActivity) getActivity(),
                            R.menu.menu_p_bottom, project, member, null);
                } else {
                    GeneralFunctions.updateBottomNavView((MainActivity) getActivity(),
                            R.menu.menu_p_bottom, project, null, role);
                }
            }
        } else {
            // getActivity() is somehow null, returns to previous fragment.
            onBackPressed();
        }
        setHasOptionsMenu(true);
    }

    /** Sets up the layout for the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnView;
        if (project.displayedInfo == ProjectData.DISPLAYED_NONE) {
            returnView = inflater.inflate(R.layout.fragment_project_info_notable, container, false);
        } else {
            returnView = inflater.inflate(R.layout.fragment_project_info, container, false);
        }
        // TODO: Set up layout
        return returnView;
    }

    /** Returns to
     * @see ProjectSelectFragment **/
    @Override
    public boolean onBackPressed() {
        projectDatabase.close();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            return true;
        }
        return false;
    }
}
