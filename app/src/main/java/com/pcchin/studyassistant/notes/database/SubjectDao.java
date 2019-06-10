package com.pcchin.studyassistant.notes.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SubjectDao {
    @Query("SELECT * FROM notesSubject WHERE _title = :title")
    NotesSubject search(String title);

    @Query("SELECT * FROM notesSubject ORDER BY _title ASC")
    List<NotesSubject> getAll();

    @Insert
    void insert(NotesSubject subject);

    @Update
    void update(NotesSubject subject);

    @Delete
    void delete(NotesSubject subject);
}
