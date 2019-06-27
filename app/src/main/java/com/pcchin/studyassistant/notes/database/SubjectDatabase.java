package com.pcchin.studyassistant.notes.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

/** The database layer for the subjects. **/
@Database(entities = {NotesSubject.class}, version = 2, exportSchema = false)
public abstract class SubjectDatabase extends RoomDatabase {
    /** References the SQL requests in the interface. **/
    public abstract SubjectDao SubjectDao();
}
