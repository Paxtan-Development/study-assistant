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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/** Class that is use to submit feedback for the app. **/
public class FeedbackSubmission {
    private String encryptedString;
    private MainActivity activity;

    /** Constructor for the class as activity needs to be passed on. **/
    public FeedbackSubmission(MainActivity activity) {
        this.activity = activity;
    }

    /** Send the feedback request to the server. **/
    public void sendFeedback(String name, String email, String summary, String desc) throws JSONException {
        JSONObject uploadObject = new JSONObject();
        uploadObject.put("name", name);
        uploadObject.put("email", email);
        uploadObject.put("summary", summary);
        uploadObject.put("description", desc);
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        encryptedString = GeneralFunctions.getServerJson(SecurityFunctions.RSAServerEncrypt(activity, uploadObject.toString()));

        if (encryptedString == null) {
            Log.e(ActivityConstants.LOG_APP_NAME, "Network Error: Unable to encrypt JSON Object using RSA.");
            Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
        } else if (GeneralFunctions.getConnected(cm)) {
            RequestQueue queue = Volley.newRequestQueue(activity);
            StringRequest postSecBackupFeedback = getFeedbackRequest(NetworkConstants.SEC_BACKUP_API, queue, null),
                    postBackupFeedback = getFeedbackRequest(NetworkConstants.BACKUP_API, queue, postSecBackupFeedback),
                    postFeedback = getFeedbackRequest(NetworkConstants.MAIN_API, queue, postBackupFeedback);
            queue.add(postFeedback);
        } else {
            Toast.makeText(activity, R.string.error_not_connected, Toast.LENGTH_SHORT).show();
        }
    }

    /** Gets the JSON request used to send the feedback. **/
    @NonNull
    private StringRequest getFeedbackRequest(String apiUrl, RequestQueue queue,
                                             StringRequest secondaryRelease) {
        return new StringRequest(Request.Method.POST, apiUrl + NetworkConstants.FEEDBACK_PATH, response -> {
            // TODO: Store response
            Log.d(ActivityConstants.LOG_APP_NAME, "Response: " + response);
            activity.displayFragment(new AboutFragment());
            Toast.makeText(activity, R.string.m6_submitted, Toast.LENGTH_SHORT).show();
        }, error -> {
            Log.d(ActivityConstants.LOG_APP_NAME, "Network Error: Volley returned error "
                    + error.getMessage() + ":" + error.toString() + " from " + apiUrl + NetworkConstants.FEEDBACK_PATH + ", stack trace is");
            error.printStackTrace();
            Log.d(ActivityConstants.LOG_APP_NAME, "Attempting to connect to backup server");
            if (secondaryRelease == null) {
                queue.stop();
                Toast.makeText(activity, R.string.network_error, Toast.LENGTH_SHORT).show();
            } else {
                queue.add(secondaryRelease);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "text/*, application/json");
                headers.put("User-agent", NetworkConstants.USER_AGENT);
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return encryptedString.getBytes();
            }
        };
    }
}
