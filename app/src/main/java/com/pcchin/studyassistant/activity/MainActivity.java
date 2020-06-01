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

package com.pcchin.studyassistant.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectSubject;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectZip;
import com.pcchin.studyassistant.fragment.main.MainFragment;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragment;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragmentClick;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragment;
import com.pcchin.studyassistant.fragment.project.settings.ProjectSettingsFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.preference.PreferenceString;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public BottomNavigationView bottomNavView;
    public ViewPager pager;
    public Fragment currentFragment;

    // Values that are only used when processing the ID
    private String projectID;
    private String id2;
    private boolean isMember;

    /** Initializes activity. Sets up toolbar and drawer.  **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MainActivityCreate(MainActivity.this).onCreate(savedInstanceState);
    }

    /** Delegates the items that are selected on the menu to the respective fragments. **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        MainActivityOptions.processOption(item.getItemId(), currentFragment);
        return true;
    }

    /** Handles button presses in the drawer.
     * Due to need for dynamic menu, most buttons have been moved to
     * GeneralFunctions.updateNavView(MainActivity activity)
     * @see UIFunctions **/
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

    /** Delegates each onBackPressed to each Fragment **/
    @Override
    public void onBackPressed() {
        if (!closeDrawer()) {
            if (currentFragment instanceof NotesViewFragment) {
                // As NotesViewFragment is inside a PagerAdapter, it does not intercept
                // the onBackPressed, hence its a special case
                ((NotesViewFragment) currentFragment).onBackPressed();
            } else {
                closeDrawer();
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.base);
                if (!(fragment instanceof ExtendedFragment) || !((ExtendedFragment) fragment).onBackPressed()) {
                    super.onBackPressed();
                }
            }
        }
    }

    /** Returns the check permission result for all runtime permissions needed. **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ActivityConstants.EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, R.string.error_write_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** External intent is returned here from picking a file from the following:
     * @see ImportSubjectZip
     * @see ImportSubjectSubject
     * The file would be sent back to a new ImportSubject to be imported. **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getData() != null) {
            String targetFile = FileFunctions.getRealPathFromUri(MainActivity.this, data.getData());
            boolean imagePicked = false;
            if (targetFile == null) {
                targetFile = ImagePicker.Companion.getFilePath(data);
                imagePicked = true;
            }
            processFileRequest(requestCode, targetFile, imagePicked);
        }
    }

    /** Process the file request received. **/
    private void processFileRequest(int requestCode, String targetFile, boolean imagePicked) {
        if (requestCode == ActivityConstants.SELECT_ZIP_FILE) {
            // Select zip file
            new ImportSubjectZip(MainActivity.this).importZipConfirm(targetFile);
        } else if (requestCode == ActivityConstants.SELECT_SUBJECT_FILE) {
            // Select .subject file
            if (targetFile != null && targetFile.endsWith(".subject")) {
                new ImportSubjectSubject(MainActivity.this).importSubjectFile(targetFile);
            } else {
                Toast.makeText(MainActivity.this, R.string.not_subject_file, Toast.LENGTH_SHORT).show();
            }
        } else if (imagePicked && targetFile != null) {
            updateIcon(targetFile);
        } else if (targetFile != null) {
            // TODO: Import media to project
        } else {
            // File could not be processed.
            Toast.makeText(MainActivity.this, R.string.file_error, Toast.LENGTH_SHORT).show();
            Log.e(ActivityConstants.LOG_APP_NAME,
                    String.format("File Error: The intent received with request code %s is unable to be processed", requestCode));
        }
    }

    /** Updates the icon of the project specified earlier. **/
    private void updateIcon(String targetFile) {
        try {
            String iconPath = DatabaseFunctions.getProjectIconPath(MainActivity.this, projectID);
            FileFunctions.copyFile(new File(targetFile), new File(iconPath));
            new Thread(() -> {
                ProjectDatabase database = DatabaseFunctions.getProjectDatabase(MainActivity.this);
                ProjectData project = database.ProjectDao().searchByID(projectID);
                project.hasIcon = true;
                database.ProjectDao().update(project);
                runOnUiThread(this::startProjectSettings);
            }).start();
            Toast.makeText(MainActivity.this, R.string.p3_general_icon_updated, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, R.string.file_error, Toast.LENGTH_SHORT).show();
            Log.e(ActivityConstants.LOG_APP_NAME, String.format("File Error: Unable to be update " +
                    "the icon of project ID %s from targetFile %s", projectID, targetFile));
            e.printStackTrace();
        }
    }

    /** Go to the settings page if it is not at the settings for the imported project. **/
    private void startProjectSettings() {
        if (currentFragment instanceof ProjectSettingsFragment
                && Objects.equals(((ProjectSettingsFragment) currentFragment)
                .project.projectID, projectID)) {
            ((ProjectSettingsFragment) currentFragment).displayPreference(PreferenceString.PREF_MENU_GENERAL);
        } else {
            // Start the settings page for that project
            safeOnBackPressed();
            displayFragment(ProjectSettingsFragment.newInstance(projectID, id2, isMember));
            ((ProjectSettingsFragment) currentFragment).displayPreference(PreferenceString.PREF_MENU_GENERAL);
        }
    }

    /** Displays the fragment that is needed to be displayed.
     * Keyboard will be hidden between fragments **/
    public void displayFragment(Fragment fragment) {
        // Hides bottomNavView if the project comes from a project fragment
        // and to a non-project fragment
        if (!MainActivityFunctions.fragmentHasBottomNavView(fragment)) {
            bottomNavView.setVisibility(View.GONE);
        }

        // Display the fragment
        // The lines need to be executed in this order to allow a smooth transition
        getSupportFragmentManager().beginTransaction().replace(R.id.base, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        if (pager.getAdapter() != null) pager.setAdapter(null);
        pager.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
        pager.setVisibility(View.GONE);
        findViewById(R.id.base).setVisibility(View.VISIBLE);
        currentFragment = fragment;
        new MainActivityFunctions(MainActivity.this).hideKeyboard();
    }

    /** Displays the notes for the subject through a custom PageAdaptor.
     * Keyboard will be hidden between the transition. **/
    public void displayNotes(String subject, int size) {
        if (currentFragment != null && !(currentFragment instanceof NotesViewFragment)) {
            // Removes last fragment from the normal container if its not NotesViewFragment
            // as NotesViewFragment is not displayed using the normal container
            // This is to remove the menu from the bottom container and prevent double onBackPressed
            getSupportFragmentManager().beginTransaction().remove(currentFragment).commit();
        }
        FragmentStatePagerAdapter baseAdapter = new MainActivityFunctions(MainActivity.this).getNoteAdapter(subject, size);
        // Updates currentFragment to the current item
        ViewPager.OnPageChangeListener baseAdapterPageChanger = new MainActivityFunctions(MainActivity.this).getNoteAdapterPageChanger(baseAdapter);
        pager.setAdapter(baseAdapter);
        pager.addOnPageChangeListener(baseAdapterPageChanger);
        new MainActivityFunctions(MainActivity.this).fadeToNote();
    }

    /** Set the info of the project based on the given project info. **/
    public void setProjectInfo(String projectID, String id2, boolean isMember) {
        this.projectID = projectID;
        this.id2 = id2;
        this.isMember = isMember;
    }

    /** Closes the navigation drawer.
     * Returns whether the drawer is closed. **/
    public boolean closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }

    /** Triggers the onBackPressed for key fragments (eg. when files are not saved)
     * when a function redirects to an external app/fragment. **/
    public void safeOnBackPressed() {
        if (currentFragment instanceof NotesEditFragment) {
            new NotesEditFragmentClick((NotesEditFragment) currentFragment).onSavePressed();
        }
    }
}
