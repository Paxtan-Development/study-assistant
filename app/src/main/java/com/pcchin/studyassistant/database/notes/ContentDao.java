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
public interface ContentDao {
    /** Search for a note based on its ID. **/
    @Query("SELECT * FROM notesContent WHERE _noteId = :id")
    NotesContent search(int id);

    /** Get all of the notes for a specific subject. **/
    @Query("SELECT * FROM notesContent WHERE subjectId = :subjectId")
    List<NotesContent> searchBySubject(int subjectId);

    /** Get all of the IDs for all notes. **/
    @Query("SELECT _noteId from notesContent")
    List<Integer> getAllNoteId();

    /** Adds a new subject into the notes. **/
    @Insert
    void insert(NotesContent note);

    /** Updates an existing subject. **/
    @Update
    void update(NotesContent note);

    /** Deletes an existing subject. **/
    @Delete
    void delete(NotesContent note);
}
