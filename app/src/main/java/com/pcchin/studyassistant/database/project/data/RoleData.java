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

package com.pcchin.studyassistant.database.project.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** The entity for each role of users in a project. **/
@Entity
public class RoleData {
    /** Reserved roles for each project. None is reserved as
     * it is possible for a task to be assigned to no one. **/
    @Ignore
    public static final String[] reservedRoles = {"admin", "none"};

    /** The ID for each role, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_roleID")
    public String roleID = "";

    /** The project ID which contains the role. **/
    public String parentProject;

    /** The name for each role. Cannot overlap with existing roles. **/
    public String roleName;

    /** The salt for the role used specifically to protect the password. **/
    public String salt;

    /** A hashed password, if needed, used by specific roles to access the project. **/
    public String rolePass;

    // A list of boolean values determining the privileges of the role
    /* A user by default (all false) is able to:
    1) View & modify his or her own user info
    2) View his or her own tasks
    3) View the general info for the project */
    public boolean canDeleteProject;
    public boolean canModifyInfo;
    public boolean canModifyOtherTask;
    public boolean canModifyOtherUser;
    public boolean canModifyOwnTask;
    public boolean canModifyRole;
    public boolean canModifyOtherStatus;
    public boolean canPostStatus;
    public boolean canSetPassword;
    // Implicitly granted with canModifyOtherTask
    public boolean canViewOtherTask;
    // Implicitly granted with canModifyOtherUser
    public boolean canViewOtherUser;
    // Implicitly granted with canModifyRole
    public boolean canViewRole;
    // Implicitly granted with canModifyOwnTask
    public boolean canViewTask;
    // Implicitly granted with canModifyOtherStatus and canPostStatus
    public boolean canViewStatus;
    public boolean canViewMedia;

    /** Default constructor. **/
    @Ignore
    public RoleData() {

    }

    /** Constructor used when creating a new role. Due to the number of boolean variables used,
     * they would be configured individually for each usage.
     * All the boolean values were set to false by default. **/
    public RoleData(@NonNull String roleID, String parentProject, String roleName,
                    String salt, String rolePass) {
        this.roleID = roleID;
        this.parentProject = parentProject;
        this.roleName = roleName;
        this.salt = salt;
        this.rolePass = rolePass;
        this.canDeleteProject = false;
        this.canModifyInfo = false;
        this.canModifyOtherTask = false;
        this.canModifyOtherUser = false;
        this.canModifyOwnTask = false;
        this.canModifyRole = false;
        this.canModifyOtherStatus = true;
        this.canPostStatus = true;
        this.canSetPassword = false;
        this.canViewOtherTask = false;
        this.canViewOtherUser = false;
        this.canViewRole = false;
        this.canViewTask = false;
        this.canViewStatus = false;
        this.canViewMedia = false;
    }
}
