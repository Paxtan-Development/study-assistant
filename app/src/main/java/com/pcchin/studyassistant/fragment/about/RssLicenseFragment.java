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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.MainActivity;
import com.pcchin.studyassistant.ui.ExtendedFragment;

public class RssLicenseFragment extends Fragment implements ExtendedFragment {
    /** Default constructor. **/
    public RssLicenseFragment() {
        // Default constructor.
    }

    /** Initializes the fragment. Nothing to see here. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Creates the fragment. Sets the license text. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ScrollView returnScroll = new ScrollView(getActivity());

        // Set text
        TextView textView = new TextView(getActivity());
        textView.setTextSize(18);
        UIFunctions.setHtml(textView, FileFunctions.getTxt(inflater.getContext(),
                "rss_license.txt"));
        returnScroll.addView(textView);
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
