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
    /** Reserved roles for each project. Admin is all-powerful while
     * the user can only access his/her info. None is reserved as
     * it is possible for a task to be assigned to no one. **/
    @Ignore
    public static final String[] reservedRoles = {"Admin", "User", "None"};

    /** The ID for each role, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_roleID")
    public String roleID = "";

    /** The project ID which contains the role. **/
    public String parentProject;

    /** The name for each role. Cannot overlap with existing roles. **/
    public String roleName;

    // A list of boolean values determining the privileges of the role
    /* A user by default (all false) is able to:
    1) View & modify his or her own user info
    2) View his or her own tasks
    3) View the general info for the project */
    public boolean canModifyOtherUser;
    public boolean canViewOtherUser;
    public boolean canModifyRole;
    public boolean canViewRole;
    public boolean canModifyOtherTask;
    public boolean canModifyOwnTask;
    public boolean canViewTask;
    public boolean canSetPassword;
    public boolean canModifyInfo;
    public boolean canDeleteProject;

    /** Default constructor. **/
    public RoleData() {

    }
}
