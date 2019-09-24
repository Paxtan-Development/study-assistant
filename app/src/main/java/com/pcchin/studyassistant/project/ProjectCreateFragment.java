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

import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;
import com.pcchin.studyassistant.misc.RandomString;

import java.util.Objects;

public class ProjectCreateFragment extends Fragment implements FragmentOnBackPressed {
    // Variables used in generateValidString
    private static final int TYPE_PROJECT = 0;
    private static final int TYPE_ROLE = 1;
    private static final int TYPE_MEMBER = 2;

    private ProjectDatabase projectDatabase;
    private boolean enableMembers = true, enableRoles = true,
            customAdmin = true, customMember = true;

    /** Default constructor. **/
    public ProjectCreateFragment() {

    }

    /** Initializes the fragments and the project database. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            projectDatabase = Room.databaseBuilder(getActivity(), ProjectDatabase.class,
                    MainActivity.DATABASE_PROJECT)
                    .fallbackToDestructiveMigrationFrom(1, 2)
                    .allowMainThreadQueries().build();
        } else {
            onBackPressed();
        }
    }

    /** Sets up the layout for the fragment. **/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnView = inflater.inflate(R.layout.fragment_project_create, container, false);
        // Returns to ProjectSelectFragment
        returnView.findViewById(R.id.p6_return).setOnClickListener(view -> onBackPressed());

        // Show/hide input displays according to switch & checkbox values
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

