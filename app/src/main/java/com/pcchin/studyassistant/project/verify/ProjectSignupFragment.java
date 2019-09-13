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

package com.pcchin.studyassistant.project.verify;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;
import com.pcchin.studyassistant.misc.RandomString;
import com.pcchin.studyassistant.project.ProjectInfoFragment;
import com.pcchin.studyassistant.project.ProjectSelectFragment;

import java.util.Objects;

public class ProjectSignupFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_ID = "projectID";
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    /** Default constructor. **/
    public ProjectSignupFragment() {
    }

    /** Constructor used when signing up for a new profile. **/
    public static ProjectSignupFragment newInstance(String projectID) {
        ProjectSignupFragment fragment = new ProjectSignupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the project info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            projectDatabase = Room.databaseBuilder(getActivity(),
                    ProjectDatabase.class, MainActivity.DATABASE_PROJECT)
                    .fallbackToDestructiveMigrationFrom(1)
                    .allowMainThreadQueries().build();
            if (getArguments() != null) {
                project = projectDatabase.ProjectDao().searchByID(getArguments().getString(ARG_ID));
            }
            if (project == null) {
                // Go back to project selection
                Toast.makeText(getActivity(), R.string.p_error_project_not_found,
                        Toast.LENGTH_SHORT).show();
                projectDatabase.close();
                ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            } else {
                getActivity().setTitle(R.string.v1_signup);
            }
        }
    }

    /** Sets up the layout for the sign up info. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnView = inflater.inflate(R.layout.fragment_project_signup, container, false);
        ((TextView) returnView.findViewById(R.id.v1_title)).setText(project.projectID);

        TextInputLayout usernameInput = returnView.findViewById(R.id.v1_username_input),
                passwordInput1 = returnView.findViewById(R.id.v1_password1_input),
                passwordInput2 = returnView.findViewById(R.id.v1_password2_input),
                fullnameInput = returnView.findViewById(R.id.v1_fullname_input);

        // Set up listeners
        if (getActivity() != null) {
            returnView.findViewById(R.id.v1_return).setOnClickListener(view -> {
                projectDatabase.close();
                ((MainActivity) getActivity()).displayFragment(
                        ProjectLoginFragment.newInstance(project.projectID));
            });
            returnView.findViewById(R.id.v1_signup).setOnClickListener(view -> {
                // Get all inputs
                usernameInput.setErrorEnabled(false);
                passwordInput1.setErrorEnabled(false);
                passwordInput2.setErrorEnabled(false);
                if (usernameInput.getEditText() != null && passwordInput1.getEditText() != null
                        && passwordInput2.getEditText() != null && fullnameInput.getEditText() != null) {
                    // Check username
                    String usernameText = usernameInput.getEditText().getText().toString(),
                            passwordText1 = passwordInput1.getEditText().getText().toString(),
                            passwordText2 = passwordInput2.getEditText().getText().toString(),
                            fullName = fullnameInput.getEditText().getText().toString();
                    if (usernameText.length() == 0) {
                        // Username is blank
                        usernameInput.setErrorEnabled(true);
                        usernameInput.setError(getString(R.string.v_error_username_blank));
                    } else if (usernameText.replaceAll("\\s+", "").length()
                            != usernameText.length()) {
                        // Username contains whitespace
                        usernameInput.setErrorEnabled(true);
                        usernameInput.setError(getString(R.string.v_error_username_whitespace));
                    } else if (projectDatabase.MemberDao().searchInProjectByUsername
                            (project.projectID, usernameText) == null) {
                        // Username has been taken
                        usernameInput.setErrorEnabled(true);
                        usernameInput.setError(getString(R.string.v_error_username_taken));
                    } else if (passwordText1.length() == 0) {
                        // Password is blank
                        passwordInput1.setErrorEnabled(true);
                        passwordInput1.setError(getString(R.string.v_error_password_blank));
                    } else if (passwordText1.length() < 8) {
                        // Password is too short
                        passwordInput1.setErrorEnabled(true);
                        passwordInput1.setError(getString(R.string.error_password_short));
                    } else if (!Objects.equals(passwordText1, passwordText2)) {
                        // Checks if both passwords are the same
                        passwordInput2.setErrorEnabled(true);
                        passwordInput2.setError(getString(R.string.error_password_unequal));
                    } else {
                        // Generate member ID
                        RandomString randomID = new RandomString(48);
                        String memberID = randomID.nextString();
                        while (projectDatabase.MemberDao().searchByID(memberID) != null) {
                            memberID = randomID.nextString();
                        }
                        // Generate salt
                        String salt = new RandomString(40).nextString(),
                            hashedPass = SecurityFunctions.memberHash(passwordText1, salt, project.salt);
                        projectDatabase.MemberDao().insert(new MemberData(memberID,
                                project.projectID, usernameText, fullName, salt,
                                hashedPass, project.memberDefaultRole));
                        projectDatabase.close();
                        Toast.makeText(getActivity(), R.string.v1_member_created, Toast.LENGTH_SHORT).show();
                        ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                                .newInstance(project.projectID, memberID, true, true));
                    }
                }
            });
        }
        return returnView;
    }

    /** Return to
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
