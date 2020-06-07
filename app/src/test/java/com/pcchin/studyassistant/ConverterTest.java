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

package com.pcchin.studyassistant;

import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.functions.ConverterFunctions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

/** Test the converters and whether they are working. **/
public class ConverterTest {
    private static int TEST_COUNT;

    /** Default constructor. **/
    public ConverterTest() {
        if (BuildConfig.IS_LOCAL) {
            TEST_COUNT = 5000;
        } else {
            TEST_COUNT = 1000;
        }
    }

    /** Sets the time of the converter to standardize it. **/
    @Before
    public void setTime() {
        // Set calendar to UTC time to prevent errors
        Instant.now(Clock.fixed(Instant.parse("2018-08-22T10:00:00Z"), ZoneOffset.UTC));
    }

    /** Test a specific parsed time for each type. **/
    @Test
    public void testParseTime() throws ParseException {
        // Test date first
        String original = "29/02/2004";
        Date response = ConverterFunctions.parseTime(original, ConverterFunctions.TimeFormat.DATE);
        Assert.assertNotNull(response);
        // Test date time
        original = "01/01/2048 13:14:15";
        response = ConverterFunctions.parseTime(original, ConverterFunctions.TimeFormat.DATETIME);
        Assert.assertNotNull(response);
        // Test ISO date time
        original = "2007-04-06T08:19:25.408+0730";
        response = ConverterFunctions.parseTime(original, ConverterFunctions.TimeFormat.ISO);
        Assert.assertNotNull(response);
    }

    /** Test a specific formatted time for each type.
     * Since the SimpleDateTime is based on the locale of the device,
     * its time zone would need to be mocked.
     * Thus, there is no way to check for the accuracy of the time set for now.**/
    @Test
    public void testFormatTime() {
        // Date used for the test (May 7th 1945, 17:01:12.00 EST)
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EST"), Locale.ENGLISH);
        calendar.set(1945, 4, 6, 17, 1, 12);
        calendar.set(Calendar.MILLISECOND, 0);
        Date original = new Date();
        original.setTime(calendar.getTimeInMillis());
        // Test date
        String formatted = ConverterFunctions.formatTime(original, ConverterFunctions.TimeFormat.DATE);
        Assert.assertNotNull(formatted);
        // Test date time
        formatted = ConverterFunctions.formatTime(original, ConverterFunctions.TimeFormat.DATETIME);
        Assert.assertNotNull(formatted);
        // Test ISO
        formatted = ConverterFunctions.formatTime(original, ConverterFunctions.TimeFormat.ISO);
        Assert.assertNotNull(formatted);
    }

    /** Test the conversion between formatting and parsing time. **/
    @Test
    public void testFormatParseTime() throws ParseException {
        Random rand = new Random();
        Date currentDate = new Date();
        for (int i = 0; i < TEST_COUNT / 3; i++) {
            currentDate.setTime(rand.nextLong());
            String formattedDate = ConverterFunctions.formatTime(currentDate, ConverterFunctions.TimeFormat.DATE),
                    formattedDateTime = ConverterFunctions.formatTime(currentDate, ConverterFunctions.TimeFormat.DATETIME),
                    formattedISO = ConverterFunctions.formatTime(currentDate, ConverterFunctions.TimeFormat.ISO);
            ConverterFunctions.parseTime(formattedDate, ConverterFunctions.TimeFormat.DATE);
            ConverterFunctions.parseTime(formattedDateTime, ConverterFunctions.TimeFormat.DATETIME);
            ConverterFunctions.parseTime(formattedISO, ConverterFunctions.TimeFormat.ISO);
        }
    }

    /** Test the conversion between an ArrayList of NotesContent and a Gson-encoded String. **/
    @Test
    public void testNotesListConversion() {
        // Initialize variables
        Random rand = new Random();
        ArrayList<NotesContent> originalList, convertedList;
        List<Integer> notesIdList = new ArrayList<>();
        int fakeId, subjectId;

        // Populate notesIdList with mock notes
        for (int j = 0; j < rand.nextInt(100); j++) {
            fakeId = rand.nextInt();
            while (notesIdList.contains(fakeId)) fakeId = rand.nextInt();
            notesIdList.add(fakeId);
        }

        for (int i = 0; i < rand.nextInt(20); i++) {
            subjectId = rand.nextInt();
            originalList = TestFunctions.generateRandomNotes(TEST_COUNT, subjectId);
            convertedList = ConverterFunctions.stringToNotesList(notesIdList, subjectId,
                    ConverterFunctions.notesListToString(originalList));
            assert convertedList != null;
            TestFunctions.customNotesListAssert(originalList, convertedList);
        }
    }

    /** Test the conversion between ArrayList<Integer> and a JSON String. **/
    @Test
    public void testIntArrayJson() {
        Random rand = new Random();
        ArrayList<Integer> original, converted;
        int targetInt = rand.nextInt(TEST_COUNT * 10);
        while (targetInt < 10000) {
            targetInt = rand.nextInt(TEST_COUNT * 10);
        }
        for (int i = 0; i < targetInt; i++) {
            original = new ArrayList<>();
            for (int j = 0; j < targetInt / 10; j++) {
                original.add(rand.nextInt());
            }
            converted = ConverterFunctions.jsonToSingleIntegerArray(ConverterFunctions.singleIntArrayToJson(original));
            Assert.assertEquals(converted, original);
        }
    }

    /** Test int to byte[] and vice versa. **/
    @Test
    public void testIntByte() {
        Random rand = new Random();
        int currentInt, convertedInt;
        int targetInt = rand.nextInt(TEST_COUNT * 10);
        while (targetInt < 10000) {
            targetInt = rand.nextInt(TEST_COUNT * 10);
        }
        for (int i = 0; i < targetInt; i++) {
            currentInt = rand.nextInt();
            convertedInt = ConverterFunctions.bytesToInt(ConverterFunctions.intToBytes(currentInt));
            Assert.assertEquals(currentInt, convertedInt);
        }
    }
}
