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

package com.pcchin.studyassistant.database.project.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.pcchin.studyassistant.R;

import java.util.Date;

/** The entity for each project. Each project should also have its own folder named after its ID
 * to store images related to it. Could be updated to some sort of secure key in the future but **/
@Entity
public class ProjectData {
    // The types of content that is displayed at the bottom of the project info page.
    @Ignore
    public static final int DISPLAYED_NONE = 0;
    @Ignore
    public static final int DISPLAYED_MEDIA = 1;
    @Ignore
    public static final int DISPLAYED_MEMBERS = 2;
    @Ignore
    public static final int DISPLAYED_ROLES = 3;
    @Ignore
    public static final int DISPLAYED_TASKS = 4;

    /** The ID for each project, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_projectID")
    public String projectID = "";

    /** The salt for each project used specifically to protect the password. **/
    public String salt;

    /** The title of the project. **/
    public String projectTitle;

    /** A hashed password, if needed, to access the project. **/
    public String projectPass;

    /** The description of the project. **/
    public String description;

    /** Whether the project has an icon. The default icon path is at
     * getFilesDir() + /icons/project/ + projectID.jpg **/
    public boolean hasIcon;

    /** The resource file pointing to the icon for the project status.
     * For projects without status icons, R.string.blank is used. **/
    public int projectStatusIcon;

    /** The date that the project is expected to start. **/
    public Date expectedStartDate;

    /** The date that the task is expected to end. **/
    public Date expectedEndDate;

    /** The date that the task actually started. **/
    public Date actualStartDate;

    /** The date that the task actually ended. **/
    public Date actualEndDate;

    /** Whether the the project is protected and requires a password to enter. **/
    public boolean projectProtected;

    /** Whether members can sign up via the login screen. **/
    public boolean memberSignupEnabled;

    /** The default role of members in the project. **/
    public String memberDefaultRole;

    /** Whether members are enabled in the project. **/
    public boolean membersEnabled;

    /** Whether roles are enabled in the project.
     * If membersEnabled is false but this is true, users would need to sign in with their roles.
     * If membersEnabled is true but this is false, all members would have admin privileges.
     * If both membersEnabled and this are true, members would have their own roles.
     * If both membersEnabled and this are false, all users would have admin privileges. **/
    public boolean rolesEnabled;

    /** Whether tasks are enabled in the project. **/
    public boolean taskEnabled;

    /** Whether status updates specifically are enabled in the project.
     * Both taskEnabled and statusEnabled must be true for mergeTaskStatus to be used. **/
    public boolean statusEnabled;

    /** If this is true, tasks and status share the same notes (from Tasks),
     * while if this is false, tasks and status have separate databases. **/
    public boolean mergeTaskStatus;

    /** The info that is displayed at the bottom of the project info page. **/
    public int displayedInfo;

    /** The ID of the note subject that is associated to the project.
     * Integer is used instead of int so that it can be null. **/
    public Integer associatedSubject;

    /** Whether the project is still ongoing. **/
    public boolean projectOngoing;

    /** Default constructor.
     * This constructor should not be used in code but is unable to be
     * marked as @Deprecated or @Ignore as it is the default constructor. **/
    public ProjectData() {
        // Default constructor, should not be used in code.
    }

    /** Constructor used when creating a new project with members.
     * This constructor is still used and not deprecated despite marked as @Ignore.
     * Inserts a admin member and two default roles, admin and member.
     * Even when roles are disabled, a default role would always be set up so that every
     * user will be assigned to it.
     * By default, tasks would be displayed on the description,
     * the actual start date would be the date that the project was created,
     * and the default role of new members would be "member". **/
    @Ignore
    public ProjectData(@NonNull String projectID, String projectTitle, String salt,
                       String projectPass, RoleData memberRole, RoleData adminRole,
                       MemberData defaultAdmin, RoleData memberDefaultRole) {
        this.projectID = projectID;
        this.salt = salt;
        this.projectTitle = projectTitle;
        this.projectPass = projectPass;
        this.description = "";
        this.hasIcon = false;
        this.projectStatusIcon = R.drawable.status_ic_circle;
        this.actualStartDate = new Date();
        this.projectProtected = projectPass.length() != 0;
        this.memberSignupEnabled = true;
        this.memberDefaultRole = memberDefaultRole.roleID;
        this.membersEnabled = true;
        this.rolesEnabled = true;
        this.taskEnabled = true;
        this.statusEnabled = true;
        this.mergeTaskStatus = false;
        this.displayedInfo = DISPLAYED_TASKS;
        this.projectOngoing = true;
        this.associatedSubject = null;
    }

    /** Constructor used when creating a new project without members.
     * This constructor is still used and not deprecated despite marked as @Ignore.
     * Inserts a admin and member role.
     * Even when roles are disabled, a default role would always be set up when it is required
     * By default, tasks would be displayed on the description,
     * the actual start date would be the date that the project was created,
     * and the default role of new members would be "member". **/
    @Ignore
    public ProjectData(@NonNull String projectID, String projectTitle, String salt,
                       String projectPass, boolean rolesEnabled,
                       RoleData adminRole, RoleData memberRole) {
        this.projectID = projectID;
        this.salt = salt;
        this.projectTitle = projectTitle;
        this.projectPass = projectPass;
        this.description = "";
        this.hasIcon = false;
        this.projectStatusIcon = R.drawable.status_ic_circle;
        this.actualStartDate = new Date();
        this.projectProtected = projectPass.length() != 0;
        this.memberSignupEnabled = true;
        this.memberDefaultRole = memberRole.roleID;
        this.membersEnabled = false;
        this.rolesEnabled = rolesEnabled;
        this.taskEnabled = true;
        this.statusEnabled = true;
        this.mergeTaskStatus = false;
        this.displayedInfo = DISPLAYED_TASKS;
        this.projectOngoing = true;
        this.associatedSubject = null;
    }
}
