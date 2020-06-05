/*
 * Copyright 2020 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.database.notes;

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

    /** The ID for the subject, serves as an unique key.
     * Integer used instead of int as it would not be able to annotate NonNull otherwise. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_subjectId")
    public Integer subjectId;

    /** The title of the subject. **/
    public String title;

    /** The contents of the subject. Contains the values of the notes.
     * WARNING: This variable is outdated and will be replaced in future beta releases. **/
    // TODO: Remove variable
    public ArrayList<ArrayList<String>> contents;

    /** The order in which the notes are sorted. The value is one of the 4 constants above. **/
    public int sortOrder;

    /** Default constructor. **/
    @Ignore
    NotesSubject() {
        // Default constructor.
        this.subjectId = 0;
    }

    /** Constructor used to create the subject. **/
    public NotesSubject(@NonNull String title, ArrayList<ArrayList<String>> contents, int sortOrder) {
        // TODO: Change constructor to add ID
        this.subjectId = 0;
        this.title = title;
        this.contents = contents;
        this.sortOrder = sortOrder;
    }
}
