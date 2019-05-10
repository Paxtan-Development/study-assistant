package com.pcchin.studyassistant.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

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
    public static String arrayToJson(ArrayList<ArrayList<String>> original) {
        return new Gson().toJson(original);
    }

    /** Converts a string JSON array into an ArrayList.
     * If the original array is invalid, it would return null,
     * wheres if the original array is empty, an empty ArrayList would be returned. **/
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

    /** Show the dialog to add a new subject to the notes list **/
    public static void showNewSubject(Context context, @NonNull final MainActivity activity,
                                      final SubjectDatabase database) {
        @SuppressLint("InflateParams") final View popupView = activity.getLayoutInflater()
                .inflate(R.layout.n1_popup_new_subject, null);
        AlertDialog subjectDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.n1_new_subject)
                .setView(popupView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .create();
        // OnClickListeners implemented separately to prevent dialog from being dismissed after button click
        subjectDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String inputText = ((EditText) popupView
                                        .findViewById(R.id.n1_popup_input)).getText().toString();
                                TextView errorText = popupView.findViewById(R.id.n1_popup_error);

                                // Preliminary checks if subject name is taken or is empty
                                if (inputText.replaceAll("\\s+", "").length() == 0) {
                                    errorText.setText(R.string.n1_error_subject_empty);
                                } else if (database.SubjectDao().search(inputText) != null) {
                                    errorText.setText(R.string.n1_error_subject_exists);
                                } else {
                                    // Create subject
                                    database.SubjectDao().insert(
                                            new NotesSubject(inputText,
                                                    GeneralFunctions.arrayToJson(
                                                            new ArrayList<ArrayList<String>>())));
                                    database.close();
                                    activity.displayFragment(
                                            NotesSubjectFragment.newInstance(inputText));
                                    dialog.dismiss();
                                }
                            }
                        });
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
            }
        });
        subjectDialog.show();
    }

    /** Change whether drawer is enabled **/
    public static void enableDrawer(boolean enabled, @NonNull MainActivity activity) {
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        if (enabled) {
            drawer.addDrawerListener(activity.toggle);
        } else {
            drawer.removeDrawerListener(activity.toggle);
        }
        activity.toggle.syncState();
    }
}
