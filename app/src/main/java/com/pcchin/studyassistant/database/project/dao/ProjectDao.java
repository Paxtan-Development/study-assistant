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

import com.pcchin.studyassistant.database.project.data.ProjectData;

import java.util.List;

@Dao
public interface ProjectDao {
    /** Search for a project based on its ID. **/
    @Query("SELECT * FROM projectData WHERE _projectID = :id")
    ProjectData searchByID(String id);

    /** Search for projects based on its title. Multiple projects can have the same title. **/
    @Query("SELECT * FROM projectData WHERE projectTitle = :title")
    List<ProjectData> searchByTitle(String title);

    /** Search for projects based on its related subject.
     * Multiple projects can have ths same subject. **/
    @Query("SELECT * FROM projectData WHERE associatedSubject = :subject")
    List<ProjectData> searchBySubject(String subject);

    /** Returns all the existing projects in the notes. **/
    @Query("SELECT * FROM projectData")
    List<ProjectData> getAllProjects();

    /** Adds a new project into the notes. **/
    @Insert
    void insert(ProjectData project);

    /** Updates an existing project. **/
    @Update
    void update(ProjectData project);

    /** Deletes an existing project. **/
    @Delete
    void delete(ProjectData project);
}
