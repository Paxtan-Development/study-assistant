package com.pcchin.studyassistant.functions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;

import androidx.fragment.app.Fragment;
import androidx.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.AboutFragment;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** General functions used throughout the app **/
public class GeneralFunctions {
    /** The standard date and time storage format used in the app. **/
    public static final SimpleDateFormat standardDateTimeFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.ENGLISH);

    /** The standard date storage format used in the app. **/
    public static final SimpleDateFormat standardDateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

    /** Shows the dialog to add a new subject to the notes list **/
    public static void showNewSubject(Context context, @NonNull final MainActivity activity,
                               final SubjectDatabase database) {
        @SuppressLint("InflateParams") final View popupView = activity.getLayoutInflater()
                .inflate(R.layout.popup_edittext, null);
        AlertDialog subjectDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.n1_new_subject)
                .setView(popupView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setCancelable(false)
                .create();
        // OnClickListeners implemented separately to prevent dialog from being dismissed after button click
        subjectDialog.setOnShowListener(dialog -> {
            ((EditText) popupView.findViewById(R.id.popup_input)).setHint(R.string.n1_subject_title);
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String inputText = ((EditText) popupView
                                .findViewById(R.id.popup_input)).getText().toString();
                        TextView errorText = popupView.findViewById(R.id.popup_error);

                        // Preliminary checks if subject name is taken or is empty
                        if (inputText.replaceAll("\\s+", "").length() == 0) {
                            errorText.setText(R.string.n_error_subject_empty);
                        } else if (database.SubjectDao().search(inputText) != null) {
                            errorText.setText(R.string.n1_error_subject_exists);
                        } else {
                            // Create subject
                            database.SubjectDao().insert(
                                    new NotesSubject(inputText,
                                                    new ArrayList<>(),
                                                    NotesSubject.SORT_ALPHABETICAL_ASC));
                            database.close();
                            activity.displayFragment(
                                    NotesSubjectFragment.newInstance(inputText));
                            dialog.dismiss();
                            GeneralFunctions.updateNavView(activity);
                        }
                    });
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> dialog.dismiss());
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
                "notesSubject")
                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                .allowMainThreadQueries().build();
        SubMenu subjMenu = currentMenu.addSubMenu(R.string.notes);
        List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        for (final NotesSubject subject: subjectList) {
            MenuItem subjItem = subjMenu.add(subject.title);
            // This is to prevent menu items from disappearing
            subjItem.setOnMenuItemClickListener(item -> {
                // Opens subject when clicked
                activity.closeDrawer();
                activity.displayFragment(NotesSubjectFragment.newInstance(subject.title));
                return true;
            });
        }

        // Add New Subject button
        MenuItem newSubj = subjMenu.add(R.string.m3_new_subject);
        newSubj.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            showNewSubject(activity, activity, subjectDatabase);
            return true;
        });

        // Add Import Subject button
        MenuItem subjImport = subjMenu.add(R.string.m3_data_import);
        subjImport.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            FileFunctions.importSubject();
            return false;
        });

        // Add projects
        SubMenu projMenu = currentMenu.addSubMenu(R.string.projects);
        // TODO: Add projects

        // Add New Project Button
        MenuItem newProj = projMenu.add(R.string.m3_new_project);
        newProj.setOnMenuItemClickListener(item -> {
            // TODO: New Project
            activity.closeDrawer();
            return false;
        });

        // Add Import Project button
        MenuItem projImport = projMenu.add(R.string.m3_data_import);
        projImport.setOnMenuItemClickListener(item -> {
            // TODO: Import projects
            activity.closeDrawer();
            return false;
        });

        // Add subMenu for other buttons
        SubMenu otherMenu = currentMenu.addSubMenu(R.string.m3_others);

        // Add about button
        MenuItem aboutItem = otherMenu.add(R.string.m_about);
        aboutItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.displayFragment(new AboutFragment());
            return true;
        });

        // Add exit button
        MenuItem exitItem = otherMenu.add(R.string.exit);
        exitItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            displayExit(activity);
            return true;
        });
    }

    /** Exits the app.**/
    public static void displayExit(Activity activity) {
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /** Reloads a fragment. **/
    public static void reloadFragment(@NonNull Fragment target) {
        if (target.getFragmentManager() != null) {
            target.getFragmentManager().beginTransaction()
                    .detach(target)
                    .attach(target).commit();
        }
    }

}
