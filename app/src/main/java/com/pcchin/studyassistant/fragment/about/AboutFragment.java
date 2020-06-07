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

package com.pcchin.studyassistant.fragment.about;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.fragment.about.license.LicenseFragment;
import com.pcchin.studyassistant.fragment.about.license.RssLicenseFragment;
import com.pcchin.studyassistant.fragment.about.server.BugReportFragment;
import com.pcchin.studyassistant.fragment.about.server.FeedbackFragment;
import com.pcchin.studyassistant.fragment.main.MainFragment;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.util.Calendar;
import java.util.Locale;

public class AboutFragment extends Fragment implements ExtendedFragment {
    /** Default Constructor. **/
    public AboutFragment() {
        // Default constructor.
    }

    /** Initializes the fragment. Nothing to see here. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.app_name);
    }

    /** Creates the fragment. Sets the version, current year and license text. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_about, container, false);
        String uid = requireActivity().getSharedPreferences(requireActivity().getPackageName(),
                Context.MODE_PRIVATE).getString(ActivityConstants.SHAREDPREF_UID, "");
        ((TextView) returnView.findViewById(R.id.m2_version)).setText(String.format("%s%s", getString(R.string.m2_version), BuildConfig.VERSION_NAME));
        ((TextView) returnView.findViewById(R.id.m2_copyright)).setText(String.format(Locale.ENGLISH, "%s%d %s",
                getString(R.string.m2_copyright_p1), Calendar.getInstance().get(Calendar.YEAR),
                getString(R.string.m2_copyright_p2)));

        returnView.findViewById(R.id.m2_library_license).setOnClickListener(view ->
                ((MainActivity) requireActivity()).displayFragment(new LicenseFragment()));
        returnView.findViewById(R.id.m2_rss_license).setOnClickListener(view ->
                ((MainActivity) requireActivity()).displayFragment(new RssLicenseFragment()));
        ((TextView) returnView.findViewById(R.id.m2_uid)).setText(String.format("%s%s", getString(R.string.m2_uid), uid));
        returnView.findViewById(R.id.m2_bug_report).setOnClickListener(view ->
                ((MainActivity) requireActivity()).displayFragment(new BugReportFragment()));
        returnView.findViewById(R.id.m2_feature_suggestion).setOnClickListener(view ->
                ((MainActivity) requireActivity()).displayFragment(new FeedbackFragment()));

        // Set license text
        UIFunctions.setHtml(returnView.findViewById(R.id.m2_apache), FileFunctions.getTxt(
                inflater.getContext(), "studyassistant_about.txt"));

        return returnView;
    }

    /** Go back to
     * @see MainFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new MainFragment());
        return true;
    }
}
