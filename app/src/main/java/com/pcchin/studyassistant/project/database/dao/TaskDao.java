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
import androidx.room.Update;

import com.pcchin.studyassistant.project.database.data.TaskData;

@Dao
public interface TaskDao {
    // TODO: Complete

    /** Adds a new task into the database. **/
    @Insert
    void insert(TaskData status);

    /** Updates an existing task. **/
    @Update
    void update(TaskData status);

    /** Deletes an existing task. **/
    @Delete
    void delete(TaskData status);
}
