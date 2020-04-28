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
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.activity.MainActivity;

import java.io.File;
import java.util.Objects;

public class ProjectLoginFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_ID = "projectID";
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
        if (getActivity() != null) {
            projectDatabase = GeneralFunctions.getProjectDatabase(getActivity());
            if (getArguments() != null) {
                project = projectDatabase.ProjectDao().searchByID(getArguments().getString(ARG_ID));
            }
            if (project == null) {
                // Go back to project selection
                Toast.makeText(getActivity(), R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
                projectDatabase.close();
                if (getActivity() != null) {
                    // If statement added to prevent NullPointerException
                    ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
                }
            } else {
                loginProject((MainActivity) getActivity());
            }
        }
    }

    /** Attempt to login to the project. **/
    private void loginProject(MainActivity activity) {
        if (project.projectProtected || project.membersEnabled || project.rolesEnabled) {
            activity.setTitle(R.string.v2_project_login);
            if (project.projectProtected) {
                setPasswordDialog();
            }
        } else {
            // All users will be logged in as admin if roles & members are not enabled
            // and the project is not password-protected
            projectDatabase.close();
            activity.displayFragment(ProjectInfoFragment.newInstance(project.projectID, "admin", false, true));
        }
    }

    /** Sets up and displays the project password dialog. **/
    private void setPasswordDialog() {
        // Set up password input layout
        @SuppressLint("InflateParams") TextInputLayout passwordLayout =
                (TextInputLayout) getLayoutInflater().inflate(R.layout.popup_edittext, null);
        if (passwordLayout.getEditText() != null) {
            passwordLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        passwordLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
        passwordLayout.setHint(getString(R.string.v1_project_protected_password));

        // Set up OnShowListener
        DialogInterface.OnShowListener passwordDialogListener = dialogInterface ->
                setPasswordDialogListener((MainActivity) getActivity(), (AlertDialog) dialogInterface, passwordLayout);
        AutoDismissDialog passwordDialog = new AutoDismissDialog(
                getString(R.string.v1_project_protected), passwordLayout, passwordDialogListener);
        passwordDialog.setCancellable(false);
        passwordDialog.show(getParentFragmentManager(), "ProjectLoginFragment.1");
    }

    /** Sets the onClickListeners for the project password dialog. **/
    private void setPasswordDialogListener(MainActivity activity, @NonNull AlertDialog dialogInterface,
                                           TextInputLayout passwordLayout) {
        dialogInterface.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            // Check if password is correct
            if (Objects.requireNonNull(passwordLayout.getEditText()).getText().toString().length() >= 8) {
                checkProjectPass(dialogInterface,
                        (MainActivity) getActivity(), passwordLayout);
            } else {
                // Display error message
                passwordLayout.setError(getString(R.string.error_password_short));
            }
        });
        dialogInterface.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
            // Returns to ProjectSelectFragment
            dialogInterface.dismiss();
            projectDatabase.close();
            activity.displayFragment(new ProjectSelectFragment());
        });
    }

    /** Check whether the password for the project is correct. **/
    private void checkProjectPass(DialogInterface dialogInterface, MainActivity activity,
                                  @NonNull TextInputLayout passwordLayout) {
        if (Objects.equals(SecurityFunctions.projectHash(Objects.requireNonNull(
                passwordLayout.getEditText()).getText().toString(), project.salt), project.projectPass)) {
            // Password is correct
            dialogInterface.dismiss();
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
        if (getActivity() != null) {
            String iconPath = getActivity().getFilesDir().getAbsolutePath() + "/icons/project/"
                    + project.projectID + ".jpg";
            ImageView projectIcon = returnScroll.findViewById(R.id.v2_icon);
            if (project.hasIcon && new File(iconPath).exists()) {
                projectIcon.setImageURI(Uri.fromFile(new File(iconPath)));
            }
        }
        new ProjectLoginFragmentView((MainActivity) getActivity(), projectDatabase, project).setLogin(returnScroll);
        return returnScroll;
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
