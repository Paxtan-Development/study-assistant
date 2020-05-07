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

package com.pcchin.studyassistant.fragment.about.server;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;

import com.google.android.material.textfield.TextInputLayout;
import com.jaredrummler.android.device.DeviceName;
import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.functions.DataFunctions;
import com.pcchin.studyassistant.functions.NetworkFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.network.NetworkConstants;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class BugReportFragment extends Fragment implements ExtendedFragment {
    /** Default constructor. **/
    public BugReportFragment() {
        // Default constructor.
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ArrayList<Integer> issueList = DataFunctions.getAllResponses(requireActivity(),
                ActivityConstants.SHAREDPREF_BUG_ISSUE_LIST);
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.fragment_bug_report, container, false);
        if (issueList == null || issueList.size() == 0) {
            returnScroll.findViewById(R.id.m7_previous).setVisibility(View.GONE);
            returnScroll.findViewById(R.id.m7_previous_divider).setVisibility(View.GONE);
        } else {
            returnScroll.findViewById(R.id.m7_previous).setOnClickListener(view ->
                    UIFunctions.displayPreviousSubmissions(BugReportFragment.this, issueList,
                            ActivityConstants.SHAREDPREF_BUG_ISSUE_LIST));
        }
        returnScroll.findViewById(R.id.m7_return).setOnClickListener(view -> onBackPressed());
        returnScroll.findViewById(R.id.m7_submit).setOnClickListener(view -> {
            try {
                submitReport(returnScroll);
            } catch (JSONException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, "Network Error: Unable to create JSON object for feedback submission, stack trace is");
                e.printStackTrace();
            }
        });
        return returnScroll;
    }

    /** Submits the bug report for the fragment. **/
    private void submitReport(@NonNull ScrollView returnScroll) throws JSONException {
        Button submitButton = returnScroll.findViewById(R.id.m7_submit);
        TextInputLayout nameInput = returnScroll.findViewById(R.id.m7_name_input), emailInput = returnScroll.findViewById(R.id.m7_email_input),
                summaryInput = returnScroll.findViewById(R.id.m7_summary_input), descInput = returnScroll.findViewById(R.id.m7_description_input),
                stepsInput = returnScroll.findViewById(R.id.m7_steps_input);
        String name = Objects.requireNonNull(nameInput.getEditText()).getText().toString(), email = Objects.requireNonNull(emailInput.getEditText()).getText().toString(),
                summary = Objects.requireNonNull(summaryInput.getEditText()).getText().toString(), desc = Objects.requireNonNull(descInput.getEditText()).getText().toString(),
                steps = Objects.requireNonNull(stepsInput.getEditText()).getText().toString();
        boolean hasError = false;
        emailInput.setErrorEnabled(false);
        summaryInput.setErrorEnabled(false);
        descInput.setErrorEnabled(false);
        stepsInput.setErrorEnabled(false);
        if (email.replaceAll(ActivityConstants.EMAIL_REGEX, "").length() > 0) {
            emailInput.setErrorEnabled(true);
            emailInput.setError(getString(R.string.error_email_incorrect));
            hasError = true;
        }
        if (summary.replaceAll("\\s+", "").length() == 0) {
            summaryInput.setErrorEnabled(true);
            summaryInput.setError(getString(R.string.m_error_summary_blank));
            hasError = true;
        }
        if (desc.replaceAll("\\s+", "").length() == 0) {
            descInput.setErrorEnabled(true);
            descInput.setError(getString(R.string.m_error_desc_blank));
            hasError = true;
        }
        if (steps.replaceAll("\\s+", "").length() == 0) {
            stepsInput.setErrorEnabled(true);
            stepsInput.setError(getString(R.string.m_error_steps_blank));
            hasError = true;
        }
        if (!hasError) {
            sendBugReport(name, email, summary, desc, steps, submitButton);
        }
    }

    /** Creates the body for the bug request and sends it. **/
    private void sendBugReport(String name, String email, String summary, String desc, String steps, Button submitButton) throws JSONException {
        JSONObject uploadObject = new JSONObject();
        uploadObject.put("name", name);
        uploadObject.put("email", email);
        uploadObject.put("uid", requireActivity().getSharedPreferences(
                requireActivity().getPackageName(), Context.MODE_PRIVATE)
                .getString(ActivityConstants.SHAREDPREF_UID, ""));
        uploadObject.put("device", DeviceName.getDeviceName());
        uploadObject.put("version", BuildConfig.VERSION_NAME);
        uploadObject.put("summary", summary);
        uploadObject.put("description", desc);
        uploadObject.put("steps", steps);
        NetworkFunctions.sendPostRequest(((MainActivity) requireActivity()), NetworkConstants.BUG_PATH,
                uploadObject, ActivityConstants.SHAREDPREF_BUG_ISSUE_LIST, submitButton);
    }

    /** Returns to
     * @see AboutFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new AboutFragment());
        return true;
    }
}
