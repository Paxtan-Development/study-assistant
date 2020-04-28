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

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Handles the onCreateView functions from ProjectLoginFragment. **/
class ProjectLoginFragmentView {
    private MainActivity activity;
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    /** The constructor for the class as activity and database needs to be passed on. **/
    ProjectLoginFragmentView(MainActivity activity, ProjectDatabase projectDatabase, ProjectData project) {
        this.activity = activity;
        this.projectDatabase = projectDatabase;
        this.project = project;
    }

    /** Sets up the main login system. **/
    void setLogin(@NonNull View returnScroll) {
        TextInputLayout passwordInputLayout = returnScroll.findViewById(R.id.v2_password_input);
        TextInputLayout userInputLayout = returnScroll.findViewById(R.id.v2_username_input);

        // Set up whether to display members or roles
        if (project.membersEnabled) {
            returnScroll.findViewById(R.id.v2_role).setVisibility(View.GONE);
            returnScroll.findViewById(R.id.v2_role_input).setVisibility(View.GONE);
            returnScroll.findViewById(R.id.v2_button_signup).setOnClickListener(view ->
                    activity.displayFragment(ProjectSignupFragment.newInstance(project.projectID)));
            returnScroll.findViewById(R.id.v2_button_login).setOnClickListener(view ->
                    setMemberLogin(userInputLayout, passwordInputLayout));
            displaySignup(returnScroll);
        } else if (project.rolesEnabled) {
            setRoleLogin(returnScroll, passwordInputLayout);
        }
    }

    /** Sets up the member login system. **/
    private void setMemberLogin(@NonNull TextInputLayout userInputLayout,
                                @NonNull TextInputLayout passwordInputLayout) {
        Toast.makeText(activity, R.string.v2_logging_in, Toast.LENGTH_SHORT).show();
        userInputLayout.setErrorEnabled(false);
        passwordInputLayout.setErrorEnabled(false);

        // Check if username exists
        if (userInputLayout.getEditText() != null && passwordInputLayout.getEditText() != null) {
            if (userInputLayout.getEditText().getText().length() == 0) {
                userInputLayout.setErrorEnabled(true);
                userInputLayout.setError(activity.getString(R.string.v_error_username_blank));
            } else {
                MemberData targetMember = projectDatabase.MemberDao()
                        .searchInProjectByUsername(project.projectID,
                                userInputLayout.getEditText().getText().toString());
                if (targetMember == null) {
                    userInputLayout.setErrorEnabled(true);
                    userInputLayout.setError(activity.getString(R.string.v_error_username_password_incorrect));
                } else {
                    checkMemberPass(passwordInputLayout, targetMember);
                }
            }
        }
    }

    /** Check if the password entered for the member is correct. **/
    private void checkMemberPass(@NonNull TextInputLayout passwordInputLayout,
                                 @NonNull MemberData targetMember) {
        // Check if password entered is correct
        String hashedPassword = SecurityFunctions.memberHash(Objects.requireNonNull(
                passwordInputLayout.getEditText()).getText().toString(), targetMember.salt, project.salt);
        if (Objects.equals(hashedPassword, targetMember.memberPass)) {
            projectDatabase.close();
            activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID,
                    targetMember.memberID, true, true));
        } else {
            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(activity.getString(R.string.v_error_username_password_incorrect));
        }
    }

    /** Display the signup button if conditions are met. **/
    private void displaySignup(View returnScroll) {
        if (project.membersEnabled && project.memberSignupEnabled) {
            returnScroll.findViewById(R.id.v2_button_signup).setOnClickListener(view -> {
                projectDatabase.close();
                activity.displayFragment(ProjectSignupFragment.newInstance(project.projectID));
            });
        }
    }

    /** Sets up the role login system. **/
    private void setRoleLogin(@NonNull View returnScroll,
                              TextInputLayout passwordInputLayout) {
        returnScroll.findViewById(R.id.v2_username).setVisibility(View.GONE);
        returnScroll.findViewById(R.id.v2_username_input).setVisibility(View.GONE);
        returnScroll.findViewById(R.id.v2_button_signup).setVisibility(View.GONE);

        // Populate spinner
        Spinner roleSpinner = returnScroll.findViewById(R.id.v2_role_input);
        List<RoleData> roleDataList = projectDatabase.RoleDao().searchByProject(project.projectID);
        ArrayList<String> roleList = new ArrayList<>();
        for (RoleData role : roleDataList) roleList.add(role.roleName);
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_dropdown_item, roleList);
        roleSpinner.setAdapter(roleAdapter);

        // Login through role
        returnScroll.findViewById(R.id.v2_button_login).setOnClickListener(view ->
                roleLogin(passwordInputLayout, roleSpinner, roleDataList));
    }

    /** Attempt to log in as the specific role with the password given. **/
    private void roleLogin(@NonNull TextInputLayout passwordInputLayout,
                           Spinner roleSpinner, List<RoleData> roleDataList) {
        Toast.makeText(activity, R.string.v2_logging_in, Toast.LENGTH_SHORT).show();
        passwordInputLayout.setErrorEnabled(false);
        if (passwordInputLayout.getEditText() != null) {
            String inputPassword = passwordInputLayout.getEditText().getText().toString();
            RoleData roleSelected = roleDataList.get(roleSpinner.getSelectedItemPosition());
            if (inputPassword.length() == 0 && roleSelected.rolePass.length() == 0) {
                // Role doesn't have a password
                projectDatabase.close();
                activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, roleSelected.roleID,
                        false, true));
            } else if (inputPassword.length() >= 8) {
                checkRolePass(passwordInputLayout, inputPassword, roleSelected);
            } else {
                passwordInputLayout.setErrorEnabled(true);
                passwordInputLayout.setError(activity.getString(R.string.error_password_short));
            }
        }
    }

    /** Check if the password entered for the role is correct. **/
    private void checkRolePass(TextInputLayout passwordInputLayout,
                               String inputPassword, @NonNull RoleData roleSelected) {
        String hashedPassword = SecurityFunctions.roleHash(
                inputPassword, roleSelected.salt);
        if (Objects.equals(hashedPassword, roleSelected.rolePass)) {
            projectDatabase.close();
            activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, roleSelected.roleID,
                    false, true));
        } else {
            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(activity.getString(R.string.error_password_incorrect));
        }
    }
}
