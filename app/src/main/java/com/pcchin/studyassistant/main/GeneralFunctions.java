package com.pcchin.studyassistant.main;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
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
import java.util.List;

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
                .inflate(R.layout.popup_new_title, null);
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
                ((EditText) popupView.findViewById(R.id.popup_input)).setHint(R.string.n1_subject_title);
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String inputText = ((EditText) popupView
                                        .findViewById(R.id.popup_input)).getText().toString();
                                TextView errorText = popupView.findViewById(R.id.popup_error);

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
                                    GeneralFunctions.updateNavView(activity);
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

    /** Updates the NavigationView in MainActivity **/
    public static void updateNavView(@NonNull final MainActivity activity) {
        NavigationView navView = activity.findViewById(R.id.nav_view);
        // Nuke menu
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.menu_main_drawer);
        Menu currentMenu = navView.getMenu();

        // Add subjects
        final SubjectDatabase subjectDatabase = Room.databaseBuilder(activity, SubjectDatabase.class,
                "notesSubject").allowMainThreadQueries().build();
        SubMenu subjMenu = currentMenu.addSubMenu(R.string.notes);
        List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        for (final NotesSubject subject: subjectList) {
            MenuItem subjItem = subjMenu.add(subject.title);
            // This is to prevent menu items from disappearing
            subjItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    // Opens subject when clicked
                    activity.closeDrawer();
                    activity.displayFragment(NotesSubjectFragment.newInstance(subject.title));
                    return true;
                }
            });
        }

        // Add New Subject button
        MenuItem newSubj = subjMenu.add(R.string.m3_new_subject);
        newSubj.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activity.closeDrawer();
                showNewSubject(activity, activity, subjectDatabase);
                return true;
            }
        });

        // Add Import Subject button
        MenuItem subjImport = subjMenu.add(R.string.m3_data_import);
        subjImport.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: Import
                activity.closeDrawer();
                return false;
            }
        });

        // Add projects
        SubMenu projMenu = currentMenu.addSubMenu(R.string.projects);
        // TODO: Add projects

        // Add New Project Button
        MenuItem newProj = projMenu.add(R.string.m3_new_project);
        newProj.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: New Project
                activity.closeDrawer();
                return false;
            }
        });

        // Add Import Subject button
        MenuItem projImport = projMenu.add(R.string.m3_data_import);
        projImport.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // TODO: Import
                activity.closeDrawer();
                return false;
            }
        });

        // Add exit button
        MenuItem exitItem = currentMenu.add(R.string.exit);
        exitItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activity.closeDrawer();
                displayExit(activity);
                return true;
            }
        });

        // Add about button
        MenuItem aboutItem = currentMenu.add(R.string.m_about);
        aboutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activity.closeDrawer();
                activity.displayFragment(new AboutFragment());
                return true;
            }
        });

        // Add settings button
        MenuItem settingsItem = currentMenu.add(R.string.m_settings);
        settingsItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                activity.closeDrawer();
                activity.displayFragment(new SettingsFragment());
                return true;
            }
        });
    }

    /** Displays the exit dialog **/
    static void displayExit(final MainActivity activity) {
        new android.support.v7.app.AlertDialog.Builder(activity)
                .setTitle(R.string.exit)
                .setMessage(R.string.m3_exit_confirm)
                .setIcon(R.mipmap.ic_launcher_round)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        })
                .create().show();
    }
}
