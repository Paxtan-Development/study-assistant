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

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.utils.misc.RandomString;

import java.util.Objects;

/** Functions used when creating a project in ProjectCreateFragment. **/
final class ProjectCreateFragmentCreate {
    private ProjectCreateFragmentCreate() {
        throw new IllegalStateException("Utility class");
    }

    /** Creates the admin role based on the given info. **/
    @NonNull
    static RoleData createAdminRole(boolean customAdmin, RandomString idRand,
                                     RandomString saltRand, String projectID,
                                     TextInputLayout customAdminName,
                                     TextInputLayout customAdminPass1,
                                     ProjectDatabase projectDatabase) {
        RoleData adminRole;
        if (customAdmin && customAdminName.getEditText() != null && customAdminPass1.getEditText() != null) {
            if (customAdminPass1.getEditText().getText().length() > 0) {
                // Admin with password
                String adminSalt = saltRand.nextString();
                adminRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                        ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                        customAdminName.getEditText().getText().toString(),
                        adminSalt, SecurityFunctions.passwordHash(customAdminPass1
                        .getEditText().getText().toString(), adminSalt));
            } else {
                adminRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                        ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                        customAdminName.getEditText().getText().toString(), saltRand.nextString(), "");
            }
        } else {
            adminRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                    ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                    "Admin", saltRand.nextString(), "");
        }
        setAdminPrivileges(adminRole);
        return adminRole;
    }

    /** Sets the permissions for the admin role. **/
    private static void setAdminPrivileges(@NonNull RoleData adminRole) {
        adminRole.canDeleteProject = true;
        adminRole.canModifyInfo = true;
        adminRole.canModifyOtherTask = true;
        adminRole.canModifyOtherUser = true;
        adminRole.canModifyOwnTask = true;
        adminRole.canModifyRole = true;
        adminRole.canModifyOtherStatus = true;
        adminRole.canPostStatus = true;
        adminRole.canSetPassword = true;
        adminRole.canViewOtherTask = true;
        adminRole.canViewOtherUser = true;
        adminRole.canViewRole = true;
        adminRole.canViewTask = true;
        adminRole.canViewStatus = true;
        adminRole.canViewMedia = true;
    }

    /** Creates the member role based on the given info. **/
    @NonNull
    static RoleData createMemberRole(boolean customMember, RandomString idRand,
                                      RandomString saltRand, String projectID,
                                      TextInputLayout customMemberName,
                                      TextInputLayout customMemberPass1,
                                      ProjectDatabase projectDatabase) {
        RoleData memberRole;
        // Creates member role
        if (customMember) {
            memberRole = getCustomMemberRole(projectDatabase, projectID, idRand, saltRand,
                    customMemberName, customMemberPass1);
        } else {
            memberRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                    ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                    "Member", saltRand.nextString(), "");
        }
        return memberRole;
    }

    /** Gets a custom member role based on the inputs provided by the user. **/
    @NonNull
    private static RoleData getCustomMemberRole(ProjectDatabase projectDatabase, String projectID,
                                                RandomString idRand, RandomString saltRand,
                                                TextInputLayout customMemberName, @NonNull TextInputLayout customMemberPass1) {
        RoleData memberRole;
        if (Objects.requireNonNull(customMemberPass1.getEditText()).getText().length() > 0) {
            // Admin with password
            String memberSalt = saltRand.nextString();
            memberRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                    ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                    Objects.requireNonNull(customMemberName.getEditText()).getText().toString(),
                    memberSalt, SecurityFunctions.passwordHash(customMemberPass1
                    .getEditText().getText().toString(), memberSalt));
        } else {
            memberRole = new RoleData(DatabaseFunctions.generateValidProjectString(idRand,
                    ProjectCreateFragment.TYPE_ROLE, projectDatabase), projectID,
                    Objects.requireNonNull(customMemberName.getEditText()).getText().toString(),
                    saltRand.nextString(), "");
        }
        return memberRole;
    }

    /** Creates the initial member based on the given info. **/
    static MemberData createInitialMember(RandomString idRand, @NonNull RandomString saltRand,
                                          String projectID, String projectSalt, String adminRoleID,
                                          @NonNull TextInputLayout memberName,
                                          TextInputLayout memberPass1,
                                          ProjectDatabase projectDatabase) {
        MemberData initialMember = null;
        String memberSalt = saltRand.nextString();
        if (memberName.getEditText() != null && memberPass1.getEditText() != null) {
            if (memberPass1.getEditText().getText().length() > 0) {
                initialMember = new MemberData(DatabaseFunctions
                        .generateValidProjectString(idRand, ProjectCreateFragment.TYPE_MEMBER, projectDatabase),
                        projectID, memberName.getEditText().getText().toString(), "",
                        memberSalt,
                        SecurityFunctions.passwordHash(memberPass1.getEditText()
                                .getText().toString(), memberSalt),
                        adminRoleID);
            } else {
                initialMember = new MemberData(DatabaseFunctions
                        .generateValidProjectString(idRand, ProjectCreateFragment.TYPE_MEMBER, projectDatabase),
                        projectID, memberName.getEditText().getText().toString(), "",
                        memberSalt, "", adminRoleID);
            }
        }
        return initialMember;
    }
}
