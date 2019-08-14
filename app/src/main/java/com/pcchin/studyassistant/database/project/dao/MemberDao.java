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

import com.pcchin.studyassistant.database.project.data.MemberData;

import java.util.List;

@Dao
public interface MemberDao {
    /** Search for a member based on his ID. **/
    @Query("SELECT * FROM memberData WHERE _memberID = :ID")
    MemberData searchByID(String ID);

    /** Search for members in a project. **/
    @Query("SELECT * FROM memberData WHERE parentProject = :projectID")
    List<MemberData> searchByProject(String projectID);

    /** Search for members in a project based on their roles. **/
    @Query("SELECT * FROM memberData WHERE parentProject = :projectID AND role = :roleID")
    List<MemberData> searchRoleInProject(String projectID, String roleID);

    /** Search for members in all project based on their roles. **/
    @Query("SELECT * FROM memberData WHERE role = :roleID")
    List<MemberData> searchByRole(String roleID);

    /** Search for a member in all projects based on his username. **/
    @Query("SELECT * FROM memberData WHERE username = :username")
    List<MemberData> searchByUsername(String username);

    /** Adds a new role into the notes. **/
    @Insert
    void insert(MemberData member);

    /** Updates an existing role. **/
    @Update
    void update(MemberData member);

    /** Deletes an existing role. **/
    @Delete
    void delete(MemberData member);
}
