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

import com.pcchin.studyassistant.database.project.data.StatusData;

import java.util.List;

@Dao
public interface StatusDao {
    /** Search for a status based on its ID. **/
    @Query("SELECT * FROM statusData WHERE _statusID = :ID")
    StatusData searchByID(String ID);

    /** Search for a status based on its title. **/
    @Query("SELECT * FROM statusData WHERE statusTitle = :title")
    List<StatusData> searchByTitle(String title);

    /** Search for a status based on its parent project. **/
    @Query("SELECT * FROM statusData WHERE parentProject = :projectID")
    List<StatusData> searchByProject(String projectID);

    /** Search for a status based on its type. **/
    @Query("SELECT * FROM statusData WHERE statusType = :type")
    List<StatusData> searchByType(String type);

    /** Search for a status based on its member. **/
    @Query("SELECT * FROM statusData WHERE memberID = :memberID")
    List<StatusData> searchByMember(String memberID);

    /** Search for a status in a project based on its title. **/
    @Query("SELECT * FROM statusData WHERE parentProject = :projectID AND statusTitle = :title")
    List<StatusData> searchInProjectByTitle(String projectID, String title);

    /** Search for a status in a project based on its type. **/
    @Query("SELECT * FROM statusData WHERE parentProject = :projectID AND statusType = :type")
    List<StatusData> searchInProjectByType(String projectID, String type);

    /** Search for a status in a project based on its member. **/
    @Query("SELECT * FROM statusData WHERE parentProject = :projectID AND memberID = :memberID")
    List<StatusData> searchInProjectByMember(String projectID, String memberID);

    /** Adds a new role into the notes. **/
    @Insert
    void insert(StatusData status);

    /** Updates an existing role. **/
    @Update
    void update(StatusData status);

    /** Deletes an existing role. **/
    @Delete
    void delete(StatusData status);
}
