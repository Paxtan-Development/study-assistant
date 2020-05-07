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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.functions.DataFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.network.server.FeedbackSubmission;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class FeedbackFragment extends Fragment implements ExtendedFragment {
    /** Default constructor. **/
    public FeedbackFragment() {
        // Default constructor.
    }

    /** Initializes the fragment. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Creates the view for the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ArrayList<Integer> issueList = DataFunctions.getAllResponses(requireActivity(),
                ActivityConstants.SHAREDPREF_FEEDBACK_ISSUE_LIST);
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.fragment_about_feedback, container, false);
        if (issueList == null || issueList.size() == 0) {
            returnScroll.findViewById(R.id.m6_previous).setVisibility(View.GONE);
            returnScroll.findViewById(R.id.m6_previous_divider).setVisibility(View.GONE);
        } else {
            returnScroll.findViewById(R.id.m6_previous).setOnClickListener(view -> displayPreviousSubmissions(issueList));
        }
        returnScroll.findViewById(R.id.m6_return).setOnClickListener(view -> onBackPressed());
        returnScroll.findViewById(R.id.m6_submit).setOnClickListener(view -> {
            try {
                submitFeedback(returnScroll);
            } catch (JSONException e) {
                Log.e(ActivityConstants.LOG_APP_NAME, "Network Error: Unable to create JSON object for feedback submission, stack trace is");
                e.printStackTrace();
            }
        });
        return returnScroll;
    }

    /** Displays the previous feedback submissions if the device is connected to the internet. **/
    private void displayPreviousSubmissions(@NonNull ArrayList<Integer> issueList) {
        if (GeneralFunctions.getConnected(
                (ConnectivityManager)requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE))) {
            @SuppressLint("InflateParams") ScrollView blankScroll =
                    (ScrollView) getLayoutInflater().inflate(R.layout.blank_list, null);
            LinearLayout blankLinear = blankScroll.findViewById(R.id.blank_linear);
            for (int issue : issueList) {
                addIssue(issue, blankLinear);
            }
            new DefaultDialogFragment(new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.m6_previous_submissions)
                    .setView(blankScroll)
                    .setPositiveButton(R.string.close, null).create())
                    .show(getParentFragmentManager(), "FeedbackFragment.1");
        } else {
            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Adds an issue to the AlertDialog, and if it does not exist,
     * add it on to the list to be deleted. **/
    private void addIssue(int issue, @NonNull LinearLayout blankLinear) {
        @SuppressLint("InflateParams")
        View currentIssue = getLayoutInflater().inflate(R.layout.m_issue, null);
        TextView issueTitle = currentIssue.findViewById(R.id.m_issue_title);
        issueTitle.setText(String.format(Locale.ENGLISH, "Issue #%d", issue));
        issueTitle.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://github.com/Paxtan-Development/study-assistant/issues/" + issue));
            startActivity(intent);
        });
        currentIssue.findViewById(R.id.m_issue_del).setOnClickListener(view -> {
            DataFunctions.removeResponse(requireActivity(), ActivityConstants.SHAREDPREF_FEEDBACK_ISSUE_LIST, issue);
            DefaultDialogFragment dialogFragment = (DefaultDialogFragment)
                    getParentFragmentManager().findFragmentByTag("FeedbackFragment.1");
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
            GeneralFunctions.reloadFragment(FeedbackFragment.this);
        });
        blankLinear.addView(currentIssue);
    }

    /** Submit the feedback for the fragment. **/
    private void submitFeedback(@NonNull ScrollView returnScroll) throws JSONException {
        TextInputLayout nameInput = returnScroll.findViewById(R.id.m6_name_input), emailInput = returnScroll.findViewById(R.id.m6_email_input),
            summaryInput = returnScroll.findViewById(R.id.m6_summary_input), descInput = returnScroll.findViewById(R.id.m6_description_input);
        String name = Objects.requireNonNull(nameInput.getEditText()).getText().toString(), email = Objects.requireNonNull(emailInput.getEditText()).getText().toString(),
                summary = Objects.requireNonNull(summaryInput.getEditText()).getText().toString(), desc = Objects.requireNonNull(descInput.getEditText()).getText().toString();
        boolean hasError = false;
        emailInput.setErrorEnabled(false);
        summaryInput.setErrorEnabled(false);
        descInput.setErrorEnabled(false);
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
        if (!hasError) new FeedbackSubmission((MainActivity) getActivity()).sendFeedback(name, email, summary, desc);
    }

    /** Returns to
     * @see AboutFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new AboutFragment());
        return true;
    }
}
