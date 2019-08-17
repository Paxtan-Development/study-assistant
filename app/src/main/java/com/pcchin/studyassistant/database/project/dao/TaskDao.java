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

package com.pcchin.studyassistant.database.project.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pcchin.studyassistant.database.project.data.TaskData;

import java.util.List;

@Dao
public interface TaskDao {
    /** Search for a task based on its ID. **/
    @Query("SELECT * FROM taskData WHERE _taskID = :ID")
    TaskData searchById(String ID);

    /** Search for a task based on its parent project. **/
    @Query("SELECT * FROM taskData WHERE parentProject = :projectID")
    List<TaskData> searchByProject(String projectID);

    /** Search for a task based on its current status. **/
    @Query("SELECT * FROM taskData WHERE taskStatus = :status")
    List<TaskData> searchByStatus(int status);

    /** Search for a task based on its title. **/
    @Query("SELECT * FROM taskData WHERE assignedMember = :member")
    List<TaskData> searchByMember(String member);

    /** Search for a task based on its description. **/
    @Query("SELECT * FROM taskData WHERE taskDesc = :desc")
    List<TaskData> searchByDesc(String desc);

    /** Search for a task based on its current status. **/
    @Query("SELECT * FROM taskData WHERE parentProject = :projectID AND taskStatus = :status")
    List<TaskData> searchInProjectByStatus(String projectID, int status);

    /** Search for a task based on its title. **/
    @Query("SELECT * FROM taskData WHERE parentProject = :projectID AND assignedMember = :member")
    List<TaskData> searchInProjectByMember(String projectID, String member);

    /** Search for a task based on its description. **/
    @Query("SELECT * FROM taskData WHERE parentProject = :projectID AND taskDesc = :desc")
    List<TaskData> searchInProjectByDesc(String projectID, String desc);

    /** Adds a new task into the notes. **/
    @Insert
    void insert(TaskData status);

    /** Updates an existing task. **/
    @Update
    void update(TaskData status);

    /** Deletes an existing task. **/
    @Delete
    void delete(TaskData status);
}
