package com.pcchin.studyassistant.notes.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubjectDao {
    /** Search for a subject based on its title. **/
    @Query("SELECT * FROM notesSubject WHERE _title = :title")
    NotesSubject search(String title);

    /** Get all of the notes. **/
    @Query("SELECT * FROM notesSubject ORDER BY _title ASC")
    List<NotesSubject> getAll();

    /** Adds a new subject into the database. **/
    @Insert
    void insert(NotesSubject subject);

    /** Updates an existing subject. **/
    @Update
    void update(NotesSubject subject);

    /** Deletes an existing subject. **/
    @Delete
    void delete(NotesSubject subject);
}
