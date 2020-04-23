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

import com.pcchin.studyassistant.R;

/** The preference that adds a password with double checks. **/
public class PasswordPreference extends DialogPreference {
    public PasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PasswordPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /** Stores the password for it to be handled within ProjectSettingsFragment. **/
    void setPassword(String hashedPass) {
        persistString(hashedPass);
        getOnPreferenceChangeListener().onPreferenceChange(PasswordPreference.this, hashedPass);
    }

    /** Returns the layout resource of the DatePicker. **/
    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_password_edittext;
    }

    /** Stub as nothing is stored in the SharedPreference. **/
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    /** Stub as nothing is stored in the SharedPreference. **/
    @Override
    protected void onSetInitialValue(Object defaultValue) {

    }
}
