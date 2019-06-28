package com.pcchin.studyassistant;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.room.testing.MigrationTestHelper;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/** Functions used in tests. **/
class AndroidTestFunctions {
    static SubjectDatabase getDatabaseAftMigration(@NonNull MigrationTestHelper migrationTestHelper,
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

    @NonNull
    static String randomString(int count) {
        return RandomStringUtils.random(new Random().nextInt(count));
    }
}
