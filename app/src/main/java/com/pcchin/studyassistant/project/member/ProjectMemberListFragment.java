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

package com.pcchin.studyassistant.project.member;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pcchin.studyassistant.R;

public class ProjectMemberListFragment extends Fragment {
    private static final String ARG_ID = "projectID";
    private static final String ARG_MEMBER_ID = "memberID";
    private static final String ARG_UPDATE_NAV_VIEW = "updateNavView";

    /** Default constructor. **/
    public ProjectMemberListFragment() {
    }

    /** Used in all instances when accessing a user's profile.
     * @param memberID the member ID of the project.
     * @param updateNavView determines whether the navigation view will be updated. **/
    public static ProjectMemberListFragment newInstance(String projectID, String memberID,
                                                    boolean updateNavView) {
        ProjectMemberListFragment fragment = new ProjectMemberListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_MEMBER_ID, memberID);
        args.putBoolean(ARG_UPDATE_NAV_VIEW, updateNavView);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the members' info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /** Sets up the layout for displaying all the users. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_member_list, container, false);
    }

}
