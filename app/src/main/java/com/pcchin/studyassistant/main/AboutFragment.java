/*
 * Copyright 2019 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.main;

import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pcchin.studyassistant.BuildConfig;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;

import java.util.Calendar;
import java.util.Locale;

public class AboutFragment extends Fragment implements FragmentOnBackPressed {

    /** Default Constructor **/
    public AboutFragment() {}

    /** Initializes the fragment. Nothing to see here. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.app_name_release);
        }
    }

    /** Creates the fragment. Sets the version, current year and license text. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_about, container, false);
        // Set version
        TextView textView = returnView.findViewById(R.id.m2_version);
        textView.setText(String.format("%s%s", getString(R.string.m2_version), BuildConfig.VERSION_NAME));

        // Set current year
        TextView copyrightView = returnView.findViewById(R.id.m2_copyright);
        copyrightView.setText(String.format(Locale.ENGLISH, "%s%d %s",
                getString(R.string.m2_copyright_p1), Calendar.getInstance().get(Calendar.YEAR),
                getString(R.string.m2_copyright_p2)));

        // Set license text
        Spanned license;
        String licenseText = FileFunctions.getTxt(inflater.getContext(),
                "license.txt");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            license = Html.fromHtml(licenseText, Html.FROM_HTML_MODE_LEGACY); // Adds hyperlink to text
        } else {
            license = Html.fromHtml(licenseText);
        }
        TextView licenseView = returnView.findViewById(R.id.m2_license);
        licenseView.setText(license);
        licenseView.setMovementMethod(LinkMovementMethod.getInstance());
        return returnView;
    }

    /** Go back to
     * @see MainFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new MainFragment());
            return true;
        }
        return false;
    }
}
