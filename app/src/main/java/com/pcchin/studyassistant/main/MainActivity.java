package com.pcchin.studyassistant.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.notes.NotesEditFragment;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // TODO: Implement status_view_recycler and status_edit_recycler

    public ActionBarDrawerToggle toggle;

    /* These strings are used when passing values to OnOptionsItemSelected
     * All vals usage:
     * NotesSubjectFragment - Val1 is subject title
     * NotesViewFragment - Val1 is subject title, Val2 is order of note in subject.
     * NotesEditFragment - If Val2 is null, Val1 is title of note in subject.
     * If Val2 is not null, then Val1 is the subject of the note, and Val2 is the index of the note.
     */
    public String activityVal1;
    public String activityVal2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayFragment(new MainFragment());

        // Set toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.m3_nav_open, R.string.m3_nav_close);
        GeneralFunctions.enableDrawer(true, this);
        navigationView.setNavigationItemSelectedListener(this);

        GeneralFunctions.updateNavView(this);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // When NotesSelectActivity is activated
            case R.id.n1_new_subj:
                SubjectDatabase subjectDatabase = Room.databaseBuilder(this,
                        SubjectDatabase.class, "notesSubject")
                        .allowMainThreadQueries().build();
                GeneralFunctions.showNewSubject(this, this, subjectDatabase);
                break;
            case R.id.n1_import:
                // TODO: Import
                break;

            // When NotesSubjectFragment is activated
            case R.id.n2_new_note:
                @SuppressLint("InflateParams") final View popupView = getLayoutInflater()
                        .inflate(R.layout.popup_new_title, null);
                AlertDialog newNoteDialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.n2_new_note)
                        .setView(popupView)
                        .setPositiveButton(android.R.string.ok, null)
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                // OnClickListeners implemented separately to prevent
                // dialog from being dismissed after button click
                newNoteDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        ((EditText) popupView.findViewById(R.id.popup_input)).setHint(R.string.title);
                        ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String popupInputText = ((EditText) popupView
                                                .findViewById(R.id.popup_input))
                                                .getText().toString();

                                        // Check if input is blank
                                        if (popupInputText.replaceAll("\\s+", "")
                                        .length() == 0) {
                                            ((TextView) popupView.findViewById(R.id.popup_error))
                                                    .setText(R.string.n2_error_note_title_empty);
                                        } else {
                                            // Edit new note
                                            dialog.dismiss();
                                            displayFragment(NotesEditFragment.newInstance(
                                                    popupInputText));
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
                newNoteDialog.show();
                break;

            case R.id.n2_export:
                // TODO: Export
                break;

            case R.id.n2_del:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.del)
                        .setMessage(R.string.n2_del_confirm)
                        .setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Delete subject
                                SubjectDatabase database = Room.databaseBuilder(MainActivity.this,
                                        SubjectDatabase.class, "notesSubject")
                                        .allowMainThreadQueries().build();
                                NotesSubject delTarget = database.SubjectDao().search(activityVal1);
                                if (delTarget != null) {
                                    database.SubjectDao().delete(delTarget);
                                }
                                database.close();
                                // Return to NotesSelectFragment
                                Toast.makeText(MainActivity.this,
                                        getString(R.string.n2_deleted), Toast.LENGTH_SHORT).show();
                                GeneralFunctions.updateNavView(MainActivity.this);
                                displayFragment(new NotesSelectFragment());
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                break;

            // When NotesViewFragment is activated
            case R.id.n3_edit:
                displayFragment(NotesEditFragment.newInstance(activityVal1, activityVal2));
                break;

            case R.id.n3_del:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.del)
                        .setMessage(R.string.n3_del_confirm)
                        .setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SubjectDatabase database = Room.databaseBuilder(MainActivity.this,
                                        SubjectDatabase.class, "notesSubject")
                                        .allowMainThreadQueries().build();
                                NotesSubject currentSubject = database.SubjectDao().search(activityVal1);
                                if (activityVal1 != null) {
                                    // Check if contents is valid
                                    ArrayList<ArrayList<String>> contents = GeneralFunctions.jsonToArray(currentSubject.contents);
                                    if (contents != null) {
                                        // Variable has to be used to prevent contents.remove(int)
                                        // from error as type "Integer" is different from type "int"
                                        int index = Integer.valueOf(activityVal2);
                                        if (index < contents.size()) {
                                            contents.remove(index);
                                        }
                                    } else {
                                        contents = new ArrayList<>();
                                    }
                                    // Update value in database
                                    currentSubject.contents = GeneralFunctions.arrayToJson(contents);
                                    database.SubjectDao().update(currentSubject);
                                    database.close();
                                    displayFragment(NotesSubjectFragment.newInstance(activityVal1));
                                } else {
                                    // In case the note somehow doesn't have a subject
                                    displayFragment(new NotesSelectFragment());
                                }
                                Toast.makeText(MainActivity.this, getString(
                                        R.string.n3_deleted), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Note: due to need of dynamic menu, most buttons have been moved to
        // GeneralFunctions.updateNavView(MainActivity activity)

        // Handle navigation view item clicks here.
        if (item.getItemId() == R.id.m3_home) {
            displayFragment(new MainFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void displayFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.base, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
        hideKeyboard();
    }

    public void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    private void hideKeyboard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
