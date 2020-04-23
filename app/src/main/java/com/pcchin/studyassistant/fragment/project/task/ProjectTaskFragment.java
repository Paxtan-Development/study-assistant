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

package com.pcchin.studyassistant.fragment.project.task;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pcchin.studyassistant.R;

public class ProjectTaskFragment extends Fragment {
    private static final String ARG_ID = "projectID",
            ARG_ID2 = "ID2",
            ARG_IS_MEMBER = "isMember",
            ARG_UPDATE_NAV_VIEW = "updateNavView";

    /** Default constructor. **/
    public ProjectTaskFragment() {
    }

    /** Used in all instances when creating new project.
     * @param ID2 can be either the role ID or member ID depending on the project.
     * @param isMember determines whether ID2 is a member ID or a role ID. If ID2 is none.
     * @param updateNavView determines whether the navigation view will be updated. **/
    public static ProjectTaskFragment newInstance(String projectID, String ID2, boolean isMember,
                                                  boolean updateNavView) {
        ProjectTaskFragment fragment = new ProjectTaskFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_ID2, ID2);
        args.putBoolean(ARG_IS_MEMBER, isMember);
        args.putBoolean(ARG_UPDATE_NAV_VIEW, updateNavView);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the tasks' info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /** Sets up the layout for displaying all the tasks. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_task, container, false);
    }

}
