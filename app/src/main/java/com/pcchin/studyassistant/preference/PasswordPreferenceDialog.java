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

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.SecurityFunctions;

import java.util.Objects;

/** The dialog for PasswordPreference. **/
public final class PasswordPreferenceDialog extends PreferenceDialogFragmentCompat {
    private String salt;
    private TextInputLayout input1, input2;

    /** Creates a new instance of this dialog.
     * @param key The key of the preference selected.
     * @param salt The salt of the current project. **/
    @NonNull
    public static PasswordPreferenceDialog newInstance(String key, String salt) {
        final PasswordPreferenceDialog
                fragment = new PasswordPreferenceDialog();
        final Bundle bundle = new Bundle(1);
        bundle.putString(ARG_KEY, key);
        fragment.setArguments(bundle);
        fragment.salt = salt;
        return fragment;
    }

    /** Initializes the view and sets up the EditTexts. **/
    @Override
    protected void onBindDialogView(@NonNull View view) {
        input1 = view.findViewById(R.id.pref_input1_input);
        input2 = view.findViewById(R.id.pref_input2_input);
    }

    /** Only save the password if the input is correct. **/
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            if (input1.getEditText() != null && input2.getEditText() != null) {
                String inputText1 = input1.getEditText().getText().toString(),
                        inputText2 = input2.getEditText().getText().toString();
                boolean bothCorrect = true;
                if (!Objects.equals(inputText1, inputText2)) {
                    Toast.makeText(requireContext(), R.string.error_password_unequal, Toast.LENGTH_SHORT).show();
                    bothCorrect = false;
                } else if (inputText1.length() < 8) {
                    Toast.makeText(requireContext(), R.string.error_password_short, Toast.LENGTH_SHORT).show();
                    bothCorrect = false;
                }

                if (bothCorrect) {
                    super.onClick(dialog, which);
                }
            }
        } else {
            super.onClick(dialog, which);
        }
    }

    /** Stores the values of the updated password into the database. **/
    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult && input1.getEditText() != null) {
            ((PasswordPreference) getPreference()).setPassword(
                    SecurityFunctions.projectHash(input1.getEditText().getText().toString(), salt));
        }
    }
}
