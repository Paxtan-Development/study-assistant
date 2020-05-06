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

package com.pcchin.studyassistant.network;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Functions that are unable to fit inside
 * @see AppUpdate **/
class AppUpdate2 {
    private static final String STACK_TRACE_IS = ", stack trace is";

    private MainActivity activity;
    private boolean calledFromNotif;

    /** Constructor for the functions. **/
    AppUpdate2(MainActivity activity, boolean calledFromNotif) {
        this.activity = activity;
        this.calledFromNotif = calledFromNotif;
    }

    /** Show users the update notification,
     * separated from checkServerUpdates() for clarity,
     * updateViaGithub(String downloadLink) separated for clarity. **/
    void showUpdateNotif(@NonNull JSONObject response, String host) {
        try {
            // Update so that it will not ask again on the same day
            SharedPreferences.Editor editor =
                    activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).edit();
            editor.putString(ActivityConstants.SHAREDPREF_LAST_UPDATE_CHECK, ConverterFunctions
                    .standardDateFormat.format(new Date()));
            editor.apply();

            // Get latest version from releases page
            if (!Objects.equals(response.getString("version").replace("v", ""),
                    BuildConfig.VERSION_NAME)) getVersionLinks(response);
        } catch (JSONException e) {
            Log.d(ActivityConstants.LOG_APP_NAME, "Network Error: Response returned by " + host
                    + " invalid, response given is " + response + ", error given is "
                    + e.getMessage());
        }
    }

    /** Get the download and release links and show them within the dialog. **/
    private void getVersionLinks(@NonNull JSONObject response) throws JSONException {
        String downloadLink = response.getString("download");
        String releaseLink = response.getString("page");
        if (!calledFromNotif) showUpdateAvailableNotif();
        // Show update dialog
        DismissibleDialogFragment updateDialog = new DismissibleDialogFragment(new AlertDialog.Builder(activity)
                .setTitle(R.string.a_update_app).setMessage(R.string.a_new_version).create());
        updateDialog.setPositiveButton(activity.getString(android.R.string.yes), view -> {
            updateDialog.dismiss();
            updateViaGithub(downloadLink);
        });
        updateDialog.setNegativeButton(activity.getString(android.R.string.no), view -> updateDialog.dismiss());
        updateDialog.setNeutralButton(activity.getString(R.string.a_learn_more), view -> {
            // The user should be able to update after coming back from the website
            activity.safeOnBackPressed();
            Intent gitlabReleaseSite = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(releaseLink));
            activity.startActivity(gitlabReleaseSite);
        });
        updateDialog.show(activity.getSupportFragmentManager(), "AppUpdate.1");
    }

    /** Displays a notification that an update is available. **/
    private void showUpdateAvailableNotif() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ActivityConstants.INTENT_VALUE_DISPLAY_UPDATE, true);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) notif.setChannelId(activity.getString(R.string.notif_channel_update_ID));
        NotificationManagerCompat manager = NotificationManagerCompat.from(activity);
        manager.notify(activity.getTaskId(), notif.build());
    }

    /** Download and update the newest version of the app via Github,
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
            displayDownloadDialog(queue, continueDownload, progressBar, downloadLink, outputFileName);
        }
    }

    /** Displays the download dialog and starts the download process. **/
    private void displayDownloadDialog(RequestQueue queue, @NonNull AtomicBoolean continueDownload,
                                       ProgressBar progressBar, String downloadLink, String outputFileName) {
        DialogInterface.OnDismissListener dismissListener = dialogInterface -> {
            Log.d(ActivityConstants.LOG_APP_NAME, "Notification: Download of latest APK cancelled");
            queue.stop();
            continueDownload.set(false);
        };
        AlertDialog downloadDialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.a_downloading)
                .setView(progressBar)
                .setPositiveButton(android.R.string.cancel, null)
                .create();
        downloadDialog.setCancelable(false);
        downloadDialog.setOnDismissListener(dismissListener);
        DefaultDialogFragment dialogFragment = new DefaultDialogFragment(downloadDialog);
        dialogFragment.show(activity.getSupportFragmentManager(), "AppUpdate.2");
        VolleyFileDownloadRequest request = getDownloadRequest(dialogFragment, queue, downloadLink, outputFileName);
        if (continueDownload.get()) {
            queue.add(request);
        }
    }

    /** Returns the download request for the APK file. **/
    @NonNull
    private VolleyFileDownloadRequest getDownloadRequest(DefaultDialogFragment downloadDialog,
                                                         RequestQueue queue, String downloadLink, String outputFileName) {
        return new VolleyFileDownloadRequest(Request.Method.GET, downloadLink,
                response -> tryCreateApk(downloadDialog, queue, response, outputFileName), error -> {
            downloadDialog.dismiss();
            Log.d(ActivityConstants.LOG_APP_NAME, "Network Error: Volley file download request failed"
                    + ", response given is " + error.getMessage() + STACK_TRACE_IS);
            error.printStackTrace();
            Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
        }, null){
            @NonNull
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-agent", NetworkConstants.USER_AGENT);
                return headers;
            }
        };
    }

    /** The try / catch blocks for createApk. **/
    private void tryCreateApk(DefaultDialogFragment downloadDialog, RequestQueue queue,
                              byte[] response, String outputFileName) {
        try {
            createApk(downloadDialog, queue, response, outputFileName);
        } catch (FileNotFoundException e) {
            Log.d(ActivityConstants.LOG_APP_NAME, "File Error: File" + outputFileName
                    + " not found" + STACK_TRACE_IS);
            e.printStackTrace();
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        } catch (IOException e2) {
            Log.d(ActivityConstants.LOG_APP_NAME, "File Error: An IOException occurred at " + outputFileName
                    + STACK_TRACE_IS);
            e2.printStackTrace();
            Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.d(ActivityConstants.LOG_APP_NAME, "Error: Volley download request failed " +
                    "in middle of operation with error");
            e.printStackTrace();
            Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates the APK file in the downloads directory. **/
    private void createApk(@NonNull DefaultDialogFragment downloadDialog, @NonNull RequestQueue queue,
                           byte[] response, String outputFileName) throws IOException {
        downloadDialog.dismiss();
        queue.stop();
        if (response != null) {
            File outputFile = new File(outputFileName);
            if (outputFile.createNewFile()) {
                writeApk(response, outputFileName, outputFile);
            } else {
                Log.d(ActivityConstants.LOG_APP_NAME, "File Error: File " + outputFileName
                        + " could not be created.");
                Toast.makeText(activity, R.string.file_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Writes the downloaded APK into the file. **/
    private void writeApk(byte[] response, String outputFileName, File outputFile) throws IOException {
        SharedPreferences.Editor editor = activity.getSharedPreferences(
                activity.getPackageName(), Context.MODE_PRIVATE).edit();
        editor.putString(ActivityConstants.SHAREDPREF_APP_UPDATE_PATH, outputFileName);
        editor.apply();

        // Write output file with buffer
        InputStream input = new ByteArrayInputStream(response);
        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            byte[] data = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            installApp(outputFileName);
        }
    }

    /** Installs the new app from the given file path. **/
    private void installApp(String outputFileName) {
        activity.safeOnBackPressed();
        Toast.makeText(activity, R.string.a_app_updating, Toast.LENGTH_SHORT).show();
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(FileProvider.getUriForFile(activity,
                activity.getPackageName() + ".ContentProvider",
                new File(outputFileName)),
                "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(installIntent);
    }
}
