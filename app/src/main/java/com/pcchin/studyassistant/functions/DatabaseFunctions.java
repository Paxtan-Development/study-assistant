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

package com.pcchin.studyassistant.functions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.utils.misc.RandomString;
import com.pcchin.studyassistant.utils.misc.SortingComparators;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Database related functions used throughout the app. **/
public final class DatabaseFunctions {
    public enum SubjIdType {
        SUBJECT,
        NOTE
    }

    public enum ProjIdType {
        PROJECT,
        ROLE,
        MEMBER
    }

    private DatabaseFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Returns the subject database. **/
    @NonNull
    public static SubjectDatabase getSubjectDatabase(Context context) {
        return Room.databaseBuilder(context, SubjectDatabase.class,
                ActivityConstants.DATABASE_NOTES)
                .fallbackToDestructiveMigrationFrom(1, 2, 3)
                .allowMainThreadQueries().build();
    }

    /** Returns the project database. **/
    @NonNull
    public static ProjectDatabase getProjectDatabase(Context context) {
        return Room.databaseBuilder(context, ProjectDatabase.class,
                ActivityConstants.DATABASE_PROJECT)
                .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
                .allowMainThreadQueries().build();
    }

    /** Gets the icon path of a project for a specific project ID. **/
    @NonNull
    public static String getProjectIconPath(@NonNull Context context, String projectID) {
        return context.getFilesDir() + "/icons/project/" + projectID + ".jpg";
    }

    /** Generates a valid subject ID to be used to create a new subject. **/
    public static int generateValidId(@NonNull SubjectDatabase database, @NonNull SubjIdType type) {
        List<Integer> idList;
        if (type.equals(SubjIdType.SUBJECT)) {
            idList = database.SubjectDao().getAllSubjectId();
        } else {
            idList = database.ContentDao().getAllNoteId();
        }
        Random idRand = new Random();
        int currentId = idRand.nextInt();
        while (idList.contains(currentId)) currentId = idRand.nextInt();
        return currentId;
    }

    /** Generates a valid random String based on the type specified. **/
    public static String generateValidProjectString(@NonNull RandomString rand, @NonNull ProjIdType type,
                                                    ProjectDatabase projectDatabase) {
        String returnString = rand.nextString();
        switch (type) {
            case PROJECT:
                while (projectDatabase.ProjectDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            case MEMBER:
                while (projectDatabase.MemberDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            case ROLE:
                while (projectDatabase.RoleDao().searchByID(returnString) != null) {
                    returnString = rand.nextString();
                }
                break;
            default:
                returnString = "";
        }
        return returnString;
    }

    /** Sort the notes based on the sorting format given.
     * @see NotesSubject
     * @see SortingComparators **/
    public static void sortNotes(Context context, @NonNull NotesSubject currentSubject, List<NotesContent> notesList) {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(context);
        int sortOrder = currentSubject.sortOrder;
        if (sortOrder == NotesSubject.SORT_ALPHABETICAL_DES) {
            // Sort by alphabetical order, descending
            Collections.sort(notesList, SortingComparators.noteTitleComparator);
            Collections.reverse(notesList);
        } else if (sortOrder == NotesSubject.SORT_DATE_ASC) {
            Collections.sort(notesList, SortingComparators.noteDateComparator);
        } else if (sortOrder == NotesSubject.SORT_DATE_DES) {
            Collections.sort(notesList, SortingComparators.noteDateComparator);
            Collections.reverse(notesList);
        } else {
            // Sort by alphabetical order, ascending
            if (sortOrder != NotesSubject.SORT_ALPHABETICAL_ASC) {
                // Default to this if sortOrder is invalid
                currentSubject.sortOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
            }
            Collections.sort(notesList, SortingComparators.noteTitleComparator);
        }
        database.close();
    }
}
