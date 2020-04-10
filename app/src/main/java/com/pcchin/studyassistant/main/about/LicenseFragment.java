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

package com.pcchin.studyassistant.main.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.licenseview.LicenseView;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.display.ExtendedFragment;
import com.pcchin.studyassistant.main.MainActivity;

public class LicenseFragment extends Fragment implements ExtendedFragment {
    private static final int[] licenseArrays = new int[]{R.array.bouncycastle_license,
            R.array.jsoup_license, R.array.licenseview_license, R.array.zip4j_license};

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
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.fragment_license, container, false);
        LicenseView licenseView = returnScroll.findViewById(R.id.m5_linear);

        for (int licenseArray : licenseArrays) {
            // Updates license info & OnClickListeners
            String[] infoArray = getResources().getStringArray(licenseArray);
            licenseView.addLicense(infoArray);
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
