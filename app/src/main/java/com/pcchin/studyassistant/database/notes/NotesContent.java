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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

/** The entity for each note. **/
@Entity
public class NotesContent {
    /** The ID for the note, serves as an unique key.
     * Integer used instead of int as it would not be able to annotate NonNull otherwise. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "_noteId")
    public Integer noteId;

    /** The ID for the parent subject for the note. **/
    public int subjectId;

    /** The title for the note. **/
    public String noteTitle;

    /** The content for the note. **/
    public String noteContent;

    /** The date that the note was last edited. **/
    public Date lastEdited;

    /** The salt used to hash the password used for locking the note. **/
    public String lockedSalt;

    /** The hashed password used to lock the note. **/
    public String lockedPass;

    /** The date and time that the alert on the note would be run. **/
    public Date alertDate;

    /** The alert code used in the alert for the note.
     * Integer used instead of int so that it can be null. **/
    public Integer alertCode;

    /** Default constructor. **/
    @Ignore
    NotesContent() {
        // Default constructor.
        this.noteId = 0;
    }

    /** Constructor used to create the note. **/
    public NotesContent(int noteId, int subjectId, String noteTitle, String noteContent,
                        Date lastEdited, String lockedSalt) {
        this.noteId = noteId;
        this.subjectId = subjectId;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.lastEdited = lastEdited;
        this.lockedSalt = lockedSalt;
        this.lockedPass = null;
        this.alertDate = null;
        this.alertCode = null;
    }

    /** Constructor used to create the note. Used when importing a new note. **/
    public NotesContent(int noteId, int subjectId, String noteTitle, String noteContent,
                        Date lastEdited, String lockedSalt, String lockedPass,
                        Date alertDate, Integer alertCode) {
        this.noteId = noteId;
        this.subjectId = subjectId;
        this.noteTitle = noteTitle;
        this.noteContent = noteContent;
        this.lastEdited = lastEdited;
        this.lockedSalt = lockedSalt;
        this.lockedPass = lockedPass;
        this.alertDate = alertDate;
        this.alertCode = alertCode;
    }
}
