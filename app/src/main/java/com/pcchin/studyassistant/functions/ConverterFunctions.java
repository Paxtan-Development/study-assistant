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

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/** Functions specifically used to convert from one type of variable to another. **/
public final class ConverterFunctions {
    /** Constructor made private to simulate static class. **/
    private ConverterFunctions() {
        // Constructor made private to simulate static class.
    }

    /** The ISO-8601 compliant date and time format.  **/
    private static final SimpleDateFormat isoDateTimeFormat =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.ENGLISH);
    /** The standard date and time display format. **/
    public static final SimpleDateFormat standardDateTimeFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
    /** The standard date storage format. **/
    public static final SimpleDateFormat standardDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes();

    /** Converts a date to a ISO-8601 compliant string. If the date is null, "null" is returned. **/
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

    /** Converts a single layer ArrayList to a string JSON array.
     * GSON was used for backwards compatibility and is more secure. **/
    @TypeConverter
    public static String singleStringArrayToJson(ArrayList<String> original) {
        return new Gson().toJson(original);
    }

    /** Converts a string JSON array into a single layer ArrayList.
     * Returns null if the original array is invalid.
     * Returns an empty ArrayList if the original array is empty.
     * GSON was used for backwards compatibility and is more secure.**/
    @TypeConverter
    @Nullable
    public static ArrayList<String> jsonToSingleStringArray(String original) {
        if (isJson(original)) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
            return new Gson().fromJson(original, listType);
        }
        return null;
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
    @TypeConverter
    @Nullable
    static ArrayList<Integer> jsonToSingleIntegerArray(String original) {
        if (isJson(original)) {
            Type listType = new TypeToken<ArrayList<Integer>>() {}.getType();
            return new Gson().fromJson(original, listType);
        }
        return null;
    }

    /** Converts a double layer ArrayList to a string JSON array.
     * GSON was used for backwards compatibility and is more secure. **/
    @TypeConverter
    public static String doubleArrayToJson(ArrayList<ArrayList<String>> original) {
        return new Gson().toJson(original);
    }

    /** Converts a string JSON array into a double layer ArrayList.
     * Returns null if the original array is invalid.
     * Returns an empty ArrayList if the original array is empty.
     * GSON was used for backwards compatibility and is more secure.**/
    @TypeConverter
    @Nullable
    public static ArrayList<ArrayList<String>> doubleJsonToArray(String original) {
        if (isJson(original)) {
            Type listType = new TypeToken<ArrayList<ArrayList<String>>>() {}.getType();
            return new Gson().fromJson(original, listType);
        }
        return null;
    }

    /** Checks if a string is formatted in the JSON format. **/
    private static boolean isJson(String Json) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    /** Takes in an integer
     * @param i and convert it to a byte array. **/
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
    public static int bytesToInt(byte[] original) {
        return ((original[0] & 0xff) << 24) |
                ((original[1] & 0xff) << 16) |
                ((original[2] & 0xff) << 8) |
                (original[3] & 0xff);
    }

    /** Takes in a byte array
     * @param original and convert it into a hexadecimal string.
     * From https://stackoverflow.com/a/9855338 **/
    static String bytesToHex(byte[] original) {
        byte[] hexChars = new byte[original.length * 2];
        for (int j = 0; j < original.length; j++) {
            int v = original[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
