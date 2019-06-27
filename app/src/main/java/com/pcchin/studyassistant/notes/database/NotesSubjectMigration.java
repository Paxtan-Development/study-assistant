package com.pcchin.studyassistant.notes.database;

import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;

/** Functions to migrate the notesSubject database from one version to another. **/
public class NotesSubjectMigration {
    /** Migrates the Database from 1 to 2. Adds a sortOrder column. **/
    public static final androidx.room.migration.Migration MIGRATION_1_2 = new androidx.room.migration.Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE notesSubject ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT " +
                    NotesSubject.SORT_ALPHABETICAL_ASC);
        }
    };
}
