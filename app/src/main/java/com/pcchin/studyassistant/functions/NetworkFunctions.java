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

package com.pcchin.studyassistant.functions;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.pcchin.auto_app_updater.AutoAppUpdater;
import com.pcchin.auto_app_updater.endpoint.custom.JSONObjectEndpoint;
import com.pcchin.auto_app_updater.endpoint.repo.GitHubEndpoint;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.network.NetworkConstants;

import java.util.Arrays;
import java.util.List;

/** Functions used for network related tasks within the app. **/
public final class NetworkFunctions {
    private NetworkFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Starts the AutoAppUpdater which checks for updates to the app. **/
    public static void checkForUpdates(AppCompatActivity activity) {
        if (!isFromPlayStore(activity)) {
            // Creates the updater
            AutoAppUpdater.Builder builder = new AutoAppUpdater.Builder(activity,
                    activity.getSupportFragmentManager(),
                    activity.getPackageName() + ".ContentProvider");
            // Sets the attributes
            builder.setUpdateType(AutoAppUpdater.UpdateType.DIFFERENCE);
            builder.setCurrentVersion(BuildConfig.VERSION_NAME);
            addEndpoints(builder);
            // Sets the update interval
            //noinspection ConstantConditions
            if (BuildConfig.BUILD_TYPE.equals("debug")) builder.setUpdateInterval(0);
            else builder.setUpdateInterval(60 * 60 * 24);
            // Runs the updater
            builder.build().run();
        }
    }

    /** Adds the endpoints that is used by the updater. **/
    private static void addEndpoints(@NonNull AutoAppUpdater.Builder builder) {
        JSONObjectEndpoint mainApi = getApiEndpoint(NetworkConstants.MAIN_API);
        JSONObjectEndpoint secondaryApi = getApiEndpoint(NetworkConstants.BACKUP_API);
        JSONObjectEndpoint tertiaryApi = getApiEndpoint(NetworkConstants.SEC_BACKUP_API);
        //noinspection ConstantConditions
        GitHubEndpoint gitHubEndpoint = new GitHubEndpoint("Paxtan-Development/study-assistant",
                BuildConfig.BUILD_TYPE.equals("beta"));
        builder.addEndpoints(mainApi, secondaryApi, tertiaryApi, gitHubEndpoint);
    }

    /** Gets the endpoint from the given website that is used to update the app. **/
    @NonNull
    private static JSONObjectEndpoint getApiEndpoint(String apiPath) {
        return new JSONObjectEndpoint(apiPath + NetworkConstants.UPDATE_PATH,
                "version", "download", "page");
    }

    /** Checks if the app is downloaded from the Play Store, separated for clarity. **/
    private static boolean isFromPlayStore(@NonNull Context context) {
        // A list with valid installers package name
        List<String> validInstallers = Arrays.asList("com.android.vending", "com.google.android.feedback");
        // The package name of the app that has installed your app
        final String installer = context.getPackageManager().getInstallerPackageName(context.getPackageName());
        // true if your app has been downloaded from Play Store
        return installer != null && validInstallers.contains(installer);
    }
}
