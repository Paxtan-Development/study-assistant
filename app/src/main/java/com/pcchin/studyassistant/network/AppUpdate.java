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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Class that is used to check Gitlab for updates to the app, separated from
 * @see MainActivity for clarity.
 * Cannot be made static as gitlabReleasesStatusCode needs to be passed on from function to function. **/
public class AppUpdate {
    private static final String MAIN_API = "https://api.paxtan.dev";
    private static final String BACKUP_API = "https://api.pcchin.com";
    private static final String SEC_BACKUP_API = "https://paxtandev.herokuapp.com";
    @SuppressWarnings("ConstantConditions")
    private static final String UPDATE_PATH = BuildConfig.BUILD_TYPE.equals("beta")
            ? "/study-assistant/beta" : "/study-assistant/latest";
    /* Example user agent: "Study-Assistant/1.5 (...)" */
    @SuppressWarnings("ConstantConditions")
    static final String USER_AGENT = System.getProperty("http.agent","")
            .replaceAll("^.+?/\\S+", String.format("Study-Assistant/%s", BuildConfig.VERSION_NAME));
    private static final String STACK_CASE_IS = ", stack trace is";

    private final boolean calledFromNotif;
    private final MainActivity activity;

    /** Checks for version updates of the app, doubles as the constructor.
     * checkServerUpdates() separated for clarity. **/
    public AppUpdate(@NonNull MainActivity activity, boolean calledFromNotif) {
        this.activity = activity;
        this.calledFromNotif = calledFromNotif;

        // Check if network is connected (Updated code as old code is deprecated)
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = getConnected(cm);

        // Check if there is a newer version of the app
        if (isConnected && !isFromPlayStore()) checkServerUpdates();
    }

    /** Get the connection status from the connectivity manager. **/
    private boolean getConnected(ConnectivityManager cm) {
        if (Build.VERSION.SDK_INT < 23) {
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                return ni.isConnected();
            }
        } else {
            final Network n = cm.getActiveNetwork();
            if (n != null) {
                final NetworkCapabilities nc = cm.getNetworkCapabilities(n);
                if (nc != null) {
                    return nc.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }
        }
        return false;
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
        JsonObjectRequest getSecBackupReleases = getReleasesRequest(SEC_BACKUP_API, queue,
                null);
        // Backup Server
        JsonObjectRequest getBackupReleases = getReleasesRequest(BACKUP_API, queue, getSecBackupReleases);
        // Main Server
        JsonObjectRequest getReleases = getReleasesRequest(MAIN_API, queue, getBackupReleases);

        // Send request
        queue.add(getReleases);
    }

    /** Generates the GET request for the release. **/
    @NonNull
    private JsonObjectRequest getReleasesRequest(String apiUrl, RequestQueue queue,
                                                 JsonObjectRequest secondaryRelease) {
        return new JsonObjectRequest(apiUrl + UPDATE_PATH, null,
                response -> new AppUpdate2(activity, calledFromNotif).showUpdateNotif(response, apiUrl), error -> {
            Log.d(ActivityConstants.LOG_APP_NAME, "Network Error: Volley returned error "
                    + error.getMessage() + ":" + error.toString() + " from " + apiUrl + STACK_CASE_IS );
            error.printStackTrace();
            Log.d(ActivityConstants.LOG_APP_NAME, "Attempting to connect to backup server");
            if (secondaryRelease == null) {
                queue.stop();
            } else {
                queue.add(secondaryRelease);
            }
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
    }
}
