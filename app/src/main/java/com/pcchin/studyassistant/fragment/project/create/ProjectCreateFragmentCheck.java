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

package com.pcchin.studyassistant.fragment.project.create;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;

import java.util.Objects;

/** Functions for checking whether the inputs in ProjectCreateFragment are valid. **/
final class ProjectCreateFragmentCheck {
    private Context context;

    ProjectCreateFragmentCheck(Context context) {
        this.context = context;
    }

    /** Checks if the values in the project inputs match their requirements, used in onCreateView. **/
    boolean checkProjectValues(@NonNull TextInputLayout projectName,
                                       @NonNull TextInputLayout projectPass1,
                                       @NonNull TextInputLayout projectPass2,
                                       ProjectDatabase projectDatabase) {
        projectName.setErrorEnabled(false);
        projectPass1.setErrorEnabled(false);
        projectPass2.setErrorEnabled(false);

        if (projectName.getEditText() != null && projectPass1.getEditText() != null &&
                projectPass2.getEditText() != null) {
            if (projectName.getEditText().getText().toString().replaceAll
                    ("\\s+", "").length() == 0) {
                // Project name is empty
                projectName.setErrorEnabled(true);
                projectName.setError(context.getString(R.string.p6_error_project_name_empty));
                return false;
            } else if (projectDatabase.ProjectDao().searchByTitle(projectName
                    .getEditText().getText().toString()).size() > 0) {
                // Project name is taken
                projectName.setErrorEnabled(true);
                projectName.setError(context.getString(R.string.p6_error_project_exists));
                return false;
            }

            if (!Objects.equals(projectPass1.getEditText().getText().toString(),
                    projectPass2.getEditText().getText().toString())) {
                // Both passwords are not the same
                projectPass2.setErrorEnabled(true);
                projectPass2.setError(context.getString(R.string.error_password_unequal));
                return false;
            }

            if (projectPass1.getEditText().getText().length() > 0 &&
                    projectPass1.getEditText().getText().toString().length() < 8) {
                // Password length is too small
                projectPass1.setErrorEnabled(true);
                projectPass1.setError(context.getString(R.string.error_password_short));
                return false;
            }
        }
        return true;
    }

    /** Checks if the values in the member inputs match their requirements, used in onCreateView. **/
    boolean checkMemberValues(@NonNull TextInputLayout memberName,
                                      @NonNull TextInputLayout memberPass1,
                                      @NonNull TextInputLayout memberPass2) {
        memberName.setErrorEnabled(false);
        memberPass1.setErrorEnabled(false);
        memberPass2.setErrorEnabled(false);

        if (memberName.getEditText() != null && memberPass1.getEditText() != null &&
                memberPass2.getEditText() != null) {
            if (memberName.getEditText().getText().toString().replaceAll
                    ("\\s+", "").length() == 0) {
                // Project name is empty
                memberName.setErrorEnabled(true);
                memberName.setError(context.getString(R.string.p6_error_member_name_empty));
                return false;
            } else if (memberName.getEditText().getText().toString()
                    .replaceAll("\\s+", "").length()
                    != memberName.getEditText().getText().toString().length()) {
                // Username contains whitespace
                memberName.setErrorEnabled(true);
                memberName.setError(context.getString(R.string.v_error_username_whitespace));
            }

            if (!Objects.equals(memberPass1.getEditText().getText().toString(),
                    memberPass2.getEditText().getText().toString())) {
                // Both passwords are not the same
                memberPass2.setErrorEnabled(true);
                memberPass2.setError(context.getString(R.string.error_password_unequal));
                return false;
            }
            if (memberPass1.getEditText().getText().length() > 0 &&
                    memberPass1.getEditText().getText().toString().length() < 8) {
                // Password length is too small
                memberPass1.setErrorEnabled(true);
                memberPass1.setError(context.getString(R.string.error_password_short));
                return false;
            }
        }
        return true;
    }

    /** Checks if the values in the custom admin inputs match their requirements,
     * used in onCreateView. **/
    boolean checkCustomAdmin(@NonNull TextInputLayout customAdminName,
                             @NonNull TextInputLayout customAdminPass1,
                             @NonNull TextInputLayout customAdminPass2) {
        customAdminName.setErrorEnabled(false);
        customAdminPass1.setErrorEnabled(false);
        customAdminPass2.setErrorEnabled(false);

        if (customAdminName.getEditText() != null && customAdminPass1.getEditText() != null &&
                customAdminPass2.getEditText() != null) {
            if (customAdminName.getEditText().getText().toString().replaceAll
                    ("\\s+", "").length() == 0) {
                // Project name is empty
                customAdminName.setErrorEnabled(true);
                customAdminName.setError(context.getString(R.string.p6_error_member_name_empty));
                return false;
            }

            if (!Objects.equals(customAdminPass1.getEditText().getText().toString(),
                    customAdminPass2.getEditText().getText().toString())) {
                // Both passwords are not the same
                customAdminPass1.setErrorEnabled(true);
                customAdminPass1.setError(context.getString(R.string.error_password_unequal));
                return false;
            } else if (customAdminPass1.getEditText().getText().length() > 0 &&
                    customAdminPass1.getEditText().getText().toString().length() < 8) {
                // Password length is too small
                customAdminPass1.setErrorEnabled(true);
                customAdminPass1.setError(context.getString(R.string.error_password_short));
                return false;
            }
        }
        return true;
    }

    /** Checks if the values in the custom member inputs match their requirements,
     * used in onCreateView. **/
    boolean checkCustomMember(@NonNull TextInputLayout customMemberName,
                                      @NonNull TextInputLayout customMemberPass1,
                                      @NonNull TextInputLayout customMemberPass2) {
        customMemberName.setErrorEnabled(false);
        customMemberPass1.setErrorEnabled(false);
        customMemberPass2.setErrorEnabled(false);

        if (customMemberName.getEditText() != null && customMemberPass1.getEditText() != null &&
                customMemberPass2.getEditText() != null) {
            if (customMemberName.getEditText().getText().toString().replaceAll
                    ("\\s+", "").length() == 0) {
                // Project name is empty
                customMemberName.setErrorEnabled(true);
                customMemberName.setError(context.getString(R.string.p6_error_member_name_empty));
                return false;
            }

            if (!Objects.equals(customMemberPass1.getEditText().getText().toString(),
                    customMemberPass2.getEditText().getText().toString())) {
                // Both passwords are not the same
                customMemberPass1.setErrorEnabled(true);
                customMemberPass1.setError(context.getString(R.string.error_password_unequal));
                return false;
            } else if (customMemberPass1.getEditText().getText().length() > 0 &&
                    customMemberPass1.getEditText().getText().toString().length() < 8) {
                // Password length is too small
                customMemberPass1.setErrorEnabled(true);
                customMemberPass1.setError(context.getString(R.string.error_password_short));
                return false;
            }
        }
        return true;
    }
}
