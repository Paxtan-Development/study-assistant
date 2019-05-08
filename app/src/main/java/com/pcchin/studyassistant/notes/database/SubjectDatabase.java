package com.pcchin.studyassistant.notes.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {NotesSubject.class}, version = 1, exportSchema = false)
public abstract class SubjectDatabase extends RoomDatabase {
    public abstract SubjectDao SubjectDao();
}
