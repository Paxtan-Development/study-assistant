package com.pcchin.studyassistant.main;

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

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.project.ProjectSelectFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // TODO: Implement status_view_recycler and status_edit_recycler

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
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.m3_nav_open, R.string.m3_nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
                // TODO: Create subject
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

    private void displayFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.base, fragment);
        fragmentTransaction.commit();
    }

    private void displayExit() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.exit)
                .setMessage(R.string.m3_exit_confirm)
                .setCancelable(false)
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
