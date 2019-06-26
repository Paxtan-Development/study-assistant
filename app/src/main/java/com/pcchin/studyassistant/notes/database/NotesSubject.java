package com.pcchin.studyassistant.notes.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class NotesSubject {
    @Ignore
    public static final int SORT_ALPHABETICAL_ASC = 1;
    @Ignore
    public static final int SORT_ALPHABETICAL_DES = 2;
    @Ignore
    public static final int SORT_DATE_ASC = 3;
    @Ignore
    public static final int SORT_DATE_DES = 4;

    @SuppressWarnings("NullableProblems")
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_title")
    public String title;

    @ColumnInfo(name = "contents")
    public String contents;

    @ColumnInfo(name = "sortOrder")
    public int sortOrder;

    @Ignore
    NotesSubject() {}

    public NotesSubject(@NonNull String title, String contents, int sortOrder) {
        this.title = title;
        this.contents = contents;
        this.sortOrder = sortOrder;
    }
}
