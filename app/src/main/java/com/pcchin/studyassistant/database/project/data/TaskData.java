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

import java.util.Date;

/** The entity for each task in the project. **/
@Entity
public class TaskData {
    // Status of each task.
    @Ignore
    public static final int TASK_COMPLETED = 5;
    @Ignore
    public static final int TASK_DELAYED = 4;
    @Ignore
    public static final int TASK_ONGOING = 3;
    @Ignore
    public static final int TASK_UPCOMING = 2;
    @Ignore
    public static final int TASK_FAILED = 1;
    @Ignore
    public static final int TASK_CUSTOM = 0;

    /** The ID for each task, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_taskID")
    public String taskID = "";

    /** The description for the task. **/
    public String taskDesc;

    /** The date that the task is expected to start. **/
    public Date expectedStartDate;

    /** The date that the task is expected to end. **/
    public Date expectedEndDate;

    /** The date that the task actually started. **/
    public Date actualStartDate;

    /** The date that the task actually ended. **/
    public Date actualEndDate;

    /** The ID of the member that is assigned to the list. **/
    public String assignedMember;

    /** The status for each task. **/
    public int taskStatus;

    /** Custom status for each task, if needed. **/
    public String taskStatusCustom;

    /** Special notes about the task. **/
    public String taskComments;

    /** Default constructor. **/
    public TaskData() {

    }
}
