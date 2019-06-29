package com.pcchin.studyassistant.notes.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.ArrayList;

/** The entity for each Subject. **/
@Entity
public class NotesSubject {
    /** Sort notes by alphabetical order, ascending. **/
    @Ignore
    public static final int SORT_ALPHABETICAL_ASC = 1;
    /** Sort notes by alphabetical order, descending. **/
    @Ignore
    public static final int SORT_ALPHABETICAL_DES = 2;
    /** Sort notes by date, from oldest to newest. **/
    @Ignore
    public static final int SORT_DATE_ASC = 3;
    /** Sort notes by date, from newest to oldest. **/
    @Ignore
    public static final int SORT_DATE_DES = 4;

    /** The title of the subject. Serves as a unique key. **/
    @SuppressWarnings("NullableProblems")
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_title")
    public String title;

    /** The contents of the subject. Contains the values of the notes. **/
    @ColumnInfo(name = "contents")
    public ArrayList<ArrayList<String>> contents;

    /** The order in which the notes are sorted. The value is one of the 4 constants above. **/
    @ColumnInfo(name = "sortOrder")
    public int sortOrder;

    /** Default constructor. **/
    @Ignore
    NotesSubject() {}

    /** Constructor used in the current version. **/
    public NotesSubject(@NonNull String title, ArrayList<ArrayList<String>> contents, int sortOrder) {
        this.title = title;
        this.contents = contents;
        this.sortOrder = sortOrder;
    }
}
