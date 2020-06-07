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

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Functions specifically used to convert from one type of variable to another. **/
public final class ConverterFunctions {
    private ConverterFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** The ISO-8601 compliant date and time format.  **/
    public static final SimpleDateFormat isoDateTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.ENGLISH);
    /** The standard date and time display format. **/
    public static final SimpleDateFormat standardDateTimeFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
    /** The standard date storage format. **/
    public static final SimpleDateFormat standardDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();

    /** Converts a date to a ISO-8601 compliant string. If the date is null, "null" is returned. **/
    @NonNull
    @TypeConverter
    public static String dateToString(Date original) {
        if (original == null) {
            return "null";
        } else {
            return isoDateTimeFormat.format(original);
        }
    }

    /** Converts a ISO-8601 compliant string to a date,
     * returns null if fails or if string is "null". **/
    @TypeConverter
    public static Date stringToDate(String original) {
        if (Objects.equals(original, "null")) return null;
        try {
            return isoDateTimeFormat.parse(original);
        } catch (ParseException e) {
            return null;
        }
    }

    /** Converts a single layer integer ArrayList to a string JSON array.
     * GSON was used for backwards compatibility and is more secure. **/
    static String singleIntArrayToJson(ArrayList<Integer> original) {
        return new Gson().toJson(original);
    }

    /** Converts an integer JSON array into a single layer ArrayList.
     * Returns null if the original array is invalid.
     * Returns an empty ArrayList if the original array is empty.
     * GSON was used for backwards compatibility and is more secure.**/
    @Nullable
    static ArrayList<Integer> jsonToSingleIntegerArray(String original) {
        Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
        if (isCorrectGson(original, listType)) {
            return new Gson().fromJson(original, listType);
        }
        return null;
    }

    /** Converts a list of NotesContent objects to a String.
     * The notes list is first converted into a double layered ArrayList,
     * which is then converted into a String. **/
    public static String notesListToString(@NonNull List<NotesContent> original) {
        ArrayList<ArrayList<String>> returnList = new ArrayList<>();
        for (NotesContent note: original) {
            // Each ArrayList would have 7 objects in the following order:
            // title, contents, last edited, lockedSalt, lockedPass, alertDate and alertCode
            // The date would be in the ISO Date time format instead of the old standardDateTimeFormat
            ArrayList<String> currentList = new ArrayList<>();
            currentList.add(note.noteTitle);
            currentList.add(ConverterFunctions.isoDateTimeFormat.format(note.lastEdited));
            currentList.add(note.noteContent);
            currentList.add(note.lockedPass);
            currentList.add(ConverterFunctions.isoDateTimeFormat.format(note.alertDate));
            currentList.add(String.valueOf(note.alertCode));
            returnList.add(currentList);
        }
        return new Gson().toJson(returnList);
    }

    /** Converts a String back to a list of NotesContent objects.
     * Returns null if the original array is invalid.
     * The String is first converted into a double layered ArrayList,
     * which is then converted back to a list of NotesContent objects.
     * This function will not update the note into the database and must be done separately. */
    public static ArrayList<NotesContent> stringToNotesList(SubjectDatabase database,
                                                            int subjectId, String original) {
        Type listType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
        if (isCorrectGson(original, listType)) {
            // Each ArrayList would have 7 objects in the following order:
            // title, contents, last edited, lockedSalt, lockedPass, alertDate and alertCode
            ArrayList<ArrayList<String>> importedContent = new Gson().fromJson(original, listType);
            ArrayList<NotesContent> returnList = new ArrayList<>();
            for (ArrayList<String> importedNote: importedContent) { // No checks for array size as error would be thrown
                try {
                    // Generate valid note ID
                    int noteId = DatabaseFunctions.generateValidId(database, DatabaseFunctions.ID_TYPE.NOTE);
                    // Special case for alertCode
                    Integer alertCode = importedNote.get(6) == null ? null : Integer.parseInt(importedNote.get(6));
                    NotesContent currentNote = new NotesContent(noteId, subjectId, importedNote.get(0),
                            importedNote.get(1), Objects.requireNonNull(isoDateTimeFormat.parse(importedNote.get(2))), importedNote.get(3),
                            importedNote.get(4), isoDateTimeFormat.parse(importedNote.get(5)), alertCode);
                    returnList.add(currentNote);
                } catch (NullPointerException | IndexOutOfBoundsException | ParseException | NumberFormatException e) {
                    Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Unable to parse " +
                            "notes of the imported file. The file may be corrupted.");
                }
            }
            return returnList;
        }
        return null;
    }

    /** Checks if a string is formatted in the JSON format and of the correct list type. **/
    private static boolean isCorrectGson(String Json, Type listType) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }

        try {
            new Gson().fromJson(Json, listType);
            return true;
        } catch (JsonParseException e) {
            return false;
        }
    }

    /** Takes in an integer
     * @param i and convert it to a byte array. **/
    @NonNull
    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);

        return result;
    }

    /** Takes in a byte array
     * @param original and convert it to an integer. **/
    public static int bytesToInt(@NonNull byte[] original) throws IndexOutOfBoundsException {
        return ((original[0] & 0xff) << 24) |
                ((original[1] & 0xff) << 16) |
                ((original[2] & 0xff) << 8) |
                (original[3] & 0xff);
    }

    /** Takes in a byte array
     * @param original and convert it into a hexadecimal string.
     * From https://stackoverflow.com/a/9855338 **/
    @NonNull
    static String bytesToHex(@NonNull byte[] original) {
        byte[] hexChars = new byte[original.length * 2];
        for (int j = 0; j < original.length; j++) {
            int v = original[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
