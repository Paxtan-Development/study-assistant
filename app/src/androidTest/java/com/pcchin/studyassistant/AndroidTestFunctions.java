package com.pcchin.studyassistant;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Random;

/** Functions used in tests. **/
class AndroidTestFunctions {
    /** Returns the database after it has been migrated. **/
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
