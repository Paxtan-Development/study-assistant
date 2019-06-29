package com.pcchin.studyassistant.notes.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pcchin.studyassistant.functions.ConverterFunctions;

/** The database layer for the subjects. **/
@Database(entities = {NotesSubject.class}, version = 2)
@TypeConverters({ConverterFunctions.class})
public abstract class SubjectDatabase extends RoomDatabase {
    /** References the SQL requests in the interface. **/
    public abstract SubjectDao SubjectDao();
}
