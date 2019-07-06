package com.pcchin.studyassistant.functions;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

/** Functions specifically used to convert from one type of variable to another. **/
public class ConverterFunctions {

    /** Converts an ArrayList to a string JSON array.
     * GSON was used for backwards compatibility and is more secure. **/
    @TypeConverter
    public static String arrayToJson(ArrayList<ArrayList<String>> original) {
        return new Gson().toJson(original);
    }

    /** Converts a string JSON array into an ArrayList.
     * Returns null if the original array is invalid.
     * Returns an empty ArrayList if the original array is empty.
     * GSON was used for backwards compatibility and is more secure.**/
    @TypeConverter
    @Nullable
    public static ArrayList<ArrayList<String>> jsonToArray(String original) {
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
}
