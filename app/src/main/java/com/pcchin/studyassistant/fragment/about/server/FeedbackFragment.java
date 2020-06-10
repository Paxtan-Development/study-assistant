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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.utils.misc.InputValidation;

import java.util.Objects;

import io.sentry.Sentry;
import io.sentry.event.Event;
import io.sentry.event.EventBuilder;

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
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.fragment_about_feedback, container, false);
        returnScroll.findViewById(R.id.m6_return).setOnClickListener(view -> onBackPressed());
        returnScroll.findViewById(R.id.m6_submit).setOnClickListener(view -> submitFeedback(returnScroll));
        return returnScroll;
    }

    /** Submit the feedback for the fragment. **/
    private void submitFeedback(@NonNull ScrollView returnScroll) {
        TextInputLayout nameInput = returnScroll.findViewById(R.id.m6_name_input), emailInput = returnScroll.findViewById(R.id.m6_email_input),
            summaryInput = returnScroll.findViewById(R.id.m6_summary_input), descInput = returnScroll.findViewById(R.id.m6_description_input);
        String name = Objects.requireNonNull(nameInput.getEditText()).getText().toString(), email = Objects.requireNonNull(emailInput.getEditText()).getText().toString(),
                summary = Objects.requireNonNull(summaryInput.getEditText()).getText().toString(), desc = Objects.requireNonNull(descInput.getEditText()).getText().toString();
        boolean hasError = false;
        emailInput.setErrorEnabled(false);
        summaryInput.setErrorEnabled(false);
        descInput.setErrorEnabled(false);
        InputValidation validator = new InputValidation(getContext());
        if (validator.emailHasError(email, emailInput)) hasError = true;
        if (validator.inputIsBlank(summary, summaryInput, R.string.m_error_summary_blank)) hasError = true;
        if (validator.inputIsBlank(desc, descInput, R.string.m_error_desc_blank)) hasError = true;
        if (!hasError) sendFeedback(name, email, summary, desc);
    }

    /** Send the feedback request to the server. **/
    private void sendFeedback(String name, String email, String summary, String desc) {
        Sentry.capture(new EventBuilder().withLevel(Event.Level.INFO)
                .withMessage("Feedback Request: " + summary).withExtra("Name", name)
                .withExtra("Email", email).withExtra("Summary", summary)
                .withExtra("Description", desc));
        Toast.makeText(getContext(), R.string.event_sent, Toast.LENGTH_SHORT).show();
        ((MainActivity) requireActivity()).displayFragment(new AboutFragment());
    }

    /** Returns to
     * @see AboutFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new AboutFragment());
        return true;
    }
}
