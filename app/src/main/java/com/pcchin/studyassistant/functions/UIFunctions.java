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
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectStatic;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.fragment.notes.notessubject.NotesSubjectFragment;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.create.ProjectCreateFragment;
import com.pcchin.studyassistant.fragment.project.member.ProjectMemberListFragment;
import com.pcchin.studyassistant.fragment.project.role.ProjectRoleFragment;
import com.pcchin.studyassistant.fragment.project.status.ProjectStatusFragment;
import com.pcchin.studyassistant.fragment.project.task.ProjectTaskFragment;
import com.pcchin.studyassistant.fragment.project.verify.ProjectLoginFragment;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

/** Functions used for UI elements in the app. **/
public final class UIFunctions {
    // TODO: Refactor class
    /** Constructor made private to simulate static class. **/
    private UIFunctions() {}

    /** Shows the dialog to add a new subject to the notes list **/
    public static void showNewSubject(@NonNull final MainActivity activity,
                               final SubjectDatabase database) {
        @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout) activity
                .getLayoutInflater().inflate(R.layout.popup_edittext, null);
        // End icon has been set by default in XML file
        // OnClickListeners implemented separately to prevent dialog from being dismissed after button click
        DialogInterface.OnShowListener subjectListener = dialog -> {
            popupView.setHint(activity.getString(R.string.n1_subject_title));
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String inputText = "";
                        if (popupView.getEditText() != null) {
                            inputText = popupView.getEditText().getText().toString();
                        }

                        // Preliminary checks if subject name is taken or is empty
                        if (inputText.replaceAll("\\s+", "").length() == 0) {
                            popupView.setErrorEnabled(true);
                            popupView.setError(activity.getString(R.string.n_error_subject_empty));
                        } else if (database.SubjectDao().search(inputText) != null) {
                            popupView.setErrorEnabled(true);
                            popupView.setError(activity.getString(R.string.error_subject_exists));
                        } else {
                            // Create subject
                            database.SubjectDao().insert(
                                    new NotesSubject(inputText,
                                                    new ArrayList<>(),
                                                    NotesSubject.SORT_ALPHABETICAL_ASC));
                            database.close();
                            activity.safeOnBackPressed();
                            activity.displayFragment(
                                    NotesSubjectFragment.newInstance(inputText));
                            dialog.dismiss();
                            UIFunctions.updateNavView(activity);
                        }
                    });
            ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> dialog.dismiss());
        };
        new AutoDismissDialog(activity.getString(R.string.n1_new_subject), popupView,
                new String[]{activity.getString(android.R.string.ok),
                        activity.getString(android.R.string.cancel), ""}, subjectListener)
                        .show(activity.getSupportFragmentManager(), "GeneralFunctions.1");
    }

    /** Updates the NavigationView in
     * @see MainActivity **/
    public static void updateNavView(@NonNull final MainActivity activity) {
        NavigationView navView = activity.findViewById(R.id.nav_view);
        // Nuke menu
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.menu_main_drawer);
        Menu currentMenu = navView.getMenu();

        // Add subjects
        final SubjectDatabase subjectDatabase = GeneralFunctions.getSubjectDatabase(activity);
        SubMenu subjMenu = currentMenu.addSubMenu(R.string.notes);
        List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        for (final NotesSubject subject: subjectList) {
            MenuItem subjItem = subjMenu.add(subject.title);
            // This is to prevent menu items from disappearing
            subjItem.setOnMenuItemClickListener(item -> {
                // Opens subject when clicked
                activity.closeDrawer();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(subject.title));
                return true;
            });
        }

        // Add New Subject button
        MenuItem newSubj = subjMenu.add(R.string.m3_new_subject);
        newSubj.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            showNewSubject(activity, subjectDatabase);
            return true;
        });

        // Add Import Subject button
        MenuItem subjImport = subjMenu.add(R.string.m3_data_import);
        subjImport.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            new Handler().post(() -> ImportSubjectStatic.displayImportDialog(activity));
            return true;
        });

        // Add projects
        SubMenu projMenu = currentMenu.addSubMenu(R.string.projects);
        ProjectDatabase projectDatabase = GeneralFunctions.getProjectDatabase(activity);
        List<ProjectData> projectList = projectDatabase.ProjectDao().getAllProjects();
        for (ProjectData project : projectList) {
            MenuItem projItem = projMenu.add(project.projectTitle);
            projItem.setOnMenuItemClickListener(menuItem -> {
                activity.closeDrawer();
                activity.safeOnBackPressed();
                activity.displayFragment(ProjectLoginFragment.newInstance(project.projectID));
                return false;
            });
        }

        // Add New Project Button
        MenuItem newProj = projMenu.add(R.string.m3_new_project);
        newProj.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            activity.displayFragment(new ProjectCreateFragment());
            return true;
        });

        // Add Import Project button
        MenuItem projImport = projMenu.add(R.string.m3_data_import);
        projImport.setOnMenuItemClickListener(item -> {
            // TODO: Import projects
            activity.closeDrawer();
            return true;
        });

        // Add subMenu for other buttons
        SubMenu otherMenu = currentMenu.addSubMenu(R.string.others);

        // Add about button
        MenuItem aboutItem = otherMenu.add(R.string.m_about);
        aboutItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            activity.displayFragment(new AboutFragment());
            return true;
        });

        // Add exit button
        MenuItem exitItem = otherMenu.add(R.string.exit);
        exitItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            GeneralFunctions.exitApp(activity);
            return true;
        });
    }

    /** Updates the bottom NavigationView to either the project's menu or the media selection's menu. **/
    public static void updateBottomNavView(MainActivity activity, int res, ProjectData project,
                                           MemberData member, RoleData role) {
        // Only member or role are allowed to be null, not both at once
        if (member == null && role == null) {
            throw new IllegalArgumentException("MemberData member and RoleData role cannot be " +
                    "null in the same arguments");
        }

        // No else statement needed as error is thrown in if statement
        // Reset menu
        activity.bottomNavView.getMenu().clear();
        activity.bottomNavView.inflateMenu(res);
        Menu navViewMenu = activity.bottomNavView.getMenu();

        // Set up database
        ProjectDatabase database = GeneralFunctions.getProjectDatabase(activity);

        // Bottom nav view
        if (res == R.menu.menu_p_bottom) {
            // Home button
            navViewMenu.findItem(R.id.p_bottom_home).setOnMenuItemClickListener(item -> {
                if (!(activity.currentFragment instanceof ProjectInfoFragment)) {
                    if (member == null) {
                        // Default to role
                        activity.displayFragment(ProjectInfoFragment
                                .newInstance(project.projectID, role.roleID, false, false));
                    } else {
                        activity.displayFragment(ProjectInfoFragment
                                .newInstance(project.projectID, member.memberID, true, false));
                    }
                }
                return true;
            });

            // Members button
            if (project.membersEnabled && member != null && database.RoleDao().searchByID(member.role) != null
                    && (database.RoleDao().searchByID(member.role).canViewOtherUser ||
                    database.RoleDao().searchByID(member.role).canModifyOtherUser)) {
                navViewMenu.findItem(R.id.p_bottom_members).setOnMenuItemClickListener(item -> {
                    if (!(activity.currentFragment instanceof ProjectMemberListFragment)) {
                        activity.displayFragment(ProjectMemberListFragment
                                .newInstance(project.projectID, member.memberID, false));
                    }
                    return true;
                });
            } else {
                navViewMenu.findItem(R.id.p_bottom_members).setVisible(false);
            }

            // Tasks button
            if (project.taskEnabled && (
                    // Member has privileges
                    (member != null && database.RoleDao().searchByID(member.role) != null
                            && (database.RoleDao().searchByID(member.role).canViewOtherTask ||
                            database.RoleDao().searchByID(member.role).canModifyOtherTask ||
                            database.RoleDao().searchByID(member.role).canViewTask ||
                            database.RoleDao().searchByID(member.role).canModifyOwnTask)) ||

                            // Role has privileges
                            (role != null && (role.canViewOtherTask || role.canModifyOtherTask
                                    || role.canViewTask || role.canModifyOwnTask)))) {
                navViewMenu.findItem(R.id.p_bottom_task).setOnMenuItemClickListener(item -> {
                    if (!(activity.currentFragment instanceof ProjectTaskFragment)) {
                        if (member != null) {
                            activity.displayFragment(ProjectTaskFragment
                                    .newInstance(project.projectID, member.memberID, true, false));
                        } else {
                            activity.displayFragment(ProjectTaskFragment
                                    .newInstance(project.projectID, role.roleID, false, false));
                        }
                    }
                    return true;
                });
            } else {
                navViewMenu.findItem(R.id.p_bottom_task).setVisible(false);
            }

            // Roles button
            if (project.rolesEnabled && (
                    // Member has privileges
                    (member != null && database.RoleDao().searchByID(member.role) != null
                            && (database.RoleDao().searchByID(member.role).canViewRole ||
                            database.RoleDao().searchByID(member.role).canModifyRole)) ||

                            // Role has privileges
                            (role != null && (role.canViewRole || role.canModifyRole)))) {
                navViewMenu.findItem(R.id.p_bottom_role).setOnMenuItemClickListener(item -> {
                    if (!(activity.currentFragment instanceof ProjectRoleFragment)) {
                        if (member != null) {
                            activity.displayFragment(ProjectRoleFragment
                                    .newInstance(project.projectID, member.memberID, true, false));
                        } else {
                            activity.displayFragment(ProjectRoleFragment
                                    .newInstance(project.projectID, role.roleID, false, false));
                        }
                    }
                    return true;
                });
            } else {
                navViewMenu.findItem(R.id.p_bottom_role).setVisible(false);
            }

            // Status button
            if (project.statusEnabled && (
                    // Member has privileges
                    (member != null && database.RoleDao().searchByID(member.role) != null
                            && (database.RoleDao().searchByID(member.role).canModifyOtherStatus ||
                            database.RoleDao().searchByID(member.role).canPostStatus ||
                            database.RoleDao().searchByID(member.role).canViewStatus)) ||

                            // Role has privileges
                            (role != null && (role.canModifyOtherStatus || role.canPostStatus
                                    || role.canViewStatus)))) {
                navViewMenu.findItem(R.id.p_bottom_status).setOnMenuItemClickListener(item -> {
                    if (!(activity.currentFragment instanceof ProjectStatusFragment)) {
                        if (member != null) {
                            activity.displayFragment(ProjectStatusFragment
                                    .newInstance(project.projectID, member.memberID, true, false));
                        } else {
                            activity.displayFragment(ProjectStatusFragment
                                    .newInstance(project.projectID, role.roleID, false, false));
                        }
                    }
                    return true;
                });
            } else {
                navViewMenu.findItem(R.id.p_bottom_status).setVisible(false);
            }
        } else {
            // TODO: File Manager menu
        }

        database.close();
        activity.bottomNavView.setVisibility(View.VISIBLE);
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
