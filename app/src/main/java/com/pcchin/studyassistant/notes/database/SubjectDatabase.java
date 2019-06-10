package com.pcchin.studyassistant.notes.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {NotesSubject.class}, version = 1, exportSchema = false)
public abstract class SubjectDatabase extends RoomDatabase {
    public abstract SubjectDao SubjectDao();
}
