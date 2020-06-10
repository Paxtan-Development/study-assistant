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

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.DataFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.io.File;
import java.util.Objects;

public class ProjectLoginFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_ID = "projectID";
    private ProjectLoginFragmentView loginFragmentView;
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    /** Default constructor. **/
    public ProjectLoginFragment() {
        // Default constructor.
    }

    /** Constructor used when attempting to access the project. **/
    @NonNull
    public static ProjectLoginFragment newInstance(String projectID) {
        ProjectLoginFragment fragment = new ProjectLoginFragment();
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
            loginProject((MainActivity) requireActivity());
        }
    }

    /** Attempt to login to the project. **/
    private void loginProject(MainActivity activity) {
        if (project.projectProtected || project.membersEnabled || project.rolesEnabled) {
            activity.setTitle(R.string.v2_project_login);
            if (project.projectProtected) {
                setPasswordLayout();
            }
        } else {
            // All users will be logged in as admin if roles & members are not enabled
            // and the project is not password-protected
            projectDatabase.close();
            activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, "admin", false, true));
        }
    }

    /** Sets up and displays the layout for the project password dialog. **/
    private void setPasswordLayout() {
        @SuppressLint("InflateParams") TextInputLayout passwordLayout =
                (TextInputLayout) getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (passwordLayout.getEditText() != null) {
            passwordLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordLayout.setHint(getString(R.string.v1_project_protected_password));
        setPasswordDialog(passwordLayout);
    }

    /** Set up and display the project password dialog based on the given layout. **/
    private void setPasswordDialog(TextInputLayout passwordLayout) {
        AlertDialog passwordDialog = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.v1_project_protected).setView(passwordLayout).create();
        passwordDialog.setCancelable(false);
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(passwordDialog);
        dismissibleFragment.setPositiveButton(getString(android.R.string.ok), view -> {
            // Check if password is correct, display error message if not
            if (Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().length() >= 8) {
                checkProjectPass(dismissibleFragment, (MainActivity) requireActivity(), passwordLayout);
            } else {
                passwordLayout.setError(getString(R.string.error_password_short));
            }
        });
        dismissibleFragment.setNegativeButton(getString(android.R.string.cancel), view -> {
            // Returns to ProjectSelectFragment
            dismissibleFragment.dismiss();
            projectDatabase.close();
            ((MainActivity) requireActivity()).displayFragment(new ProjectSelectFragment());
        });
        dismissibleFragment.show(getParentFragmentManager(), "ProjectLoginFragment.1");
    }

    /** Check whether the password for the project is correct. **/
    private void checkProjectPass(DismissibleDialogFragment dismissibleFragment, MainActivity activity,
                                  @NonNull TextInputLayout passwordLayout) {
        if (Objects.equals(SecurityFunctions.passwordHash(Objects.requireNonNull(
                passwordLayout.getEditText()).getText().toString(), project.salt), project.projectPass)) {
            // Password is correct
            dismissibleFragment.dismiss();
            if (!project.membersEnabled && !project.rolesEnabled) {
                // No secondary check
                projectDatabase.close();
                activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, "admin",
                        false, true));
            }
        } else {
            passwordLayout.setError(getString(
                    R.string.error_password_incorrect));
        }
    }

    /** Sets up the layout for the login fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnScroll = inflater.inflate(R.layout.fragment_project_login, container, false);
        ((TextView) returnScroll.findViewById(R.id.v2_title)).setText(project.projectTitle);
        // Set up icon (only for portrait)
        String iconPath = DatabaseFunctions.getProjectIconPath(requireActivity(), project.projectID);
        ImageView projectIcon = returnScroll.findViewById(R.id.v2_icon);
        if (project.hasIcon && new File(iconPath).exists()) {
            projectIcon.setImageURI(Uri.fromFile(new File(iconPath)));
        }
        loginFragmentView = new ProjectLoginFragmentView((MainActivity) requireActivity(), projectDatabase, project);
        loginFragmentView.setLogin(returnScroll);
        return returnScroll;
    }

    /** Return to
     * @see ProjectSelectFragment **/
    @Override
    public boolean onBackPressed() {
        projectDatabase.close();
        ((MainActivity) requireActivity()).displayFragment(new ProjectSelectFragment());
        return true;
    }

    /** Closes the database if the fragment is paused. **/
    @Override
    public void onPause() {
        super.onPause();
        projectDatabase.close();
        if (loginFragmentView != null) {
            loginFragmentView.close();
        }
    }

    /** Reopens the database when the fragment is resumed. **/
    @Override
    public void onResume() {
        super.onResume();
        if (!projectDatabase.isOpen()) {
            projectDatabase = DatabaseFunctions.getProjectDatabase(requireActivity());
        }
        if (loginFragmentView != null) {
            loginFragmentView.open(projectDatabase);
        }
    }
}
