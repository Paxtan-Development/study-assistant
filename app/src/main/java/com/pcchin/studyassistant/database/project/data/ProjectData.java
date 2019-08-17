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

import java.util.ArrayList;
import java.util.Date;

/** The entity for each project. Each project should also have its own folder named after its ID
 * to store images related to it. **/
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

    /** The path stored in the local storage that points to the icon for the project. **/
    public String projectIcon;

    /** The resource file pointing to the icon for the project status. **/
    public int projectStatusIcon;

    /** The date that the project is expected to start. **/
    public Date expectedStartDate;

    /** The date that the task is expected to end. **/
    public Date expectedEndDate;

    /** The date that the task actually started. **/
    public Date actualStartDate;

    /** The date that the task actually ended. **/
    public Date actualEndDate;

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

    /** If this is true, tasks and status share the same notes (from Tasks),
     * while if this is false, tasks and status have separate databases. **/
    public boolean mergeTaskStatus;

    /** Whether tasks are enabled in the project. **/
    public boolean taskEnabled;

    /** Whether status updates specifically are enabled in the project.
     * Both taskEnabled and statusEnabled must be true for mergeTaskStatus to be used. **/
    public boolean statusEnabled;

    /** A list of all member IDs in the project.
     * @see androidx.room.ForeignKey cannot be used in this case as
     * there are multiple members per entity.**/
    public ArrayList<String> memberList;

    /** A list of all the tasks IDs in the project.
     * @see androidx.room.ForeignKey cannot be used in this case as
     * there are multiple tasks per entity. **/
    public ArrayList<String> taskList;

    /** A list of all the roles in the project.
     * @see androidx.room.ForeignKey cannot be used in this case as
     * there are multiple roles per entity. **/
    public ArrayList<String> roleList;

    /** A list of all the status updates in the project.
     * @see androidx.room.ForeignKey cannot be used in this case as
     * there are multiple roles per entity. **/
    public ArrayList<String> statusList;

    /** The info that is displayed at the bottom of the project info page. **/
    public int displayedInfo;

    /** The title of the note subject that is associated to the project. **/
    public String associatedSubject;

    /** Default constructor. **/
    public ProjectData() {

    }
}