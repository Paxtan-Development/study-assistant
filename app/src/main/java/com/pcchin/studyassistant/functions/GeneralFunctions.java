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

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;

/** Functions generally used throughout the app. **/
public final class GeneralFunctions {
    /** Exits the app.**/
    public static void exitApp(@NonNull Activity activity) {
        activity.moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /** Reloads a fragment. **/
    public static void reloadFragment(@NonNull Fragment target) {
        target.getParentFragmentManager().beginTransaction()
                .detach(target)
                .attach(target).commit();
    }

    /** Get the connection status from the connectivity manager. **/
    public static boolean getConnected(ConnectivityManager cm) {
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

    /** Converts the message into a JSON String with an attribute named message. **/
    public static String getServerJson(String message) {
        return message == null ? null: new Gson().toJson(new PlaceholderJsonClass(message));
    }

    /** Class used for conversion of a String to a class to be parsed by GSON. **/
    static class PlaceholderJsonClass {
        String message;
        PlaceholderJsonClass(String message) {
            this.message = message;
        }
    }

}
