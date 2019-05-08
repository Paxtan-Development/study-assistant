package com.pcchin.studyassistant.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class GeneralFunctions {
    /** Returns a string of text from specific text file in the assets folder **/
    @SuppressWarnings("SameParameterValue")
    @NonNull
    static String getReadTextFromAssets(@NonNull Context context, String textFileName) {
        String text;
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(textFileName);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((text = bufferedReader.readLine()) != null) {
                stringBuilder.append(text);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /** Converts an ArrayList to a string JSON array. **/
    public static String arrayToJson(ArrayList<String> original) {
        return new Gson().toJson(original);
    }

    /** Converts a string JSON array into an ArrayList.
     * If the original array is invalid, it would return null,
     * wheres if the original array is empty, an empty ArrayList would be returned. **/
    @Nullable
    public static ArrayList<String> jsonToArray(String original) {
        if (isJson(original)) {
            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
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
}
