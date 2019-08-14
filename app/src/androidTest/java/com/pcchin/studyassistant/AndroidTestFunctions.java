/*
 * Copyright 2019 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pcchin.studyassistant.database.notes.SubjectDatabase;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Random;

/** Functions used in tests. **/
class AndroidTestFunctions {
    /** Returns the notes after it has been migrated. **/
    static SubjectDatabase getNotesDatabaseAftMigration(@NonNull MigrationTestHelper migrationTestHelper,
                                                        String databaseName,
                                                        Migration... migrations) {
        SubjectDatabase roomDatabase = Room
                .databaseBuilder(InstrumentationRegistry.getInstrumentation().getContext(),
                        SubjectDatabase.class, databaseName)
                .addMigrations(migrations)
                .build();
        migrationTestHelper.closeWhenFinished(roomDatabase);
        return roomDatabase;

    }

    /** Generate a string with random characters.
     * Some characters have been sanitized to prevent accidental SQL injections.
     * @param count defines the max number of characters in the string. **/
    @NonNull
    static String randomString(int count) {
        return RandomStringUtils.random(new Random().nextInt(count))
                .replace(",", "\\,").replace(")", "\\)")
                .replace(";", "\\;").replace("(", "\\(")
                .replace("-", "\\-");
    }

    /** Generate an ArrayList with random values.
     * @param count defines the max number of children in the array. **/
    static ArrayList<ArrayList<String>> randomArray(int count) {
        Random rand = new Random();
        ArrayList<ArrayList<String>> returnList = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(count); i++) {
            ArrayList<String> temp = new ArrayList<>();
            for (int j = 0; j < rand.nextInt(count); j++) {
                temp.add(randomString(count));
            }
            returnList.add(temp);
        }
        return returnList;
    }
}
