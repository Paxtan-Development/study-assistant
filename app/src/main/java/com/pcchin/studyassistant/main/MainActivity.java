/*
 * Copyright 2019 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.main;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.misc.ExtendedFragment;
import com.pcchin.studyassistant.notes.NotesEditFragment;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.NotesViewFragment;
import com.pcchin.studyassistant.notes.misc.ImportSubject;
import com.pcchin.studyassistant.project.ProjectInfoFragment;
import com.pcchin.studyassistant.project.ProjectSelectFragment;
import com.pcchin.studyassistant.project.member.ProjectMemberFragment;
import com.pcchin.studyassistant.project.role.ProjectRoleFragment;
import com.pcchin.studyassistant.project.status.ProjectStatusFragment;
import com.pcchin.studyassistant.project.task.ProjectTaskFragment;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public BottomNavigationView bottomNavView;
    public ViewPager pager;
    private Fragment currentFragment;
    // Constants used across fragments
    public static final String SHAREDPREF_APP_UPDATE_PATH = "AppUpdatePath";
    public static final String SHAREDPREF_GITLAB_RELEASE_JSON = "gitlabReleasesJson";
    public static final String SHAREDPREF_LAST_UPDATE_CHECK = "lastUpdateCheck";
    public static final String INTENT_VALUE_DISPLAY_UPDATE = "displayUpdate";
    public static final String INTENT_VALUE_START_FRAGMENT = "startFragment";
    public static final String INTENT_VALUE_REQUEST_CODE = "requestCode";
    public static final String INTENT_VALUE_SUBJECT = "subject";
    public static final String INTENT_VALUE_MESSAGE = "message";
    public static final String INTENT_VALUE_TITLE = "title";
    public static final String DATABASE_NOTES = "notesSubject";
    public static final String DATABASE_PROJECT = "projectDatabase";
    public static final String LOG_APP_NAME = "StudyAssistant";

    private static final int EXTERNAL_STORAGE_PERMISSION = 200;
    public static final int EXTERNAL_STORAGE_READ_PERMISSION = 201;
    public static final int SELECT_ZIP_FILE = 300;
    public static final int SELECT_SUBJECT_FILE = 301;

    /** Initializes activity. Sets up toolbar and drawer.  **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if app is opened to process files
        Uri intentUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (intentUri != null) {
            String receiveFilePath = FileFunctions.getRealPathFromUri(this, intentUri);
            // Check if file type matches the required file types
            if (receiveFilePath.endsWith(".subject")) {
                new ImportSubject(this).importSubjectFile(receiveFilePath);
            } else if (receiveFilePath.endsWith(".zip") || receiveFilePath.endsWith(".ZIP")
                || receiveFilePath.endsWith(".Zip")) {
                new ImportSubject(this).importZipConfirm(receiveFilePath);
            } else {
                Toast.makeText(this, R.string.error_file_format_incorrect, Toast.LENGTH_SHORT).show();
            }
        }
        pager = findViewById(R.id.base_pager);
        bottomNavView = findViewById(R.id.bottom_nav);

        // First time starting the app
        if (savedInstanceState == null) {
            displayFragment(new MainFragment());

            // Hide bottom navigation view
            bottomNavView.setVisibility(View.GONE);
            // Set up notification channels
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mainChannel = new NotificationChannel(getString(
                        R.string.notif_channel_notes_ID), getString(R.string.notif_channel_notes),
                        NotificationManager.IMPORTANCE_DEFAULT);
                mainChannel.setDescription(getString(R.string.notif_channel_notes_desc));
                NotificationChannel updateChannel = new NotificationChannel(getString(
                        R.string.notif_channel_update_ID), getString(R.string.notif_channel_update),
                        NotificationManager.IMPORTANCE_LOW);
                updateChannel.setDescription(getString(R.string.notif_channel_update_desc));
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(mainChannel);
                    manager.createNotificationChannel(updateChannel);
                }
            }

            // Get permission to read and write files
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                    .checkSelfPermission(this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION);
            }

            // Set up Admin & Member roles in database for projects
            // TODO: Remove statement once completed
            if (BuildConfig.DEBUG) {
                new Handler().post(() -> {
                    ProjectDatabase projectDatabase = Room.databaseBuilder(this,
                            ProjectDatabase.class, DATABASE_PROJECT)
                            .fallbackToDestructiveMigrationFrom(1, 2)
                            .allowMainThreadQueries().build();
                    RoleData admin = projectDatabase.RoleDao().searchByID("admin");
                    if (admin == null) {
                        admin = new RoleData("admin", "", "Admin", "", "");
                        admin.canDeleteProject = true;
                        admin.canModifyInfo = true;
                        admin.canModifyOtherTask = true;
                        admin.canModifyOtherUser = true;
                        admin.canModifyOwnTask = true;
                        admin.canModifyRole = true;
                        admin.canSetPassword = true;
                        admin.canViewOtherUser = true;
                        admin.canViewRole = true;
                        admin.canViewTask = true;
                        admin.canViewMedia = true;
                        projectDatabase.RoleDao().insert(admin);
                    }
                    projectDatabase.close();
                });
            }

            // Delete any past export files
            new Handler().post(() -> {
                String outputFileName = getFilesDir().getAbsolutePath() + "/temp";
                File apkInstallDir = new File(outputFileName);
                if (apkInstallDir.exists() && apkInstallDir.isDirectory()) {
                    // Deletes all children in the folder
                    File[] dirFiles = apkInstallDir.listFiles();
                    if (dirFiles != null) {
                        for (File child : dirFiles) {
                            FileFunctions.deleteDir(child);
                        }
                    }
                } else if (!apkInstallDir.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    apkInstallDir.mkdir();
                }

                SharedPreferences sharedPref = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                String pastUpdateFilePath = sharedPref.getString(SHAREDPREF_APP_UPDATE_PATH, "");
                if (pastUpdateFilePath.length() != 0) {
                    File pastUpdateFile = new File(pastUpdateFilePath);
                    if (pastUpdateFile.exists()) {
                        if (pastUpdateFile.delete()) {
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString(SHAREDPREF_APP_UPDATE_PATH,"");
                            editor.apply();
                        } else {
                            Log.w(LOG_APP_NAME, "File Error: File "
                                    + pastUpdateFilePath + " could not be deleted.");
                        }
                    } else {
                        // File has already been removed
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(SHAREDPREF_APP_UPDATE_PATH, "");
                        editor.apply();
                    }
                }
            });

            // Only check for updates once a day
            if (getIntent().getBooleanExtra(INTENT_VALUE_DISPLAY_UPDATE, false)) {
                new Handler().post(() -> new AppUpdate(MainActivity.this, true));
            } else if (!Objects.equals(getSharedPreferences(getPackageName(), MODE_PRIVATE)
                            .getString(SHAREDPREF_LAST_UPDATE_CHECK, ""),
                    ConverterFunctions.standardDateFormat.format(new Date()))) {
                new Handler().post(() -> new AppUpdate(MainActivity.this, false));
            }
        } else {
            // Set currentFragment
            currentFragment = getSupportFragmentManager().getFragments()
                    .get(getSupportFragmentManager().getFragments().size() - 1);
        }

        // Get subject, if needed from Intent
        if (getIntent().getBooleanExtra(INTENT_VALUE_START_FRAGMENT, false)) {
            String targetSubject = getIntent().getStringExtra(INTENT_VALUE_SUBJECT);
            displayFragment(NotesSubjectFragment.newInstance(targetSubject));
        }

        // Set toolbar, set again when rotated
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up drawer, set again when rotated
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.m3_nav_open, R.string.m3_nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        GeneralFunctions.updateNavView(this);
    }

    /** Delegates the items that are selected on the menu to the respective fragments. **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            // When NotesSelectFragment or ProjectSelectFragment is activated
            case R.id.menu_new_subj:
                if (currentFragment instanceof NotesSelectFragment) {
                    ((NotesSelectFragment) currentFragment).onNewSubjectPressed();
                } else {
                    ((ProjectSelectFragment) currentFragment).onNewProjectPressed();
                }
                break;
            case R.id.menu_import:
                if (currentFragment instanceof  NotesSelectFragment) {
                    ((NotesSelectFragment) currentFragment).onImportPressed();
                } else {
                    ((ProjectSelectFragment) currentFragment).onImportPressed();
                }
                break;

            // When NotesSubjectFragment is activated
            case R.id.n2_new_note:
                ((NotesSubjectFragment) currentFragment).onNewNotePressed();
                break;

            case R.id.n2_sort:
                ((NotesSubjectFragment) currentFragment).onSortPressed();
                break;

            case R.id.n2_rename:
                ((NotesSubjectFragment) currentFragment).onRenamePressed();
                break;

            case R.id.n2_export:
                ((NotesSubjectFragment) currentFragment).onExportPressed();
                break;

            case R.id.n2_del:
                ((NotesSubjectFragment) currentFragment).onDeletePressed();
                break;

            // When NotesViewFragment is activated
            case R.id.n3_edit:
                ((NotesViewFragment) currentFragment).onEditPressed();
                break;

            case R.id.n3_export:
                ((NotesViewFragment) currentFragment).onExportPressed();
                break;

            case R.id.n3_lock:
                ((NotesViewFragment) currentFragment).onLockPressed();
                break;

            case R.id.n3_notif:
                ((NotesViewFragment) currentFragment).onAlertPressed();
                break;

            case R.id.n3_cancel_notif:
                ((NotesViewFragment) currentFragment).onCancelAlertPressed();
                break;

            case R.id.n3_unlock:
                ((NotesViewFragment) currentFragment).onUnlockPressed();
                break;

            case R.id.n3_del:
                ((NotesViewFragment) currentFragment).onDeletePressed();
                break;

            // When NotesEditFragment is selected
            case R.id.n4_subj:
                ((NotesEditFragment) currentFragment).onSubjPressed();
                break;

            case R.id.n4_save:
                ((NotesEditFragment) currentFragment).onSavePressed();
                break;

            case R.id.n4_cancel:
                ((NotesEditFragment) currentFragment).onCancelPressed();
                break;

            // When ProjectInfoFragment is selected
            case R.id.p2_menu_user:
                ((ProjectInfoFragment) currentFragment).onUserPressed();
                break;

            case R.id.p2_menu_notes:
                ((ProjectInfoFragment) currentFragment).onNotesPressed();
                break;

            case R.id.p2_menu_settings:
                ((ProjectInfoFragment) currentFragment).onSettingsPressed();
                break;

            case R.id.p2_menu_media:
                ((ProjectInfoFragment) currentFragment).onMediaPressed();
                break;

            case R.id.p2_menu_export:
                ((ProjectInfoFragment) currentFragment).onExportPressed();
                break;
        }
        return true;
    }

    /** Handles button presses in the drawer.
     * Due to need for dynamic menu, most buttons have been moved to
     * GeneralFunctions.updateNavView(MainActivity activity)
     * @see GeneralFunctions **/
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
        if (currentFragment instanceof NotesViewFragment) {
            // As NotesViewFragment is inside a PagerAdapter, it does not intercept
            // the onBackPressed, hence its a special case
            ((NotesViewFragment) currentFragment).onBackPressed();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.base);
            if (!(fragment instanceof ExtendedFragment) || !((ExtendedFragment) fragment).onBackPressed()) {
                super.onBackPressed();
            }
        }
    }

    /** Returns the check permission result for all runtime permissions needed. **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.error_write_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** External intent is returned here from picking a file from
     * @see ImportSubject . The file would be sent back to a new ImportSubject to be imported. **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getData() != null) {
            String targetFile = FileFunctions.getRealPathFromUri(this, data.getData());
            if (requestCode == SELECT_ZIP_FILE) {
                // Sample URI:
                // content://com.coloros.filemanager.../documents/raw:/storage/emulated/0/file.ext
                new ImportSubject(this).importZipConfirm(targetFile);
            } else if (requestCode == SELECT_SUBJECT_FILE) {
                if (targetFile.endsWith(".subject")) {
                    new ImportSubject(this).importSubjectFile(targetFile);
                } else {
                    Toast.makeText(this, R.string.not_subject_file, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /** Displays the fragment that is needed to be displayed.
     * Keyboard will be hidden between fragments **/
    public void displayFragment(Fragment fragment) {
        // Hides bottomNavView if the project comes from a project fragment
        // and to a non-project fragment
        if (! (fragment instanceof ProjectInfoFragment || fragment instanceof ProjectMemberFragment
                || fragment instanceof ProjectTaskFragment
                || fragment instanceof ProjectRoleFragment
                || fragment instanceof ProjectStatusFragment)) {
            bottomNavView.setVisibility(View.GONE);
        }

        // Display the fragment
        // The lines need to be executed in this order to allow a smooth transition
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.base, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        if (pager.getAdapter() != null) {
            pager.setAdapter(null);
        }
        pager.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
        pager.setVisibility(View.GONE);
        findViewById(R.id.base).setVisibility(View.VISIBLE);
        currentFragment = fragment;
        hideKeyboard();
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
        FragmentStatePagerAdapter baseAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                currentFragment = NotesViewFragment.newInstance(subject, position);
                return currentFragment;
            }

            @Override
            public int getCount() {
                return size;
            }
        };
        pager.setAdapter(baseAdapter);
        // Fade out animation included to increase smoothness
        findViewById(R.id.base).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout));
        findViewById(R.id.base).setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);
        hideKeyboard();
    }


    /** Closes the navigation drawer. **/
    public void closeDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    /** Hides the soft input keyboard, separated for clarity. **/
    private void hideKeyboard() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /** Triggers the onBackPressed for key fragments (eg. when files are not saved)
     * when a function redirects to an external app/fragment. **/
    public void safeOnBackPressed() {
        if (currentFragment instanceof NotesEditFragment) {
            ((NotesEditFragment) currentFragment).onSavePressed();
        }
    }
}
