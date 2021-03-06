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

package com.pcchin.studyassistant.utils.misc;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;

/** Functions to check if the inputs provided are correct, and display an error if not. **/
public class InputValidation {
    private final Context context;

    /** Constructor used as context needs to be passed on. **/
    public InputValidation(Context context) {
        this.context = context;
    }

    /** Check if the email provided has any errors and if yes, display the error.
     * Returns whether the email has any error. **/
    public boolean emailHasError(@NonNull String email, TextInputLayout emailInput) {
        if (email.replaceAll(ActivityConstants.EMAIL_REGEX, "").length() > 0) {
            emailInput.setErrorEnabled(true);
            emailInput.setError(context.getString(R.string.error_email_incorrect));
            return true;
        } else {
            return false;
        }
    }

    /** Check if the input provided has any error and if yes, display the specific error string. **/
    public boolean inputIsBlank(@NonNull String inputString, TextInputLayout inputTextLayout, int errorString) {
        if (inputString.replaceAll("\\s+", "").length() == 0) {
            inputTextLayout.setErrorEnabled(true);
            inputTextLayout.setError(context.getString(errorString));
            return true;
        } else {
            return false;
        }
    }
}
