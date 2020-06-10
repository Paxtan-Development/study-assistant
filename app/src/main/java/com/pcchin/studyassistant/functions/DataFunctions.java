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
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

import io.sentry.Sentry;

/** Functions used for processing data throughout the app. **/
public final class DataFunctions {
    private DataFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Function that is called if the project appears to be missing. **/
    public static void onProjectMissing(MainActivity activity, @NonNull ProjectDatabase projectDatabase) {
        // Go back to project selection
        Toast.makeText(activity, R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
        projectDatabase.close();
        activity.displayFragment(new ProjectSelectFragment());
    }

    /** Returns the Shared Preferences for the app. **/
    @NonNull
    public static SharedPreferences getSharedPref(@NonNull Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    /** Gets the encrypted shared preferences for the app.
     * If the build version is below 23, the normal Shared Preferences will be used. **/
    public static SharedPreferences getEncSharedPref(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                return EncryptedSharedPreferences.create(ActivityConstants.ENC_SHAREDPREF_FILE_NAME,
                        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC), context, EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
            } catch (GeneralSecurityException | IOException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, "Security Error: Unable to get the " +
                        "encrypted shared preferences for the app. Stack trace is");
                e.printStackTrace();
                Sentry.capture(e);
                return null;
            }
        } else {
            return getSharedPref(context);
        }
    }
}
