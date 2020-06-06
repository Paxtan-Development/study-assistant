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

package com.pcchin.studyassistant.database.notes;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubjectDao {
    /** Search for a subject based on its title. **/
    @Query("SELECT * FROM notesSubject WHERE title = :title")
    NotesSubject searchByTitle(String title);

    /** Search for a subject based on its ID.
     * Integer is used instead of int as subjectId might be null. **/
    @Query("SELECT * FROM notesSubject WHERE _subjectId = :subjectId")
    NotesSubject searchById(Integer subjectId);

    /** Get all of the subjects. **/
    @Query("SELECT * FROM notesSubject ORDER BY title ASC")
    List<NotesSubject> getAll();

    /** Get all of the IDs for all subjects. **/
    @Query("SELECT _subjectId from notesSubject")
    List<Integer> getAllSubjectId();

    /** Adds a new subject into the notes. **/
    @Insert
    void insert(NotesSubject subject);

    /** Updates an existing subject. **/
    @Update
    void update(NotesSubject subject);

    /** Deletes an existing subject. **/
    @Delete
    void delete(NotesSubject subject);
}
