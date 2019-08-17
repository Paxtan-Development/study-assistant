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

package com.pcchin.studyassistant.main.about;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.misc.AutoDismissDialog;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;

import java.util.Arrays;
import java.util.Objects;

public class LicenseFragment extends Fragment implements FragmentOnBackPressed {
    private static final int[] licenseArrays = new int[]{R.array.bouncycastle_license,
            R.array.jsoup_license, R.array.zip4j_license};

    /** Default constructor. **/
    public LicenseFragment() { }

    /** Initializes the fragment. Nothing to see here. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Creates the fragment. Sets all the license texts. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.blank_list, container, false);
        LinearLayout returnLayout = returnScroll.findViewById(R.id.blank_linear);

        for (int licenseArray : licenseArrays) {
            // Updates license info & OnClickListeners
            String[] infoArray = getResources().getStringArray(licenseArray);
            if (infoArray.length == 3 && getFragmentManager() != null) {
                @SuppressLint("InflateParams") LinearLayout licenseDisplay = (LinearLayout) inflater
                        .inflate(R.layout.license_display, null);
                // Common functions used by all licenses for neatness
                ((TextView) licenseDisplay.findViewById(R.id.m4_lib)).setText(infoArray[1]);
                DialogInterface.OnClickListener[] yListeners = new DialogInterface
                        .OnClickListener[]{null, (dialogInterface, i1) ->
                        dialogInterface.dismiss(), null};
                String[] buttonList = {"", getString(R.string.close), ""};

                if (Objects.equals(infoArray[0], getString(R.string.license_apache_2))) {
                    // Apache 2.0 license
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_apache_2);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(getActivity());
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + FileFunctions.getTxt(inflater.getContext(),
                                        "apache_2_license.txt"));
                        new AutoDismissDialog(getString(R.string.license_apache_2), licenseView,
                                buttonList, yListeners).show(getFragmentManager(),
                                "LicenseFragment.Apache2");
                    });
                } else if (Objects.equals(infoArray[0], getString(R.string.license_mit))) {
                    // MIT license
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_mit);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(getActivity());
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + FileFunctions.getTxt(inflater.getContext(),
                                        "mit_license.txt"));
                        new AutoDismissDialog(getString(R.string.license_mit), licenseView,
                                buttonList, yListeners).show(getFragmentManager(),
                                "LicenseFragment.Apache2");
                    });
                } else if (Objects.equals(infoArray[0], getString(R.string.license_cc_3_unported))) {
                    // Creative Commons CC 3.0 Unported
                    ((TextView) licenseDisplay.findViewById(R.id.m4_type)).setText(R.string.license_cc_3_unported);
                    licenseDisplay.setOnClickListener(view -> {
                        // TextView needs to be set for each instance to prevent error
                        TextView licenseView = new TextView(getActivity());
                        licenseView.setTextSize(18);
                        licenseView.setPadding(20, 20, 20, 20);
                        GeneralFunctions.setHtml(licenseView,
                                infoArray[2] + FileFunctions.getTxt(inflater.getContext(),
                                        "cc_3_unported.txt"));
                        new AutoDismissDialog(getString(R.string.license_cc_3_unported), licenseView,
                                buttonList, yListeners).show(getFragmentManager(),
                                "LicenseFragment.Apache2");
                    });
                }
                returnLayout.addView(licenseDisplay);
            } else {
                Log.w(MainActivity.LOG_APP_NAME, "XML Error: Incorrect CharSequence[] in " +
                        "license_display read, value is " + Arrays.toString(infoArray));
            }
        }
        return returnScroll;
    }

    /** Go back to
     * @see AboutFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new AboutFragment());
            return true;
        }
        return false;
    }
}
