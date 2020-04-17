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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public final class DefaultDialogPreferenceDialog extends PreferenceDialogFragmentCompat {
    private DefaultDialogPreference preference;

    /** Creates a new instance of this dialog.
     * @param key The key of the preference selected. **/
    @NonNull
    public static DefaultDialogPreferenceDialog newInstance(String key) {
        final DefaultDialogPreferenceDialog fragment = new DefaultDialogPreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        return fragment;
    }

    /** Sets the preference of the dialog and customizes the AlertDialog. **/
    public DefaultDialogPreferenceDialog setPreference(DefaultDialogPreference preference) {
        this.preference = preference;
        return this;
    }

    /** Forwards the result to
     * @see DefaultDialogPreference **/
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            preference.positiveClicked();
        }
    }
}
