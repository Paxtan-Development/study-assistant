package com.pcchin.studyassistant.notes.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class NotesSubject {
    @SuppressWarnings("NullableProblems")
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_title")
    public String title;

    @ColumnInfo(name = "contents")
    public String contents;

    @Ignore
    NotesSubject() {}

    public NotesSubject(@NonNull String title, String contents) {
        this.title = title;
        this.contents = contents;
    }
}
