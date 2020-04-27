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

package com.pcchin.studyassistant.fragment.project.create;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.NavViewFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.ui.MainActivity;
import com.pcchin.studyassistant.utils.misc.RandomString;

public class ProjectCreateFragment extends Fragment implements ExtendedFragment {
    // Variables used in generateValidString
    public static final int TYPE_PROJECT = 0;
    public static final int TYPE_ROLE = 1;
    public static final int TYPE_MEMBER = 2;

    private ProjectDatabase projectDatabase;
    private boolean enableMembers = true, enableRoles = true,
            customAdmin = false, customMember = false;

    /** Default constructor. **/
    public ProjectCreateFragment() {
        // Default constructor.
    }

    /** Initializes the fragments and the project database. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            projectDatabase = GeneralFunctions.getProjectDatabase(getActivity());
        } else {
            onBackPressed();
        }
    }

    /** Sets up the layout for the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnView = inflater.inflate(R.layout.fragment_project_create, container, false);
        // Returns to ProjectSelectFragment
        returnView.findViewById(R.id.p6_return).setOnClickListener(view -> onBackPressed());
        initEnableMembers(returnView);
        initEnableRoles(returnView);
        initCustomAdmin(returnView);
        initCustomMember(returnView);

        // Creates project if conditions are met
        returnView.findViewById(R.id.p6_create).setOnClickListener(view -> {
            boolean allInputCorrect = true;
            ProjectCreateFragmentCheck check = new ProjectCreateFragmentCheck(getContext());

            // Check for all requirements
            TextInputLayout projectName = returnView.findViewById(R.id.p6_name_input),
                    projectPass1 = returnView.findViewById(R.id.p6_project_password1_input),
                    projectPass2 = returnView.findViewById(R.id.p6_project_password2_input),
                    memberName = returnView.findViewById(R.id.p6_first_member_name_input),
                    memberPass1 = returnView.findViewById(R.id.p6_first_member_password1_input),
                    memberPass2 = returnView.findViewById(R.id.p6_first_member_password2_input),
                    customAdminName = returnView.findViewById(R.id.p6_admin_name_input),
                    customAdminPass1 = returnView.findViewById(R.id.p6_admin_password1_input),
                    customAdminPass2 = returnView.findViewById(R.id.p6_admin_password2_input),
                    customMemberName = returnView.findViewById(R.id.p6_member_name_input),
                    customMemberPass1 = returnView.findViewById(R.id.p6_member_password1_input),
                    customMemberPass2 = returnView.findViewById(R.id.p6_member_password2_input);
            if (!check.checkProjectValues(projectName, projectPass1, projectPass2, projectDatabase)) allInputCorrect = false;
            if (enableMembers && !check.checkMemberValues(memberName, memberPass1, memberPass2)) allInputCorrect = false;

            if (enableRoles) {
                if (customAdmin && !check.checkCustomAdmin(customAdminName, customAdminPass1,
                        customAdminPass2)) allInputCorrect = false;
                if (customMember && !check.checkCustomMember(customMemberName, customMemberPass1,
                        customMemberPass2)) allInputCorrect = false;
            }

            if (allInputCorrect && projectName.getEditText() != null
                    && projectPass1.getEditText() != null
                    && projectPass2.getEditText() != null
                    && memberName.getEditText() != null
                    && memberPass1.getEditText() != null
                    && memberPass2.getEditText() != null
                    && customAdminName.getEditText() != null
                    && customAdminPass1.getEditText() != null
                    && customAdminPass2.getEditText() != null
                    && customMemberName.getEditText() != null
                    && customMemberPass1.getEditText() != null
                    && customMemberPass2.getEditText() != null) {

                // Creates admin role
                RandomString idRand = new RandomString(48),
                        saltRand = new RandomString(40);
                String projectID = GeneralFunctions.generateValidProjectString(idRand, TYPE_PROJECT,
                        projectDatabase);
                RoleData adminRole = ProjectCreateFragmentCreate.createAdminRole(customAdmin, idRand,
                            saltRand, projectID, customAdminName, customAdminPass1, projectDatabase),
                        memberRole = ProjectCreateFragmentCreate.createMemberRole(customMember, idRand,
                            saltRand, projectID, customMemberName, customMemberPass1, projectDatabase);
                projectDatabase.RoleDao().insert(adminRole);
                projectDatabase.RoleDao().insert(memberRole);

                // Create the project
                String projectSalt = GeneralFunctions.generateValidProjectString(idRand, TYPE_PROJECT,
                        projectDatabase), projectPass;
                if (projectPass1.getEditText().getText().length() == 0) {
                    projectPass = "";
                } else {
                    projectPass = SecurityFunctions.projectHash(projectPass1.getEditText()
                            .getText().toString(), projectSalt);
                }

                if (enableMembers) {
                    MemberData initialMember = ProjectCreateFragmentCreate.createInitialMember(idRand, saltRand,
                            projectID, projectSalt, adminRole.roleID,
                            memberName, memberPass1, projectDatabase);
                    projectDatabase.MemberDao().insert(initialMember);

                    // Different types of project creation for different constructors
                    if (enableRoles) {
                        // Set default role according to user preference
                        Spinner roleSpinner = returnView.findViewById(R.id.p6_default_role_input);
                        RoleData defaultRole =
                                roleSpinner.getSelectedItemPosition() == 0 ? adminRole : memberRole;

                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectName.getEditText().getText().toString(),
                                projectSalt, projectPass, memberRole, adminRole,
                                initialMember, defaultRole));
                    } else {
                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectName.getEditText().getText().toString(),
                                projectSalt, projectPass, memberRole, adminRole,
                                initialMember, adminRole));
                    }
                    projectDatabase.close();

                    // Go to project info
                    Toast.makeText(getActivity(), R.string.p6_project_created, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        NavViewFunctions.updateNavView((MainActivity) getActivity());
                        ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                                .newInstance(projectID, initialMember.memberID,
                                        true, true));
                    }
                } else {
                    // Members are not enabled
                    if (projectPass1.getEditText().getText().length() == 0) {
                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectName.getEditText().getText().toString(), projectSalt,
                                "", enableRoles, adminRole, memberRole));
                    } else {
                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectName.getEditText().getText().toString(), projectSalt,
                                SecurityFunctions.projectHash(
                                        projectPass1.getEditText().getText().toString(), projectSalt),
                                enableRoles, adminRole, memberRole));
                    }
                    projectDatabase.close();
                    // Go to project info
                    Toast.makeText(getActivity(), R.string.p6_project_created, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
                        NavViewFunctions.updateNavView((MainActivity) getActivity());
                        ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                                .newInstance(projectID, adminRole.roleID,
                                        false, true));
                    }
                }
            }
        });
        return returnView;
    }

    /** Initializes the "Enable Members" switch, used in onCreateView. **/
    private void initEnableMembers(@NonNull View returnView) {
        ((Switch) returnView.findViewById(R.id.p6_enable_members))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    enableMembers = b;
                    if (b) {
                        // Members are enabled
                        returnView.findViewById(R.id.p6_first_member).setVisibility(View.VISIBLE);
                        if (enableRoles) {
                            returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Members are disabled
                        LinearLayout memberGrp = returnView.findViewById(R.id.p6_first_member);
                        memberGrp.setVisibility(View.GONE);
                        returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.GONE);

                        // Disable all error dialogs
                        for (int i = 0; i < memberGrp.getChildCount(); i++) {
                            if (memberGrp.getChildAt(i) instanceof TextInputLayout) {
                                ((TextInputLayout) memberGrp.getChildAt(i)).setErrorEnabled(false);
                            }
                        }
                    }
                });
    }

    /** Initializes the "Enable Roles" switch, used in onCreateView. **/
    private void initEnableRoles(@NonNull View returnView) {
        ((Switch) returnView.findViewById(R.id.p6_enable_roles))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    enableRoles = b;
                    if (b) {
                        // Roles are enabled
                        returnView.findViewById(R.id.p6_custom_roles).setVisibility(View.VISIBLE);
                        if (enableMembers) {
                            returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Roles are disabled
                        LinearLayout roleGrp = returnView.findViewById(R.id.p6_custom_roles);
                        roleGrp.setVisibility(View.GONE);
                        returnView.findViewById(R.id.p6_default_role_layout).setVisibility(View.GONE);

                        // Disable all error dialogs in child LinearLayouts
                        for (int i = 0; i < roleGrp.getChildCount(); i++) {
                            if (roleGrp.getChildAt(i) instanceof LinearLayout) {
                                LinearLayout subLayout = (LinearLayout) roleGrp.getChildAt(i);
                                for (int j = 0; j < subLayout.getChildCount(); j++) {
                                    if (subLayout.getChildAt(j) instanceof TextInputLayout) {
                                        ((TextInputLayout) subLayout.getChildAt(j)).setErrorEnabled(false);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    /** Initializes the "Custom Admin" switch, used in onCreateView. **/
    private void initCustomAdmin(@NonNull View returnView) {
        ((CheckBox) returnView.findViewById(R.id.p6_custom_admin_switch))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    customAdmin = b;
                    if (b) {
                        // Custom admin is enabled
                        returnView.findViewById(R.id.p6_custom_admin).setVisibility(View.VISIBLE);
                    } else {
                        // Custom admin is disabled
                        LinearLayout adminView = returnView.findViewById(R.id.p6_custom_admin);
                        adminView.setVisibility(View.GONE);

                        // Disable all error dialogs
                        for (int i = 0; i < adminView.getChildCount(); i++) {
                            if (adminView.getChildAt(i) instanceof TextInputLayout) {
                                ((TextInputLayout) adminView.getChildAt(i)).setErrorEnabled(false);
                            }
                        }
                    }
                });
    }

    /** Initializes the "Custom Member" switch, used in onCreateView. **/
    private void initCustomMember(@NonNull View returnView) {
        ((CheckBox) returnView.findViewById(R.id.p6_custom_member_switch))
                .setOnCheckedChangeListener((compoundButton, b) -> {
                    customMember = b;
                    if (b) {
                        // Custom members enabled
                        returnView.findViewById(R.id.p6_custom_member).setVisibility(View.VISIBLE);
                    } else {
                        // Custom members disabled
                        LinearLayout memberView = returnView.findViewById(R.id.p6_custom_member);
                        memberView.setVisibility(View.GONE);

                        // Disable all error dialogs
                        for (int i = 0; i < memberView.getChildCount(); i++) {
                            if (memberView.getChildAt(i) instanceof TextInputLayout) {
                                ((TextInputLayout) memberView.getChildAt(i)).setErrorEnabled(false);
                            }
                        }
                    }
                });
    }

    /** Returns to
     * @see ProjectSelectFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            projectDatabase.close();
            ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            return true;
        }
        return false;
    }

}
