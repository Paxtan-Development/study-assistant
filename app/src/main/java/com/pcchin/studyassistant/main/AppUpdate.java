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

package com.pcchin.studyassistant.main;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.display.AutoDismissDialog;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.misc.VolleyFileDownloadRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Class that is used to check Gitlab for updates to the app, separated from
 * @see MainActivity for clarity.
 * Cannot be made static as gitlabReleasesStatusCode needs to be passed on from function to function. **/
class AppUpdate {
    private static final String MAIN_API = "https://api.paxtan.dev";
    private static final String BACKUP_API = "https://api.pcchin.com";
    private static final String SEC_BACKUP_API = "https://paxtandev.herokuapp.com";
    private static final String UPDATE_PATH = "/study-assistant/latest";
    /* Example user agent: "Study-Assistant/1.5 (...)" */
    @SuppressWarnings("ConstantConditions")
    private static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("Study-Assistant/%s", BuildConfig.VERSION_NAME));

    private final boolean calledFromNotif;
    private final MainActivity activity;

    /** Checks for version updates of the app, doubles as the constructor.
     * checkServerUpdates() separated for clarity. **/
    AppUpdate(@NonNull MainActivity activity, boolean calledFromNotif) {
        this.activity = activity;
        this.calledFromNotif = calledFromNotif;

        // Check if network is connected (Updated code as old code is deprecated)
        boolean isConnected = false;
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT < 23) {
                final NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    isConnected = ni.isConnected();
                }
            } else {
                final Network n = cm.getActiveNetwork();
                if (n != null) {
                    final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                    if (nc != null) {
                        isConnected = nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                    }
                }
            }
        }

        // Check if there is a newer version of the app
        if (isConnected) {
            if (!isFromPlayStore()) {
                checkServerUpdates();
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

    /** Checks whether a newer version of the app has been released on GitHub through the main api,
     * and checks the backup API if the main API fails,
     * separated from constructor for clarity,
     * showUpdateNotif(JSONArray response) separated for clarity. */
    private void checkServerUpdates() {
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Secondary Backup Server
        JsonObjectRequest getSecBackupReleases = new JsonObjectRequest(SEC_BACKUP_API + UPDATE_PATH, null,
                response -> showUpdateNotif(response, BACKUP_API), error -> {
            Log.d(MainActivity.LOG_APP_NAME, "Network Error: Volley returned error " +
                    error.getMessage() + ":" + error.toString() + " from " + BACKUP_API
                    + ", stack trace is");
            error.printStackTrace();
            queue.stop();
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", USER_AGENT);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        // Backup Server
        JsonObjectRequest getBackupReleases = new JsonObjectRequest(BACKUP_API + UPDATE_PATH, null,
                response -> showUpdateNotif(response, BACKUP_API), error -> {
            Log.d(MainActivity.LOG_APP_NAME, "Network Error: Volley returned error " +
                    error.getMessage() + ":" + error.toString() + " from " + BACKUP_API
                    + ", stack trace is");
            error.printStackTrace();
            Log.d(MainActivity.LOG_APP_NAME, "Attempting to connect to secondary backup server");
            queue.add(getSecBackupReleases);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", USER_AGENT);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        // Main Server
        JsonObjectRequest getReleases = new JsonObjectRequest(MAIN_API + UPDATE_PATH, null,
                response -> showUpdateNotif(response, MAIN_API), error -> {
            Log.d(MainActivity.LOG_APP_NAME, "Network Error: Volley returned error " +
                    error.getMessage() + ":" + error.toString() + " from " + MAIN_API
                    + ", stack trace is");
            error.printStackTrace();
            Log.d(MainActivity.LOG_APP_NAME, "Attempting to connect to backup server");
            queue.add(getBackupReleases);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", USER_AGENT);
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(@NonNull NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };

        // Send request
        queue.add(getReleases);
    }

    /** Show users the update notification,
     * separated from checkServerUpdates() for clarity,
     * updateViaGithub(String downloadLink) separated for clarity. **/
    private void showUpdateNotif(@NonNull JSONObject response, String host) {
        try {
            // Update so that it will not ask again on the same day
            SharedPreferences.Editor editor =
                    activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).edit();
            editor.putString(MainActivity.SHAREDPREF_LAST_UPDATE_CHECK, ConverterFunctions
                    .standardDateFormat.format(new Date()));
            editor.apply();

            // Get latest version from releases page
            if (!Objects.equals(response.getString("version")
                    .replace("v", ""), BuildConfig.VERSION_NAME)) {
                String downloadLink = response.getString("download");
                String releaseLink = response.getString("page");

                if (!calledFromNotif) {
                    // Set up notification
                    Intent intent = new Intent(activity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(MainActivity.INTENT_VALUE_DISPLAY_UPDATE, true);
                    PendingIntent pendingIntent = PendingIntent
                            .getActivity(activity, 0, intent, 0);
                    NotificationCompat.Builder notif = new NotificationCompat.Builder
                            (activity, activity.getPackageName())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(activity.getString(R.string.app_name))
                            .setContentText(activity.getString(R.string.a_update_app))
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setLights(Color.BLUE, 2000, 0)
                            .setVibrate(new long[]{0, 250, 250, 250, 250})
                            .setAutoCancel(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notif.setChannelId(activity.getString(R.string.notif_channel_update_ID));
                    }
                    NotificationManagerCompat manager = NotificationManagerCompat.from(activity);
                    manager.notify(activity.getTaskId(), notif.build());
                }

                // Set up dialog
                DialogInterface.OnShowListener updateListener = dialogInterface -> {
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                        dialogInterface.dismiss();
                        updateViaGithub(downloadLink);
                    });
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                        // The user should be able to update after coming back from the website
                        activity.safeOnBackPressed();
                        Intent gitlabReleaseSite = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(releaseLink));
                        activity.startActivity(gitlabReleaseSite);
                    });
                    ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                            view -> dialogInterface.dismiss());
                };
                new AutoDismissDialog(activity.getString(R.string.a_update_app),
                        activity.getString(R.string.a_new_version), new String[]
                        {activity.getString(android.R.string.yes),
                        activity.getString(android.R.string.no),
                        activity.getString(R.string.a_learn_more)}, updateListener)
                        .show(activity.getSupportFragmentManager(), "AppUpdate.1");
            }
        } catch (JSONException e) {
            Log.d(MainActivity.LOG_APP_NAME, "Network Error: Response returned by " + host
                    + " invalid, response given is " + response + ", error given is "
                    + e.getMessage());
        }
    }

    /** Download and update the newest version of the app via GitLab,
     * separated from showUpdateNotif(JSONArray response) for clarity. **/
    private void updateViaGithub(String downloadLink) {
        // Generate output file name
        // Checks if the /files directory exists, if not it is created
        File filesDir = new File(activity.getFilesDir().getAbsolutePath() + "/temp");
        if (filesDir.exists() || filesDir.mkdir()) {
            // Ask other APK files is deleted on startup, leftover files would not be checked here
            String outputFileName = FileFunctions.generateValidFile(activity
                    .getFilesDir().getAbsolutePath() + "/temp/studyassistant-update", ".apk");
            RequestQueue queue = Volley.newRequestQueue(activity);
            // Boolean used as it is possible for user to cancel the dialog before the download starts
            AtomicBoolean continueDownload = new AtomicBoolean(true);
            ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true);
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> {
                Log.d(MainActivity.LOG_APP_NAME, "Notification: Download of latest APK cancelled");
                queue.stop();
                continueDownload.set(false);
            };
            AutoDismissDialog downloadDialog = new AutoDismissDialog(activity
                    .getString(R.string.a_downloading), progressBar,
                    new String[]{activity.getString(android.R.string.cancel), "", ""});
            downloadDialog.setCancellable(false);
            downloadDialog.setDismissListener(dismissListener);
            downloadDialog.show(activity.getSupportFragmentManager(), "AppUpdate.2");

            VolleyFileDownloadRequest request = new VolleyFileDownloadRequest(Request.Method.GET,
                    downloadLink, response -> {
                try {
                    downloadDialog.dismiss();
                    queue.stop();
                    if (response != null) {
                        File outputFile = new File(outputFileName);
                        if (outputFile.createNewFile()) {
                            SharedPreferences.Editor editor = activity.getSharedPreferences(
                                    activity.getPackageName(), Context.MODE_PRIVATE).edit();
                            editor.putString(MainActivity.SHAREDPREF_APP_UPDATE_PATH, outputFileName);
                            editor.apply();

                            // Write output file with buffer
                            InputStream input = new ByteArrayInputStream(response);
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
                            byte[] data = new byte[1024];
                            int count;
                            while ((count = input.read(data)) != -1) {
                                output.write(data, 0, count);
                            }
                            output.flush();
                            output.close();
                            input.close();

                            // Install app
                            activity.safeOnBackPressed();
                            Toast.makeText(activity, R.string.a_app_updating, Toast.LENGTH_SHORT).show();
                            Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(FileProvider.getUriForFile(activity,
                                    activity.getPackageName() + ".ContentProvider",
                                    new File(outputFileName)),
                                    "application/vnd.android.package-archive");
                            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            activity.startActivity(installIntent);
                        } else {
                            Log.d(MainActivity.LOG_APP_NAME, "File Error: File " + outputFileName
                                    + " could not be created.");
                            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (FileNotFoundException e) {
                    Log.d(MainActivity.LOG_APP_NAME, "File Error: File" + outputFileName + " not found, stack trace is ");
                    e.printStackTrace();
                    Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                } catch (IOException e2) {
                    Log.d(MainActivity.LOG_APP_NAME, "File Error: An IOException occurred at " + outputFileName
                            + ", stack trace is");
                    e2.printStackTrace();
                    Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                Log.d(MainActivity.LOG_APP_NAME, "Error: Volley download request failed " +
                        "in middle of operation with error");
                e.printStackTrace();
                Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
                }
            }, error -> {
                downloadDialog.dismiss();
                Log.d(MainActivity.LOG_APP_NAME, "Network Error: Volley file download request failed"
                        + ", response given is " + error.getMessage() + ", stack trace is");
                error.printStackTrace();
                Toast.makeText(activity, R.string.a_network_error, Toast.LENGTH_SHORT).show();
            }, null){
                @NonNull
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("User-agent", USER_AGENT);
                    return headers;
                }
            };

            if (continueDownload.get()) {
                queue.add(request);
            }
        }
    }
}
