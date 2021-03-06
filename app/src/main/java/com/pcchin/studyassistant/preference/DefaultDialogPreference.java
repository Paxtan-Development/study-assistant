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

package com.pcchin.studyassistant.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

/** The preference that shows a dialog and displays 2 buttons.
 * An integer would be stored if the positive button is pressed. **/
public class DefaultDialogPreference extends DialogPreference {

    public DefaultDialogPreference(Context context) {
        super(context);
    }

    public DefaultDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DefaultDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DefaultDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Stub as nothing is stored in the SharedPreference. **/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    /** Stub as nothing is stored in the SharedPreference. **/
    @Override
    protected void onSetInitialValue(Object defaultValue) {
        // Stub as nothing is stored in the SharedPreference.
    }

    /** Stores a value into the SharedPreference when the positive value is clicked. **/
    void positiveClicked() {
        persistInt(0);
        getOnPreferenceChangeListener().onPreferenceChange(DefaultDialogPreference.this, 0);
    }
}