        // Creates project if conditions are met
        returnView.findViewById(R.id.p6_create).setOnClickListener(view -> {
            boolean allInputCorrect = true;

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

            projectName.setErrorEnabled(false);
            projectPass1.setErrorEnabled(false);
            projectPass2.setErrorEnabled(false);

            if (projectName.getEditText() != null && projectPass1.getEditText() != null &&
                    projectPass2.getEditText() != null) {
                if (projectName.getEditText().getText().toString().replaceAll
                        ("\\s+", "").length() == 0) {
                    // Project name is empty
                    projectName.setErrorEnabled(true);
                    projectName.setError(getString(R.string.p6_error_project_name_empty));
                    allInputCorrect = false;
                } else if (projectDatabase.ProjectDao().searchByTitle(projectName
                        .getEditText().getText().toString()).size() > 0) {
                    // Project name is taken
                    projectName.setErrorEnabled(true);
                    projectName.setError(getString(R.string.p6_error_project_exists));
                    allInputCorrect = false;
                }

                if (!Objects.equals(projectPass1.getEditText().getText().toString(),
                        projectPass2.getEditText().getText().toString())) {
                    // Both passwords are not the same
                    projectPass1.setErrorEnabled(true);
                    projectPass1.setError(getString(R.string.error_password_unequal));
                    allInputCorrect = false;
                } else if (projectPass1.getEditText().getText().length() > 0 &&
                        projectPass1.getEditText().getText().toString().length() < 8) {
                    // Password length is too small
                    projectPass1.setErrorEnabled(true);
                    projectPass1.setError(getString(R.string.error_password_short));
                    allInputCorrect = false;
                }
            }

            if (enableMembers) {
                memberName.setErrorEnabled(false);
                memberPass1.setErrorEnabled(false);
                memberPass2.setErrorEnabled(false);

                if (memberName.getEditText() != null && memberPass1.getEditText() != null &&
                        memberPass2.getEditText() != null) {
                    if (memberName.getEditText().getText().toString().replaceAll
                            ("\\s+", "").length() == 0) {
                        // Project name is empty
                        memberName.setErrorEnabled(true);
                        memberName.setError(getString(R.string.p6_error_member_name_empty));
                        allInputCorrect = false;
                    } else if (memberName.getEditText().getText().toString()
                            .replaceAll("\\s+", "").length()
                            != memberName.getEditText().getText().toString().length()) {
                        // Username contains whitespace
                        memberName.setErrorEnabled(true);
                        memberName.setError(getString(R.string.v_error_username_whitespace));
                    }

                    if (!Objects.equals(memberPass1.getEditText().getText().toString(),
                            memberPass2.getEditText().getText().toString())) {
                        // Both passwords are not the same
                        memberPass1.setErrorEnabled(true);
                        memberPass1.setError(getString(R.string.error_password_unequal));
                        allInputCorrect = false;
                    } else if (memberPass1.getEditText().getText().length() > 0 &&
                            memberPass1.getEditText().getText().toString().length() < 8) {
                        // Password length is too small
                        memberPass1.setErrorEnabled(true);
                        memberPass1.setError(getString(R.string.error_password_short));
                        allInputCorrect = false;
                    }
                }
            }

            if (enableRoles) {
                if (customAdmin) {
                    customAdminName.setErrorEnabled(false);
                    customAdminPass1.setErrorEnabled(false);
                    customAdminPass2.setErrorEnabled(false);

                    if (customAdminName.getEditText() != null && customAdminPass1.getEditText() != null &&
                            customAdminPass2.getEditText() != null) {
                        if (customAdminName.getEditText().getText().toString().replaceAll
                                ("\\s+", "").length() == 0) {
                            // Project name is empty
                            customAdminName.setErrorEnabled(true);
                            customAdminName.setError(getString(R.string.p6_error_member_name_empty));
                            allInputCorrect = false;
                        }

                        if (!Objects.equals(customAdminPass1.getEditText().getText().toString(),
                                customAdminPass2.getEditText().getText().toString())) {
                            // Both passwords are not the same
                            customAdminPass1.setErrorEnabled(true);
                            customAdminPass1.setError(getString(R.string.error_password_unequal));
                            allInputCorrect = false;
                        } else if (customAdminPass1.getEditText().getText().length() > 0 &&
                                customAdminPass1.getEditText().getText().toString().length() < 8) {
                            // Password length is too small
                            customAdminPass1.setErrorEnabled(true);
                            customAdminPass1.setError(getString(R.string.error_password_short));
                            allInputCorrect = false;
                        }
                    }
                }

                if (customMember) {
                    customMemberName.setErrorEnabled(false);
                    customMemberPass1.setErrorEnabled(false);
                    customMemberPass2.setErrorEnabled(false);

                    if (customMemberName.getEditText() != null && customMemberPass1.getEditText() != null &&
                            customMemberPass2.getEditText() != null) {
                        if (customMemberName.getEditText().getText().toString().replaceAll
                                ("\\s+", "").length() == 0) {
                            // Project name is empty
                            customMemberName.setErrorEnabled(true);
                            customMemberName.setError(getString(R.string.p6_error_member_name_empty));
                            allInputCorrect = false;
                        }

                        if (!Objects.equals(customMemberPass1.getEditText().getText().toString(),
                                customMemberPass2.getEditText().getText().toString())) {
                            // Both passwords are not the same
                            customMemberPass1.setErrorEnabled(true);
                            customMemberPass1.setError(getString(R.string.error_password_unequal));
                            allInputCorrect = false;
                        } else if (customMemberPass1.getEditText().getText().length() > 0 &&
                                customMemberPass1.getEditText().getText().toString().length() < 8) {
                            // Password length is too small
                            customMemberPass1.setErrorEnabled(true);
                            customMemberPass1.setError(getString(R.string.error_password_short));
                            allInputCorrect = false;
                        }
                    }
                }
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
                RoleData adminRole, memberRole;
                String projectID = generateValidString(idRand, TYPE_PROJECT);
                if (customAdmin) {
                    if (customAdminPass1.getEditText().getText().length() > 0) {
                        // Admin with password
                        String adminSalt = saltRand.nextString();
                        adminRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                                customAdminName.getEditText().getText().toString(),
                                adminSalt, SecurityFunctions.roleHash(customAdminPass1
                                .getEditText().getText().toString(), adminSalt));
                    } else {
                        adminRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                                customAdminName.getEditText().getText().toString(),
                                saltRand.nextString(), "");
                    }
                } else {
                    adminRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                            "Admin", saltRand.nextString(), "");
                }
                adminRole.canDeleteProject = true;
                adminRole.canModifyInfo = true;
                adminRole.canModifyOtherTask = true;
                adminRole.canModifyOtherUser = true;
                adminRole.canModifyOwnTask = true;
                adminRole.canModifyRole = true;
                adminRole.canSetPassword = true;
                adminRole.canViewOtherUser = true;
                adminRole.canViewRole = true;
                adminRole.canViewTask = true;
                adminRole.canViewMedia = true;
                projectDatabase.RoleDao().insert(adminRole);

