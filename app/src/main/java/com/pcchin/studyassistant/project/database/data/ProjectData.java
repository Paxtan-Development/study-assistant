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

package com.pcchin.studyassistant.project.database.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.Date;

/** The entity for each project. **/
@Entity
public class ProjectData {
    /** The ID for each project, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_projectID")
    public String projectID = "";

    /** The salt for each project used specifically to protect the password. **/
    public String salt;

    /** A hashed password, if needed, to access the project. **/
    public String projectPass;

    /** The description of the project. **/
    public String description;

    /** The date that the project was started. **/
    public Date startDate;

    /** The due date of the project. **/
    public Date dueDate;

    /** Whether members are enabled in the project. **/
    public boolean membersEnabled;

    /** Whether roles are enabled in the project.
     * If membersEnabled is false but this is true, users would need to sign in with their roles.
     * If membersEnabled is true but this is false, all members would have admin privileges.
     * If both membersEnabled and this are true, members would have their own roles.
     * If both membersEnabled and this are false, all users would have admin privileges. **/
    public boolean rolesEnabled;

    /** If this is true, tasks and status share the same database (from Tasks),
     * while if this is false, tasks and status have separate databases. **/
    public boolean mergeTaskStatus;

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

    /** Default constructor. **/
    ProjectData() {

    }
}
