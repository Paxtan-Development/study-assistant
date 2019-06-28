package com.pcchin.studyassistant;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

/** Check whether all functions used in the NotesSubject database is correct. **/
public class NotesDatabaseTest {
    @Rule
    public static final int TEST_COUNT = 1000;
    @Rule
    public static final String TEST_DB_NAME = AndroidTestFunctions.randomString(TEST_COUNT);
    @Rule
    public String testUser = AndroidTestFunctions.randomString(TEST_COUNT);
    @Rule
    public String testContents = AndroidTestFunctions.randomString(TEST_COUNT);
    @Rule
    private Random rand  = new Random();
    @Rule
    public MigrationTestHelper testHelper =
            new MigrationTestHelper(
                    InstrumentationRegistry.getInstrumentation(),
                    SubjectDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());

    /** Check the data integrity between versions 1 to 2. **/
    @SuppressWarnings("deprecation")
    @Test
    public void migration_1to2_data_integrity() throws IOException {
        SubjectDatabase db = (SubjectDatabase) testHelper.createDatabase(TEST_DB_NAME, 1);
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            String testTitle = AndroidTestFunctions.randomString(TEST_COUNT);
            if (!Objects.equals(testTitle, testUser)) {
                db.SubjectDao().insert(new NotesSubject(AndroidTestFunctions.randomString(TEST_COUNT),
                        AndroidTestFunctions.randomString(TEST_COUNT)));
            }
        }
        db.SubjectDao().insert(new NotesSubject(testUser, testContents));
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            String testTitle = AndroidTestFunctions.randomString(TEST_COUNT);
            if (!Objects.equals(testTitle, testUser)) {
                db.SubjectDao().insert(new NotesSubject(AndroidTestFunctions.randomString(TEST_COUNT),
                        AndroidTestFunctions.randomString(TEST_COUNT)));
            }
        }
        db.close();

        testHelper.runMigrationsAndValidate(TEST_DB_NAME, 2, true,
                NotesSubjectMigration.MIGRATION_1_2);

        SubjectDatabase afterDatabase = AndroidTestFunctions.getDatabaseAftMigration(testHelper,
                TEST_DB_NAME, NotesSubjectMigration.MIGRATION_1_2);
        Assert.assertEquals(afterDatabase.SubjectDao().search(TEST_DB_NAME).contents, testContents);
    }
}