                // Creates member role
                if (customMember) {
                    if (customMemberPass1.getEditText().getText().length() > 0) {
                        // Admin with password
                        String memberSalt = saltRand.nextString();
                        memberRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                                customMemberName.getEditText().getText().toString(),
                                memberSalt, SecurityFunctions.roleHash(customMemberPass1
                                .getEditText().getText().toString(), memberSalt));
                    } else {
                        memberRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                                customMemberName.getEditText().getText().toString(),
                                saltRand.nextString(), "");
                    }
                } else {
                    memberRole = new RoleData(generateValidString(idRand, TYPE_ROLE), projectID,
                            "Member", saltRand.nextString(), "");
                }
                projectDatabase.RoleDao().insert(memberRole);

                // Create the project
                String projectSalt = generateValidString(idRand, TYPE_PROJECT), projectPass;
                if (projectPass1.getEditText().getText().length() == 0) {
                    projectPass = "";
                } else {
                    projectPass = SecurityFunctions.projectHash(projectPass1.getEditText()
                            .getText().toString(), projectSalt);
                }

                if (enableMembers) {
                    MemberData initialMember;
                    String memberSalt = saltRand.nextString();
                    if (memberPass1.getEditText().getText().length() > 0) {
                        initialMember = new MemberData(generateValidString(idRand, TYPE_MEMBER),
                                projectID, memberName.getEditText().getText().toString(), "",
                                memberSalt, memberPass1.getEditText().getText().toString(),
                                adminRole.roleID);
                    } else {
                        initialMember = new MemberData(generateValidString(idRand, TYPE_MEMBER),
                                projectID, memberName.getEditText().getText().toString(), "",
                                memberSalt, "", adminRole.roleID);
                    }
                    projectDatabase.MemberDao().insert(initialMember);

                    // Different types of project creation for different constructors
                    if (enableRoles) {
                        // Set default role according to user preference
                        Spinner roleSpinner = returnView.findViewById(R.id.p6_default_role_input);
                        RoleData defaultRole;
                        if (roleSpinner.getSelectedItemPosition() == 0) {
                            defaultRole = adminRole;
                        } else {
                            defaultRole = memberRole;
                        }

                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectPass, projectSalt, projectPass, memberRole, adminRole,
                                initialMember, defaultRole));
                    } else {
                        projectDatabase.ProjectDao().insert(new ProjectData(projectID,
                                projectPass, projectSalt, projectPass, memberRole, adminRole,
                                initialMember, adminRole));
                    }
                    projectDatabase.close();
                    // Go to project info
                    Toast.makeText(getActivity(), R.string.p6_project_created, Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) {
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
                        ((MainActivity) getActivity()).displayFragment(ProjectInfoFragment
                                .newInstance(projectID, adminRole.roleID,
                                        false, true));
                    }
                }
            }
        });
        return returnView;
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

    /** Generates a valid String based on its type. **/
    private String generateValidString(RandomString rand, int type) {
        String returnString = rand.nextString();
        switch (type) {
            case TYPE_PROJECT:
                while (projectDatabase.ProjectDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            case TYPE_MEMBER:
                while (projectDatabase.MemberDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            case TYPE_ROLE:
                while (projectDatabase.RoleDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            default:
                returnString = "";
        }
        return returnString;
    }
}
