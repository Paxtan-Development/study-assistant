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

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.ui.MainActivity;

/** Functions generally used throughout the app. **/
public final class GeneralFunctions {
    /** Exits the app.**/
    public static void exitApp(@NonNull Activity activity) {
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /** Reloads a fragment. **/
    public static void reloadFragment(@NonNull Fragment target) {
        target.getParentFragmentManager().beginTransaction()
                .detach(target)
                .attach(target).commit();
    }

    /** Returns the subject database. **/
    @NonNull
    public static SubjectDatabase getSubjectDatabase(Context context) {
        return Room.databaseBuilder(context, SubjectDatabase.class,
                MainActivity.DATABASE_NOTES).allowMainThreadQueries()
                .addMigrations(NotesSubjectMigration.MIGRATION_1_2).build();
    }

    /** Returns the project database. **/
    @NonNull
    public static ProjectDatabase getProjectDatabase(Context context) {
        return Room.databaseBuilder(context, ProjectDatabase.class,
                MainActivity.DATABASE_PROJECT)
                .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5)
                .allowMainThreadQueries().build();
    }
}
