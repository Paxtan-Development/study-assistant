package com.pcchin.studyassistant.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String GITLAB_REPO = "https://gitlab.com/pc.chin/study-assistant/releases/";
    private static final String GITLAB_RELEASES = "https://gitlab.com/api/v4/projects/11826468/releases";

    private Fragment currentFragment;

    /** The runnable that checks for version updates of the app. **/
    private Runnable updateRunnable = () -> {
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
            if (isFromPlayStore()) {
                // Check from Play Store
                startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id="
                                    + getPackageName())));
            } else {
                checkGitlabUpdates(outputFileName);
            }
        }
    };

    /** Initializes activity. Sets up toolbar and drawer.  **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        displayFragment(new MainFragment());
        new Handler().post(updateRunnable);

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

    /** Hides the soft input keyboard. Separated for clarity. **/
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

    /** Checks if the app is downloaded from the Play Store. **/
    private boolean isFromPlayStore() {
        // A list with valid installers package name
        List<String> validInstallers = Arrays.asList("com.android.vending", "com.google.android.feedback");
        // The package name of the app that has installed your app
        final String installer = getPackageManager().getInstallerPackageName(getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    /** Checks whether a newer version of the app has been released on GitLab,
     * separated for clarity. */
    private void checkGitlabUpdates(String outputFileName) {
        StringRequest getReleases = new StringRequest(GITLAB_RELEASES, response -> {
            try {
                // Get latest version from releases page
                JSONObject latestVersion = new JSONObject(response)
                        .getJSONArray("data").getJSONObject(0);
                if (!Objects.equals(latestVersion.getString("name")
                        .replace("v", ""), BuildConfig.VERSION_NAME)) {
                    // Version is not the latest, needs to be updated
                    // The first link in the description is always the download link for the apk
                    String downloadLink = GITLAB_REPO + Jsoup.parse(Parser.unescapeEntities(
                            latestVersion.getString("description_html"), false))
                            .select("a").first().attr("href");
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.a_update_app)
                            .setMessage(R.string.a_new_version)
                            .setPositiveButton(android.R.string.yes, (dialogInterface, i) ->
                                    updateViaGitlab(downloadLink, outputFileName))
                            .setNegativeButton(android.R.string.no, ((dialogInterface, i) ->
                                    dialogInterface.dismiss()))
                            .create().show();

                }
            } catch (JSONException e) {
                Log.d("Network Error", "Response returned by " + GITLAB_RELEASES
                        + " invalid, response given is " + response + ", error given is "
                        + e.getMessage());
            }
        }, error -> Log.d("Network Error", "Volley returned statusCode " +
                error.networkResponse.statusCode));
        // Send request
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getReleases);
        queue.start();
    }

    /** Download and update the newest version of the app via GitLab,
     * separated for clarity. **/
    private void updateViaGitlab(String downloadLink, String outputFileName) {
        VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(Request.Method.GET,
                downloadLink, response -> {
            try {
                if (response != null) {
                    FileOutputStream responseStream;
                    responseStream = openFileOutput(outputFileName, MODE_PRIVATE);
                    responseStream.write(response);
                    responseStream.close();
                    Toast.makeText(this, R.string.a_app_updating, Toast.LENGTH_SHORT).show();

                    // Install app
                    Intent installIntent = new Intent(Intent.ACTION_VIEW);
                    installIntent.setDataAndType(Uri.fromFile(new File(outputFileName)),
                            "application/vnd.android.package-archive");
                    startActivity(installIntent);
                }
            } catch (Exception e) {
                Log.d("Network Error", "Volley download request failed in middle of operation with error "
                + Arrays.toString(e.getStackTrace()));
            }
        }, error -> {
            Log.d("Network Error", "Volley file download request failed with error code"
                    + error.networkResponse.statusCode + ", response given is "
                    + error.getMessage(), null);
            Toast.makeText(MainActivity.this,
                    "Network Error: Please check your internet connection and try again.",
                    Toast.LENGTH_SHORT).show();
        }, null);
        RequestQueue queue = Volley.newRequestQueue(this, new HurlStack());
        queue.add(request);
        queue.start();
    }

    /** @return a file name for the updated apk that is not taken in the Downloads folder. **/
    private String getNewFileName() {
        // Download to Downloads folder as a file with the same name exists
        String outputFileName = "/storage/emulated/0/Downloads" + "/studyassistant-update" + "." + "apk";
        int i = 0;
        while (new File(outputFileName).exists()) {
            outputFileName = "/storage/emulated/0/Downloads"+ "/studyassistant-update" + "." + i + "apk";
        }
        return outputFileName;
    }
}
