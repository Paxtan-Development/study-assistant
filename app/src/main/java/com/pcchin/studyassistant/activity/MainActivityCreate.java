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
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectSubject;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectZip;
import com.pcchin.studyassistant.fragment.main.MainFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.DataFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.NavViewFunctions;
import com.pcchin.studyassistant.network.update.AppUpdate;

import java.io.File;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import io.sentry.Sentry;
import io.sentry.event.UserBuilder;

/** Functions used within onCreate of
 * @see MainActivity **/
final class MainActivityCreate {
    private final MainActivity activity;

    /** Constructor used as activity needs to be passed on. **/
    MainActivityCreate(MainActivity activity) {
        this.activity = activity;
    }

    /** The onCreate function for MainActivity. **/
    void onCreate(Bundle savedInstanceState) {
        activity.setContentView(R.layout.activity_main);
        checkFileUri();
        activity.pager = activity.findViewById(R.id.base_pager);
        activity.bottomNavView = activity.findViewById(R.id.bottom_nav);
        setActivityInfo(savedInstanceState);
        initSentry();

        // Get subject, if needed from Intent
        if (activity.getIntent().getBooleanExtra(ActivityConstants.INTENT_VALUE_START_FRAGMENT, false)) {
            int targetNote = activity.getIntent().getIntExtra(ActivityConstants.INTENT_VALUE_NOTE_ID, 0);
            SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
            int targetSubject = database.ContentDao().search(targetNote).subjectId;
            database.close();
            activity.displayFragment(NotesSubjectFragment.newInstance(targetSubject, targetNote));
        }
        initFragmentView();
        setNavigation();
    }

