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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;

import com.pcchin.studyassistant.R;

public class ProjectSettingsFragment extends PreferenceFragmentCompat {
    private static final String ARG_ID = "projectID";
    private static final String ARG_ID2 = "ID2";
    private static final String ARG_IS_MEMBER = "isMember";

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
    }

    /** Retrieve the settings for the project. **/
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    /** Displays all the settings needed in the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_project_settings, container, false);
    }

}
