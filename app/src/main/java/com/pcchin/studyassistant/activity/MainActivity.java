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
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectSubject;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectZip;
import com.pcchin.studyassistant.file.project.ImportProjectIcon;
import com.pcchin.studyassistant.fragment.main.MainFragment;
import com.pcchin.studyassistant.fragment.notes.NotesEditFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragment;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.member.ProjectMemberListFragment;
import com.pcchin.studyassistant.fragment.project.role.ProjectRoleFragment;
import com.pcchin.studyassistant.fragment.project.status.ProjectStatusFragment;
import com.pcchin.studyassistant.fragment.project.task.ProjectTaskFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.NavViewFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.network.AppUpdate;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // Shared preference constants
    public static final String SHAREDPREF_APP_UPDATE_PATH = "AppUpdatePath";
    public static final String SHAREDPREF_LAST_UPDATE_CHECK = "lastUpdateCheck";

    // General intent constants
    public static final String INTENT_VALUE_DISPLAY_UPDATE = "displayUpdate";
    public static final String INTENT_VALUE_START_FRAGMENT = "startFragment";
    public static final String INTENT_VALUE_REQUEST_CODE = "requestCode";

    // Intent constants for notes
    public static final String INTENT_VALUE_SUBJECT = "subject";
    public static final String INTENT_VALUE_MESSAGE = "message";
    public static final String INTENT_VALUE_TITLE = "title";

    // Intent constants for projects
    public static final String INTENT_PROJECT_ID = "projectID";
    public static final String INTENT_ID2 = "id2";
    public static final String INTENT_IS_MEMBER = "isMember";

    // Intent codes
    public static final int SELECT_ZIP_FILE = 300;
    public static final int SELECT_SUBJECT_FILE = 301;
    public static final int SELECT_PROJECT_ICON = 302;

    // Permission codes
    private static final int EXTERNAL_STORAGE_PERMISSION = 200;
    public static final int EXTERNAL_STORAGE_READ_PERMISSION = 201;

    // Other constants
    public static final String DATABASE_NOTES = "notesSubject";
    public static final String DATABASE_PROJECT = "projectDatabase";
    public static final String LOG_APP_NAME = "StudyAssistant";

    public BottomNavigationView bottomNavView;
    public ViewPager pager;
    public Fragment currentFragment;

    /** Initializes activity. Sets up toolbar and drawer.  **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if app is opened to process files
        Uri intentUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (intentUri != null) {
            String receiveFilePath = FileFunctions.getRealPathFromUri(MainActivity.this, intentUri);
            // Check if file type matches the required file types
            if (receiveFilePath.endsWith(".subject")) {
                new ImportSubjectSubject(MainActivity.this).importSubjectFile(receiveFilePath);
            } else if (receiveFilePath.endsWith(".zip") || receiveFilePath.endsWith(".ZIP")
                || receiveFilePath.endsWith(".Zip")) {
                new ImportSubjectZip(MainActivity.this).importZipConfirm(receiveFilePath);
            } else {
                Toast.makeText(MainActivity.this, R.string.error_file_format_incorrect, Toast.LENGTH_SHORT).show();
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
                    .checkSelfPermission(MainActivity.this, Manifest.permission
                    .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION);
            }

            // Set up Admin & Member roles in database for projects
            new Handler().post(() -> {
                ProjectDatabase projectDatabase = GeneralFunctions.getProjectDatabase(MainActivity.this);
                RoleData admin = projectDatabase.RoleDao().searchByID("admin");
                if (admin == null) {
                    admin = new RoleData("admin", "", "Admin", "", "");
                    admin.canDeleteProject = true;
                    admin.canModifyInfo = true;
                    admin.canModifyOtherTask = true;
                    admin.canModifyOtherUser = true;
                    admin.canModifyOwnTask = true;
                    admin.canModifyRole = true;
                    admin.canModifyOtherStatus = true;
                    admin.canPostStatus = true;
                    admin.canSetPassword = true;
                    admin.canViewOtherTask = true;
                    admin.canViewOtherUser = true;
                    admin.canViewRole = true;
                    admin.canViewTask = true;
                    admin.canViewStatus = true;
                    admin.canViewMedia = true;
                    projectDatabase.RoleDao().insert(admin);
                }
                projectDatabase.close();
            });

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

            // Only check for updates once a day for non-beta users
            if (getIntent().getBooleanExtra(INTENT_VALUE_DISPLAY_UPDATE, false)) {
                new Handler().post(() -> new AppUpdate(MainActivity.this, true));
            } else if (!Objects.equals(getSharedPreferences(getPackageName(), MODE_PRIVATE)
                            .getString(SHAREDPREF_LAST_UPDATE_CHECK, ""),
                    ConverterFunctions.standardDateFormat.format(new Date()))) {
                new Handler().post(() -> new AppUpdate(MainActivity.this, false));
            }
        } else {
            // Set currentFragment, pager and bottomNavView which had been cleared when rotating
            currentFragment = getSupportFragmentManager().getFragments()
                    .get(getSupportFragmentManager().getFragments().size() - 1);
            pager = findViewById(R.id.base_pager);
            bottomNavView = findViewById(R.id.bottom_nav);
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
               MainActivity.this, drawer, toolbar, R.string.m3_nav_open, R.string.m3_nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(MainActivity.this);

        NavViewFunctions.updateNavView(MainActivity.this);
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
                Toast.makeText(MainActivity.this, R.string.error_write_permission_denied,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** External intent is returned here from picking a file from the following:
     * @see ImportSubjectZip
     * @see ImportSubjectSubject
     * @see ImportProjectIcon
     * The file would be sent back to a new ImportSubject to be imported. **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data.getData() != null) {
            String targetFile = FileFunctions.getRealPathFromUri(MainActivity.this, data.getData());
            if (requestCode == SELECT_ZIP_FILE) {
                // Sample URI:
                // content://com.coloros.filemanager.../documents/raw:/storage/emulated/0/file.ext
                new ImportSubjectZip(MainActivity.this).importZipConfirm(targetFile);
            } else if (requestCode == SELECT_SUBJECT_FILE) {
                if (targetFile.endsWith(".subject")) {
                    new ImportSubjectSubject(MainActivity.this).importSubjectFile(targetFile);
                } else {
                    Toast.makeText(MainActivity.this, R.string.not_subject_file, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == SELECT_PROJECT_ICON) {
                // TODO: Forward image to project settings
                new ImportProjectIcon(MainActivity.this).start();
            } else {
                // TODO: Import media to project
            }
        }
    }

    /** Displays the fragment that is needed to be displayed.
     * Keyboard will be hidden between fragments **/
    public void displayFragment(Fragment fragment) {
        // Hides bottomNavView if the project comes from a project fragment
        // and to a non-project fragment
        if (! (fragment instanceof ProjectInfoFragment || fragment instanceof ProjectMemberListFragment
                || fragment instanceof ProjectTaskFragment
                || fragment instanceof ProjectRoleFragment
                || fragment instanceof ProjectStatusFragment)) {
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
            // getItem does not correspond to the current item selected, DO NOT USE IT AS SUCH
            public Fragment getItem(int position) {
                // Used to be an issue where NotesViewFragment would crash as menu is null,
                // but it seems to had resolved itself
                return NotesViewFragment.newInstance(subject, position);
            }

            @Override
            public int getCount() {
                return size;
            }
        };
        // Updates currentFragment to the current item
        ViewPager.OnPageChangeListener baseAdapterPageChanger = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not needed
            }

            @Override
            public void onPageSelected(int position) {
                // instantiateItem used instead of getItem as getItem returns a new instance of
                // a fragment instead of an existing one
                currentFragment = (Fragment) baseAdapter.instantiateItem(findViewById(R.id.base), position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not needed
            }
        };
        pager.setAdapter(baseAdapter);
        pager.addOnPageChangeListener(baseAdapterPageChanger);
        fadeToNote();
    }

    /** Fades out to another note to increase smoothness. **/
    private void fadeToNote() {
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
            view = new View(MainActivity.this);
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