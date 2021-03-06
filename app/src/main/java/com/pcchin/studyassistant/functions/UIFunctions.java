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

package com.pcchin.studyassistant.functions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;

/** Functions used for UI elements in the app. **/
public final class UIFunctions {
    private UIFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Shows the dialog to add a new subject to the notes list **/
    public static void showNewSubject(@NonNull final MainActivity activity) {
        @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        popupView.setHint(activity.getString(R.string.n1_subject_title));

        // End icon has been set by default in XML file
        // OnClickListeners implemented separately to prevent dialog from being dismissed after button click
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.n1_new_subject)
                        .setView(popupView)
                        .create());
        dismissibleFragment.setPositiveButton(activity.getString(android.R.string.ok), v -> {
            String inputText = "";
            if (popupView.getEditText() != null) inputText = popupView.getEditText().getText().toString();
            createSubject(dismissibleFragment, popupView, activity, inputText);
        });
        dismissibleFragment.setNegativeButton(activity.getString(android.R.string.cancel), v ->
                dismissibleFragment.dismiss());
        dismissibleFragment.show(activity.getSupportFragmentManager(), "GeneralFunctions.1");
    }

    /** Creates the subject if the subject title is not taken. **/
    private static void createSubject(DismissibleDialogFragment dismissibleFragment, TextInputLayout popupView,
                                      MainActivity activity, @NonNull String inputText) {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(activity);
        // Preliminary checks if subject name is taken or is empty
        if (inputText.replaceAll("\\s+", "").length() == 0) {
            popupView.setErrorEnabled(true);
            popupView.setError(activity.getString(R.string.n_error_subject_empty));
            database.close();
        } else if (database.SubjectDao().searchByTitle(inputText) != null) {
            popupView.setErrorEnabled(true);
            popupView.setError(activity.getString(R.string.error_subject_exists));
            database.close();
        } else {
            // Create subject
            int subjectId = DatabaseFunctions.generateValidId(database, DatabaseFunctions.SUBJ_ID_TYPE.SUBJECT);
            database.SubjectDao().insert(new NotesSubject(subjectId, inputText, NotesSubject.SORT_ALPHABETICAL_ASC));
            database.close();
            activity.safeOnBackPressed();
            activity.displayFragment(NotesSubjectFragment.newInstance(subjectId));
            dismissibleFragment.dismiss();
            NavViewFunctions.updateNavView(activity);
        }
    }

    /** Checks the given ids in a project for any errors and returns the correct values if it exists.
     * If the project has errors, {true, null, null} will be returned.
     * If the project does not have errors, {false, member, role} will be returned.
      */
    public static Object[] checkIdValidity(Activity activity, ProjectDatabase projectDatabase,
                                                ProjectData project, String id2, boolean isMember) {
        // Set up values
        MemberData member = null;
        RoleData role = null;
        Object[] responseObject = {true, null, null};

        // Set title
        if (project == null) {
            // Project is somehow missing
            Toast.makeText(activity, R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
            return responseObject;
        } else {
            activity.setTitle(project.projectTitle);
            if (isMember) {
                member = projectDatabase.MemberDao().searchByID(id2);
                if (member == null) {
                    // Member is somehow missing
                    Toast.makeText(activity, R.string.p_error_member_not_found, Toast.LENGTH_SHORT).show();
                    return responseObject;
                } else if (project.rolesEnabled) {
                    // Get the associated role if needed
                    role = projectDatabase.RoleDao().searchByID(member.role);
                    if (role == null) {
                        // Role is somehow missing
                        Toast.makeText(activity, R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                        return responseObject;
                    }
                }
            } else {
                // We can safely assume that members are disabled
                // Get the associated role if needed
                role = projectDatabase.RoleDao().searchByID(id2);
                if (role == null) {
                    // Role is somehow missing
                    Toast.makeText(activity, R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                    return responseObject;
                }
            }
        }
        responseObject[0] = false;
        responseObject[1] = member;
        responseObject[2] = role;
        return responseObject;
    }

    /** Inserts a HTML text into a TextView. **/
    public static void setHtml(TextView view, String htmlText) {
        Spanned output;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            output = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            output = Html.fromHtml(htmlText);
        }
        view.setText(output);
        view.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
