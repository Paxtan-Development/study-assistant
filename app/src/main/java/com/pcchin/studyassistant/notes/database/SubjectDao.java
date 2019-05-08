package com.pcchin.studyassistant.notes.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

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
