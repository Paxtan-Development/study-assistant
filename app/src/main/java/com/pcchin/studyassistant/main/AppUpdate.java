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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.misc.VolleyFileDownloadRequest;

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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Class that is used to check Gitlab for updates to the app, separated from
 * @see MainActivity for clarity.
 * Cannot be made static as gitlabReleasesStatusCode needs to be passed on from function to function. **/
class AppUpdate {
    private static final String GITLAB = "https://gitlab.com";
    private static final String GITLAB_API_RELEASES = "https://gitlab.com/api/v4/projects/11826468/releases";
    private static final String GITLAB_RELEASES = "https://gitlab.com/pc.chin/study-assistant/releases";

    private int gitlabReleasesStatusCode;
    private final boolean calledFromNotif;
    private final MainActivity activity;

    /** Checks for version updates of the app, doubles as the constructor.
     * checkGitlabUpdates() separated for clarity. **/
    AppUpdate(MainActivity activity, boolean calledFromNotif) {
        this.activity = activity;
        this.calledFromNotif = calledFromNotif;

        // Check if network is connected
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
    }

    /** Checks if the app is downloaded from the Play Store, separated for clarity. **/
    private boolean isFromPlayStore() {
        // A list with valid installers package name
        List<String> validInstallers = Arrays.asList("com.android.vending", "com.google.android.feedback");
        // The package name of the app that has installed your app
        final String installer = activity.getPackageManager().getInstallerPackageName(activity.getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }

    /** Checks whether a newer version of the app has been released on GitLab,
     * separated from constructor for clarity,
     * showGitlabUpdateNotif(JSONArray response) separated for clarity. */
    private void checkGitlabUpdates() {
        RequestQueue queue = Volley.newRequestQueue(activity);
        JsonArrayRequest getReleases = new JsonArrayRequest(GITLAB_API_RELEASES, response -> {
            SharedPreferences.Editor editor = activity.getSharedPreferences(
                    activity.getPackageName(), Context.MODE_PRIVATE).edit();
            editor.putString("gitlabReleasesJson", response.toString());
            editor.apply();
            showGitlabUpdateNotif(response);
        }, error -> {
            if (gitlabReleasesStatusCode == 304) {
                // Error 304 means that the page remains the same, pulls page from old site
                SharedPreferences sharedPref = activity.getSharedPreferences(
                        activity.getPackageName(), Context.MODE_PRIVATE);
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
                        error.getMessage() + ":" + error.toString() + " from " + GITLAB_API_RELEASES
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
            // Update so that it will not ask again on the same day
            SharedPreferences.Editor editor =
                    activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).edit();
            editor.putString("lastUpdateCheck", GeneralFunctions
                    .standardDateFormat.format(new Date()));
            editor.apply();

            // Get latest version from releases page
            JSONObject latestVersion = response.getJSONObject(0);
            if (!Objects.equals(latestVersion.getString("name")
                    .replace("v", ""), BuildConfig.VERSION_NAME)) {

                // Version is not the latest, needs to be updated
                // The first link in the description is always the download link for the apk
                String downloadLink = GITLAB + Jsoup.parse(Parser.unescapeEntities(
                        latestVersion.getString("description_html"), false))
                        .select("a").first().attr("href");

                if (!calledFromNotif) {
                    // Set up notification
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("displayUpdate", true);
                    PendingIntent pendingIntent = PendingIntent
                            .getActivity(activity, 0, intent, 0);
                    Notification notif = new NotificationCompat.Builder
                            (activity, activity.getPackageName())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(activity.getString(R.string.app_name_release))
                            .setContentText(activity.getString(R.string.a_update_app))
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setLights(Color.BLUE, 2000, 0)
                            .setVibrate(new long[]{0, 250, 250, 250, 250})
                            .setAutoCancel(true).build();
                    NotificationManagerCompat manager = NotificationManagerCompat.from(activity);
                    manager.notify(activity.getTaskId(), notif);
                }

                // Set up dialog
                AlertDialog updateDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.a_update_app)
                        .setMessage(R.string.a_new_version)
                        .setPositiveButton(android.R.string.yes, null)
                        .setNeutralButton(R.string.a_learn_more, null)
                        .setNegativeButton(android.R.string.no, null)
                        .create();
                updateDialog.setOnShowListener(dialogInterface -> {
                    updateDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        updateDialog.dismiss();
                        updateViaGitlab(downloadLink);
                    });
                    updateDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                        // The user should be able to update after coming back from the website
                        activity.safeOnBackPressed();
                        Intent gitlabReleaseSite = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(GITLAB_RELEASES));
                        activity.startActivity(gitlabReleaseSite);
                    });
                    updateDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                            view -> updateDialog.dismiss());
                });
                updateDialog.show();
            }
        } catch (JSONException e) {
            Log.d("StudyAssistant", "Network Error: Response returned by " + GITLAB_API_RELEASES
                    + " invalid, response given is " + response + ", error given is "
                    + e.getMessage());
        }
    }

    /** Download and update the newest version of the app via GitLab,
     * separated from showGitlabUpdateNotif(JSONArray response) for clarity. **/
    private void updateViaGitlab(String downloadLink) {
        // Generate output file name
        // Delete any incomplete apk file if present
        String outputFileName = activity.getFilesDir().getAbsolutePath() + "/apk";
        File apkInstallDir = new File(outputFileName);
        if (apkInstallDir.exists() && apkInstallDir.isDirectory()) {
            // Deletes all children in the folder
            File[] dirFiles = apkInstallDir.listFiles();
            if (dirFiles != null) {
                for (File child: dirFiles) {
                    FileFunctions.deleteDir(child);
                }
            }
            outputFileName += "/studyassistant-update.apk";
        } else if (!apkInstallDir.exists()) {
            if (apkInstallDir.mkdir()) {
                outputFileName += "/studyassistant-update.apk";
            } else {
                outputFileName = FileFunctions.generateValidFile(
                        "/storage/emulated/0/Download/studyassistant-update", ".apk");
            }
        } else {
            outputFileName = FileFunctions.generateValidFile(
                    "/storage/emulated/0/Download/studyassistant-update", ".apk");
        }

        RequestQueue queue = Volley.newRequestQueue(activity);
        // Boolean used as it is possible for user to cancel the dialog before the download starts
        AtomicBoolean continueDownload = new AtomicBoolean(true);
        ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setIndeterminate(true);
        AlertDialog downloadDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.a_downloading)
                .setView(progressBar)
                .setPositiveButton(android.R.string.cancel, null)
                .setOnDismissListener(dialogInterface -> {
                    Log.d("StudyAssistant", "Notification: Download of latest APK cancelled");
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
                    File outputFile = new File(finalOutputFileName);
                    if (outputFile.createNewFile()) {
                        responseStream = new FileOutputStream(outputFile);
                        responseStream.flush();
                        responseStream.close();

                        // Install app
                        activity.safeOnBackPressed();
                        Toast.makeText(activity, R.string.a_app_updating, Toast.LENGTH_SHORT).show();
                        Intent installIntent = new Intent(Intent.ACTION_VIEW);
                        installIntent.setDataAndType(Uri.fromFile(new File(finalOutputFileName)),
                                "application/vnd.android.package-archive");
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        activity.startActivity(installIntent);
                    } else {
                        Log.d("StudyAssistant", "File Error: File " + finalOutputFileName
                                + " could not be created.");
                        Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (FileNotFoundException e) {
                Log.d("StudyAssistant", "File Error: File" + finalOutputFileName + " not found, stack trace is ");
                e.printStackTrace();
                Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
            } catch (IOException e2) {
                Log.d("StudyAssistant", "File Error: An IOException occurred at " + finalOutputFileName
                        + ", stack trace is");
                e2.printStackTrace();
                Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.d("Study Assistant", "Error: Volley download request failed " +
                        "in middle of operation with error");
                e.printStackTrace();
                Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            downloadDialog.dismiss();
            Log.d("StudyAssistant", "Network Error: Volley file download request failed"
                    + ", response given is " + error.getMessage() + ", stack trace is");
            error.printStackTrace();
            Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
        }, null);

        if (continueDownload.get()) {
            queue.add(request);
        }
    }
}
