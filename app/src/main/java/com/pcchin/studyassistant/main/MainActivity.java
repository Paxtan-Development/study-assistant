package com.pcchin.studyassistant.main;

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
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;
import com.pcchin.studyassistant.project.ProjectSelectFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // TODO: Implement status_view_recycler and status_edit_recycler

    public ActionBarDrawerToggle toggle;
    // These strings are used when passing values to OnOptionsItemSelected
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
    }

    @Override
    public void onBackPressed() {
        // TODO: Kept sample for reference
        /*
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        */

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
            // When NotesSubjectFragment is activated
            case R.id.n2_new_note:
                // TODO: Title dialog
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
                // TODO: Edit note
                break;
            case R.id.n3_del:
                // TODO: Delete dialog
                break;
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Set up database
        SubjectDatabase database = Room.databaseBuilder(this, SubjectDatabase.class,
                "notesSubject").allowMainThreadQueries().build();

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id) {
            case (R.id.m3_home):
                displayFragment(new MainFragment());
                break;
            case (R.id.m3_notes):
                displayFragment(new NotesSelectFragment());
                break;
            case (R.id.m3_notes_create):
                GeneralFunctions.showNewSubject(this, this, database);
                if (database.isOpen()) {
                    database.close();
                }
                break;
            case (R.id.m3_notes_import):
                // TODO: Import subject
                break;
            case (R.id.m3_project):
                displayFragment(new ProjectSelectFragment());
                break;
            case (R.id.m3_project_create):
                // TODO: Create project
                break;
            case (R.id.m3_project_import):
                // TODO: Import project
                break;
            case (R.id.m3_exit):
                displayExit();
                break;
            case (R.id.m3_about):
                displayFragment(new AboutFragment());
                break;
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
    }

    private void displayExit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit)
                .setMessage(R.string.m3_exit_confirm)
                .setIcon(R.mipmap.ic_launcher_round)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
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
