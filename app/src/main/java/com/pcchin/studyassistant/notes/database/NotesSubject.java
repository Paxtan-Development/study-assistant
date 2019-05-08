package com.pcchin.studyassistant.notes.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class NotesSubject {
    @SuppressWarnings("NullableProblems")
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_title")
    public String title;

    @ColumnInfo(name = "contents")
    public String contents;

    NotesSubject() {}

    public NotesSubject(@NonNull String title, String contents) {
        this.title = title;
        this.contents = contents;
    }
}
