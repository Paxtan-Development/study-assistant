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

package com.pcchin.studyassistant.fragment.project.verify;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.DataFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.utils.misc.RandomString;

import java.util.Objects;

public class ProjectSignupFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_ID = "projectID";
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    /** Default constructor. **/
    public ProjectSignupFragment() {
        // Default constructor.
    }

    /** Constructor used when signing up for a new profile. **/
    @NonNull
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
        projectDatabase = DatabaseFunctions.getProjectDatabase(requireActivity());
        if (getArguments() != null) {
            project = projectDatabase.ProjectDao().searchByID(getArguments().getString(ARG_ID));
        }
        if (project == null) {
            DataFunctions.onProjectMissing((MainActivity) getActivity(), projectDatabase);
        } else {
            requireActivity().setTitle(R.string.v1_signup);
        }
    }

    /** Sets up the layout for the sign up info. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View returnView = inflater.inflate(R.layout.fragment_project_signup, container, false);
        ((TextView) returnView.findViewById(R.id.v1_title)).setText(project.projectTitle);
        TextInputLayout usernameInput = returnView.findViewById(R.id.v1_username_input),
                passwordInput1 = returnView.findViewById(R.id.v1_password1_input),
                passwordInput2 = returnView.findViewById(R.id.v1_password2_input),
                fullnameInput = returnView.findViewById(R.id.v1_fullname_input);

        // Set up listeners
        returnView.findViewById(R.id.v1_return).setOnClickListener(view -> {
            projectDatabase.close();
            ((MainActivity) requireActivity()).displayFragment(ProjectLoginFragment.newInstance(project.projectID));
        });
        returnView.findViewById(R.id.v1_signup).setOnClickListener(view -> {
            // Get all inputs
            usernameInput.setErrorEnabled(false);
            passwordInput1.setErrorEnabled(false);
            passwordInput2.setErrorEnabled(false);
            checkUsername1(usernameInput, passwordInput1, passwordInput2, fullnameInput);
        });
        return returnView;
    }

    /** 1st part of the function for checking the validity of the username. **/
    private void checkUsername1(@NonNull TextInputLayout usernameInput, @NonNull TextInputLayout passwordInput1,
                                @NonNull TextInputLayout passwordInput2, @NonNull TextInputLayout fullnameInput) {
        String usernameText = Objects.requireNonNull(usernameInput.getEditText()).getText().toString(),
                passwordText1 = Objects.requireNonNull(passwordInput1.getEditText()).getText().toString(),
                passwordText2 = Objects.requireNonNull(passwordInput2.getEditText()).getText().toString(),
                fullName = Objects.requireNonNull(fullnameInput.getEditText()).getText().toString();
        if (usernameText.length() == 0) {
            // Username is blank
            usernameInput.setErrorEnabled(true);
            usernameInput.setError(getString(R.string.v_error_username_blank));
        } else if (usernameText.replaceAll("\\s+", "").length() != usernameText.length()) {
            // Username contains whitespace
            usernameInput.setErrorEnabled(true);
            usernameInput.setError(getString(R.string.v_error_username_whitespace));
        } else if (projectDatabase.MemberDao().searchInProjectByUsername
                (project.projectID, usernameText) != null) {
            // Username has been taken
            usernameInput.setErrorEnabled(true);
            usernameInput.setError(getString(R.string.v_error_username_taken));
        } else checkUsername2(passwordInput1, passwordInput2, passwordText1, passwordText2, usernameText, fullName);
    }

    /** 2nd part of the function for checking the validity of the username. **/
    private void checkUsername2(TextInputLayout passwordInput1, TextInputLayout passwordInput2,
                                String passwordText1, String passwordText2,
                                String usernameText, String fullName) {
        if (!Objects.equals(passwordText1, passwordText2)) {
            // Checks if both passwords are the same
            passwordInput2.setErrorEnabled(true);
            passwordInput2.setError(getString(R.string.error_password_unequal));
        } else if (passwordText1.length() > 0 && passwordText1.length() < 8) {
            // Password is too short
            passwordInput1.setErrorEnabled(true);
            passwordInput1.setError(getString(R.string.error_password_short));
        } else {
            // Generate member ID
            RandomString randomID = new RandomString(48);
            String memberID = randomID.nextString();
            while (projectDatabase.MemberDao().searchByID(memberID) != null) {
                memberID = randomID.nextString();
            }
            createMember((MainActivity) requireActivity(), memberID,
                    usernameText, fullName, passwordText1);
        }
    }

    /** Creates a member based on the given memberID. **/
    private void createMember(MainActivity activity, String memberID, String usernameText,
                              String fullName, @NonNull String passwordText1) {
        // Generate salt
        String salt = new RandomString(40).nextString();
        if (passwordText1.length() == 0) {
            projectDatabase.MemberDao().insert(new MemberData(memberID,
                    project.projectID, usernameText, fullName, salt,
                    "", project.memberDefaultRole));
        } else {
            String hashedPass = SecurityFunctions.passwordHash(passwordText1, salt);
            projectDatabase.MemberDao().insert(new MemberData(memberID,
                    project.projectID, usernameText, fullName, salt,
                    hashedPass, project.memberDefaultRole));
        }
        projectDatabase.close();
        Toast.makeText(activity, R.string.member_created, Toast.LENGTH_SHORT).show();
        activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, memberID,
                true, true));
    }

    /** Return to
     * @see ProjectLoginFragment **/
    @Override
    public boolean onBackPressed() {
        projectDatabase.close();
        ((MainActivity) requireActivity()).displayFragment(ProjectLoginFragment.newInstance(project.projectID));
        return true;
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
}