    /** Initializes Sentry to be used in all build configs.
     * The used is identified by their UID and their app version.
     * Their device version and Android Version is not recorded unless they submit a bug report
     * manually or are not in the release build. **/
    private void initSentry() {
        SharedPreferences sharedPref = DataFunctions.getSharedPref(activity);
        // Don't init if its in debug mode
        //noinspection ConstantConditions
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            Sentry.init((String) null);
        } else {
            Sentry.init(BuildConfig.SENTRY_DSN);
        }
        Sentry.getContext().setUser(new UserBuilder().setId(sharedPref.getString(ActivityConstants.SHAREDPREF_UID, "")).build());
        Sentry.getContext().addExtra("App Version", BuildConfig.VERSION_NAME);
        //noinspection ConstantConditions
        if (!BuildConfig.BUILD_TYPE.equals("release")) {
            Sentry.getContext().addExtra("Android Version", Build.VERSION.SDK_INT);
            Sentry.getContext().addExtra("Device Model", ActivityConstants.DEVICE_MODEL);
        }
    }

    /** Display and hides the relevant views used by the current activity. **/
    private void initFragmentView() {
        if (activity.currentFragment == null || !(activity.currentFragment instanceof NotesViewFragment)) {
            activity.findViewById(R.id.base_pager).setVisibility(View.GONE);
        } else {
            NotesViewFragment currentNote = ((NotesViewFragment) activity.currentFragment);
            activity.findViewById(R.id.base).setVisibility(View.GONE);
            activity.displayNotes(currentNote.note.subjectId);
            activity.pager.setPagerOrder(currentNote.note.noteId);
        }

        if (!MainActivityFunctions.fragmentHasBottomNavView(activity.currentFragment)) {
            activity.findViewById(R.id.bottom_nav).setVisibility(View.GONE);
        }
    }

    /** Set up info for the activity. **/
    private void setActivityInfo(Bundle savedInstanceState) {
        // First time starting the app
        if (savedInstanceState == null) {
            firstStart();
            // Only check for updates once a day
            if (activity.getIntent().getBooleanExtra(ActivityConstants.INTENT_VALUE_DISPLAY_UPDATE, false)) {
                new Handler().post(() -> new AppUpdate(activity, true));
            } else if (!Objects.equals(DataFunctions.getSharedPref(activity).getString(ActivityConstants.SHAREDPREF_LAST_UPDATE_CHECK, ""),
                    ConverterFunctions.formatTime(new Date(), ConverterFunctions.TimeFormat.DATE))) {
                new Handler().post(() -> new AppUpdate(activity, false));
            }
        } else {
            // Set currentFragment, pager and bottomNavView which had been cleared when rotating
            activity.currentFragment = activity.getSupportFragmentManager().getFragments()
                    .get(activity.getSupportFragmentManager().getFragments().size() - 1);
            activity.pager = activity.findViewById(R.id.base_pager);
            activity.bottomNavView = activity.findViewById(R.id.bottom_nav);
        }
    }

    /** Function that is called as when the app is first started. **/
    private void firstStart() {
        activity.displayFragment(new MainFragment());
        // Hide bottom navigation view
        activity.bottomNavView.setVisibility(View.GONE);
        setNotifChannel();

        // Get permission to read and write files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, ActivityConstants.EXTERNAL_STORAGE_PERMISSION);
        }
        // No handlers used for them as they had to be completed first
        generateUID();
        new Handler().post(this::createDefaultRoles); // First database access in MainActivityCreate on runtime
        new Handler().post(this::deletePastExports);
    }

    /** Generates a unique ID for the client if one does not exist. **/
    private void generateUID() {
        SharedPreferences sharedPref = DataFunctions.getSharedPref(activity);
        if (Objects.requireNonNull(sharedPref.getString(ActivityConstants.SHAREDPREF_UID, "")).length() == 0) {
            sharedPref.edit().putString(ActivityConstants.SHAREDPREF_UID, UUID.randomUUID().toString()).apply();
        }
    }

    /** Checks whether the activity is opened to process files. **/
    private void checkFileUri() {
        Uri intentUri = activity.getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (intentUri != null) {
            String receiveFilePath = FileFunctions.getRealPathFromUri(activity, intentUri);
            // Check if file type matches the required file types
            if (receiveFilePath != null && receiveFilePath.endsWith(".subject")) {
                new ImportSubjectSubject(activity).importSubjectFile(receiveFilePath);
            } else if (receiveFilePath != null && (receiveFilePath.endsWith(".zip") || receiveFilePath.endsWith(".ZIP")
                    || receiveFilePath.endsWith(".Zip"))) {
                new ImportSubjectZip(activity).importZipConfirm(receiveFilePath);
            } else {
                Toast.makeText(activity, R.string.error_file_format_incorrect, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Set up the notification channel for the app. **/
    private void setNotifChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mainChannel = new NotificationChannel(activity.getString(
                    R.string.notif_channel_notes_ID), activity.getString(R.string.notif_channel_notes), NotificationManager.IMPORTANCE_DEFAULT);
            mainChannel.setDescription(activity.getString(R.string.notif_channel_notes_desc));
            NotificationChannel updateChannel = new NotificationChannel(activity.getString(
                    R.string.notif_channel_update_ID), activity.getString(R.string.notif_channel_update), NotificationManager.IMPORTANCE_LOW);
            updateChannel.setDescription(activity.getString(R.string.notif_channel_update_desc));
            NotificationManager manager = activity.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(mainChannel);
                manager.createNotificationChannel(updateChannel);
            }
        }
    }

    /** Create the default admin and member roles. **/
    private void createDefaultRoles() {
        ProjectDatabase projectDatabase = DatabaseFunctions.getProjectDatabase(activity);
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
    }

    /** Delete the past export files. **/
    private void deletePastExports() {
        String outputFileName = activity.getFilesDir().getAbsolutePath() + "/temp";
        File apkInstallDir = new File(outputFileName);
        if (apkInstallDir.exists() && apkInstallDir.isDirectory()) {
            // Deletes all children in the folder
            File[] dirFiles = apkInstallDir.listFiles();
            if (dirFiles != null) {
                for (File child : dirFiles) FileFunctions.deleteDir(child);
            }
        } else if (!apkInstallDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            apkInstallDir.mkdir();
        }
        deletePastApk();
    }

    /** Delete the previously downloaded APK files if they exist. **/
    private void deletePastApk() {
        SharedPreferences sharedPref = DataFunctions.getSharedPref(activity);
        String pastUpdateFilePath = sharedPref.getString(ActivityConstants.SHAREDPREF_APP_UPDATE_PATH, "");
        if (Objects.requireNonNull(pastUpdateFilePath).length() != 0) {
            File pastUpdateFile = new File(pastUpdateFilePath);
            if (pastUpdateFile.exists()) {
                if (pastUpdateFile.delete()) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(ActivityConstants.SHAREDPREF_APP_UPDATE_PATH,"");
                    editor.apply();
                } else {
                    Log.w(ActivityConstants.LOG_APP_NAME, "File Error: File "
                            + pastUpdateFilePath + " could not be deleted.");
                }
            } else {
                // File has already been removed
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(ActivityConstants.SHAREDPREF_APP_UPDATE_PATH, "");
                editor.apply();
            }
        }
    }

    /** Set up the toolbar and the drawer for the app. **/
    private void setNavigation() {
        // Set toolbar, set again when rotated
        Toolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);

        // Set up drawer, set again when rotated
        DrawerLayout drawer = activity.findViewById(R.id.drawer_layout);
        NavigationView navigationView = activity.findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, drawer, toolbar, R.string.m3_nav_open, R.string.m3_nav_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(activity);
        NavViewFunctions.updateNavView(activity);
    }
}
