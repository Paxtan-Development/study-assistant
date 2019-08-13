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

package com.pcchin.studyassistant.project.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.pcchin.studyassistant.project.database.data.RoleData;

import java.util.List;

@Dao
public interface RoleDao {
    /** Search for a role based on its ID. **/
    @Query("SELECT * FROM roledata WHERE _roleID = :ID")
    RoleData searchByID(String ID);

    /** Search for roles based on their name. **/
    @Query("SELECT * FROM roleData WHERE roleName = :name")
    List<RoleData> searchByName(String name);

    /** Search for roles based on their parent project. **/
    @Query("SELECT * FROM roleData WHERE parentProject = :projectID")
    List<RoleData> searchByProject(String projectID);

    /** Search for roles based on their parent project and name. **/
    @Query("SELECT * FROM roleData WHERE parentPRoject = :projectID AND roleName = :name")
    List<RoleData> searchInProjectByName(String projectID, String name);

    /** Adds a new role into the database. **/
    @Insert
    void add(RoleData role);

    /** Updates an existing role. **/
    @Update
    void update(RoleData role);

    /** Deletes an existing role. **/
    @Delete
    void delete(RoleData role);
}
