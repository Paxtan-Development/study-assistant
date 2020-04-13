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

package com.pcchin.studyassistant.fragment.project.member;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pcchin.studyassistant.R;

public class ProjectMemberFragment extends Fragment {
    private static final String ARG_ID = "projectID";
    private static final String ARG_MEMBER_ID = "memberID";
    private static final String ARG_TARGET_MEMBER_ID = "targetMemberID";
    private static final String ARG_UPDATE_NAV_VIEW = "updateNavView";

    /** Default constructor. **/
    public ProjectMemberFragment() {
    }

    /** Used in all instances when accessing a user's profile.
     * @param memberID the member ID of the project.
     * @param targetMemberID the member ID that is being accessed by the member.
     * @param updateNavView determines whether the navigation view will be updated. **/
    public static ProjectMemberFragment newInstance(String projectID, String memberID,
                                                    String targetMemberID, boolean updateNavView) {
        ProjectMemberFragment fragment = new ProjectMemberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_MEMBER_ID, memberID);
        args.putString(ARG_TARGET_MEMBER_ID, targetMemberID);
        args.putBoolean(ARG_UPDATE_NAV_VIEW, updateNavView);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the member's info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /** Sets up the layout for displaying the user info. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_member, container, false);
    }

}
