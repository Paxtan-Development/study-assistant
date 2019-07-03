package com.pcchin.studyassistant.main;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.VolleyFileDownloadRequest;
import com.pcchin.studyassistant.notes.NotesEditFragment;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.notes.NotesViewFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String GITLAB = "https://gitlab.com";
    private static final String GITLAB_RELEASES = "https://gitlab.com/api/v4/projects/11826468/releases";

    private int gitlabReleasesStatusCode;
    private Fragment currentFragment;

    /** The runnable that checks for version updates of the app.
     * checkGitlabUpdates() separated for clarity. **/
    private final Runnable updateRunnable = () -> {
        // Check if network is connected
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities activeNetwork = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (activeNetwork != null && activeNetwork
                        .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    isConnected = true;
                }
            } else if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
                    isConnected = true;
            }
        }

        // Check if there is a newer version of the app
        if (isConnected) {
            if (!isFromPlayStore()) {
                checkGitlabUpdates();
            }
        }
    };

    /** Initializes activity. Sets up toolbar and drawer.  **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayFragment(new MainFragment());

        // Only check for updates once a day
        if (Objects.equals(getSharedPreferences(getPackageName(), MODE_PRIVATE)
                .getString("lastUpdateCheck", ""),
                GeneralFunctions.standardDateFormat.format(Calendar.getInstance()))) {
            new Handler().post(updateRunnable);
        }

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

        GeneralFunctions.updateNavView(this);
    }

    /** Delegates the items that are selected on the menu to the respective fragments. **/
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            // When NotesSelectFragment is activated
            case R.id.n1_new_subj:
                ((NotesSelectFragment) currentFragment).onNewSubjectPressed();
                break;
            case R.id.n1_import:
                ((NotesSelectFragment) currentFragment).onImportPressed();
                break;

            // When NotesSubjectFragment is activated
            case R.id.n2_new_note:
                ((NotesSubjectFragment) currentFragment).onNewNotePressed();
                break;

            case R.id.n2_sort:
                ((NotesSubjectFragment) currentFragment).onSortPressed();
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.base);
        if (!(fragment instanceof FragmentOnBackPressed) || !((FragmentOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();
        }
    }

    /** Displays the fragment that is needed to be displayed.
     * Keyboard will be hidden between fragments **/
    public void displayFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.base, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
        currentFragment = fragment;
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

    /** Checks if the app is downloaded from the Play Store, separated for clarity. **/
    private boolean isFromPlayStore() {
        // A list with valid installers package name
        List<String> validInstallers = Arrays.asList("com.android.vending", "com.google.android.feedback");
        // The package name of the app that has installed your app
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    /** Checks whether a newer version of the app has been released on GitLab,
     * separated from UpdateRunnable for clarity,
     * showGitlabUpdateNotif(JSONArray response) separated for clarity. */
    private void checkGitlabUpdates() {
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest getReleases = new JsonArrayRequest(GITLAB_RELEASES, response -> {
            SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
            editor.putString("gitlabReleasesJson", response.toString());
            editor.apply();
            showGitlabUpdateNotif(response);
        }, error -> {
            if (gitlabReleasesStatusCode == 304) {
                // Error 304 means that the page remains the same, pulls page from old site
                SharedPreferences sharedPref = getSharedPreferences(getPackageName(), MODE_PRIVATE);
                String oldResponse = sharedPref.getString("gitlabReleasesJson", "");
                try {
                    JSONArray oldArray = new JSONArray(oldResponse);
                    showGitlabUpdateNotif(oldArray);
                } catch (JSONException e) {
                    Log.d("StudyAssistant", "Data Error: former response " + oldResponse
                            + " is not a JSON array.");
                    queue.stop();
                }
            } else {
                Log.d("StudyAssistant", "Network Error: Volley returned error " +
                        error.getMessage() + ":" + error.toString() + " from " + GITLAB_RELEASES
                        + ", stack trace is");
                error.printStackTrace();
                queue.stop();
            }
        }) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                gitlabReleasesStatusCode = response.statusCode;
                return super.parseNetworkResponse(response);
            }
        };
        // Send request
        queue.add(getReleases);
    }

    /** Show users the update notification,
     * separated from checkGitlabUpdates() for clarity,
     * updateViaGitlab(String downloadLink) separated for clarity. **/
    private void showGitlabUpdateNotif(JSONArray response) {
        try {
            // Get latest version from releases page
            JSONObject latestVersion = response.getJSONObject(0);
            if (!Objects.equals(latestVersion.getString("name")
                    .replace("v", ""), BuildConfig.VERSION_NAME)) {
                // Version is not the latest, needs to be updated
                // The first link in the description is always the download link for the apk
                String downloadLink = GITLAB + Jsoup.parse(Parser.unescapeEntities(
                        latestVersion.getString("description_html"), false))
                        .select("a").first().attr("href");
                // Set up notification
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent
                        .getActivity(this, 0, intent, 0);
                Notification notif = new NotificationCompat.Builder
                        (this, getPackageName())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(getString(R.string.a_update_app))
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(Color.BLUE, 2000, 0)
                        .setVibrate(new long[]{0, 250, 250, 250, 250})
                        .setAutoCancel(true).build();
                NotificationManagerCompat manager = NotificationManagerCompat.from(this);
                manager.notify(getTaskId(), notif);

                // Set up dialog
                new AlertDialog.Builder(this)
                        .setTitle(R.string.a_update_app)
                        .setMessage(R.string.a_new_version)
                        .setPositiveButton(android.R.string.yes, (dialogInterface, i) ->
                                updateViaGitlab(downloadLink))
                        .setNegativeButton(android.R.string.no, ((dialogInterface, i) -> {
                                // Update so that it will not ask again on the same day
                                SharedPreferences.Editor editor =
                                        getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
                                editor.putString("lastUpdateCheck", GeneralFunctions
                                        .standardDateFormat.format(Calendar.getInstance()));
                                editor.apply();
                                dialogInterface.dismiss(); }))
                        .create().show();
            }
        } catch (JSONException e) {
            Log.d("StudyAssistant", "Network Error: Response returned by " + GITLAB_RELEASES
                    + " invalid, response given is " + response + ", error given is "
                    + e.getMessage());
        }
    }

    /** Download and update the newest version of the app via GitLab,
     * separated from showGitlabUpdateNotif(JSONArray response) for clarity. **/
    private void updateViaGitlab(String downloadLink) {
        // Generate output file name
        // Delete any incomplete apk file if present
        String outputFileName = getFilesDir().getAbsolutePath() + "/apk";
        File apkInstallDir = new File(outputFileName);
        if (apkInstallDir.exists() && apkInstallDir.isDirectory()) {
            // Deletes all children in the folder
            File[] dirFiles = apkInstallDir.listFiles();
            if (dirFiles != null) {
                for (File child: dirFiles) {
                    GeneralFunctions.deleteDir(child);
                }
            }
            outputFileName += "/studyassistant-update.apk";
        } else if (!apkInstallDir.exists()) {
            if (apkInstallDir.mkdir()) {
                outputFileName += "/studyassistant-update.apk";
            } else {
                outputFileName = getNewFileName();
            }
        } else {
            outputFileName = getNewFileName();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        // Boolean used as it is possible for user to cancel the dialog before the download starts
        AtomicBoolean continueDownload = new AtomicBoolean(true);
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        AlertDialog downloadDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.a_downloading)
                .setView(progressBar)
                .setPositiveButton(android.R.string.cancel, null)
                .setOnDismissListener(dialogInterface -> {
                    Log.d("StudyAssistant", "Notif: Download cancel");
                    queue.stop();
                    continueDownload.set(false);
                })
                .create();
        downloadDialog.show();

        String finalOutputFileName = outputFileName;
        VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(Request.Method.GET,
                downloadLink, response -> {
            try {
                downloadDialog.dismiss();
                queue.stop();
                if (response != null) {
                    FileOutputStream responseStream;
                    responseStream = new FileOutputStream(new File(finalOutputFileName));
                    responseStream.flush();
                    responseStream.close();

                    // Install app
                    Toast.makeText(this, R.string.a_app_updating, Toast.LENGTH_SHORT).show();
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(Uri.fromFile(new File(finalOutputFileName)),
                            "application/vnd.android.package-archive");
                    startActivity(installIntent);
                }
            } catch (FileNotFoundException e) {
                Log.d("StudyAssistant", "File Error: File" + finalOutputFileName + " not found.");
                Toast.makeText(this, R.string.a_file_error, Toast.LENGTH_SHORT).show();
            } catch (IOException e2) {
                Log.d("StudyAssistant", "File Error: An IOException occurred at " + finalOutputFileName
                + ", stack trace is");
                e2.printStackTrace();
                Toast.makeText(this, R.string.a_file_error, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("Study Assistant", "Error: Volley download request failed " +
                        "in middle of operation with error");
                e.printStackTrace();
                Toast.makeText(this, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            downloadDialog.dismiss();
            Log.d("StudyAssistant", "Network Error: Volley file download request failed"
                     + ", response given is " + error.getMessage() + ", stack trace is");
            error.printStackTrace();
            Toast.makeText(MainActivity.this, R.string.a_network_error, Toast.LENGTH_SHORT).show();
        }, null);
        if (continueDownload.get()) {
            queue.add(request);
        }
    }

    /** @return a file name for the updated apk that is not taken in the Downloads folder. **/
    private String getNewFileName() {
        // Download to Downloads folder as a file with the same name exists
        String outputFileName = "/storage/emulated/0/Downloads" + "/studyassistant-update" + "." + "apk";
        int i = 0;
        while (new File(outputFileName).exists()) {
            outputFileName = "/storage/emulated/0/Downloads"+ "/studyassistant-update(" + i + ")." + "apk";
        }
        return outputFileName;
    }
}
