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

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;

import com.google.android.material.navigation.NavigationView;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectStatic;
import com.pcchin.studyassistant.fragment.about.AboutFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.fragment.project.create.ProjectCreateFragment;
import com.pcchin.studyassistant.fragment.project.verify.ProjectLoginFragment;
import com.pcchin.studyassistant.activity.MainActivity;

import java.util.List;

/** Functions used for updating the navigation view within the app. **/
public final class NavViewFunctions {
    private NavViewFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Updates the NavigationView in
     * @see MainActivity **/
    public static void updateNavView(@NonNull final MainActivity activity) {
        NavigationView navView = activity.findViewById(R.id.nav_view);
        // Nuke menu
        navView.getMenu().clear();
        navView.inflateMenu(R.menu.menu_main_drawer);
        // Populate menu
        Menu currentMenu = navView.getMenu();
        addSubjectMenu(currentMenu, activity);
        addProjectMenu(currentMenu, activity);
        addOtherMenu(currentMenu, activity);
    }

    /** Adds the subject menu to the nav view. **/
    private static void addSubjectMenu(@NonNull Menu currentMenu, MainActivity activity) {
        // Subject menu
        final SubjectDatabase subjectDatabase = DatabaseFunctions.getSubjectDatabase(activity);
        SubMenu subjMenu = currentMenu.addSubMenu(R.string.notes);
        addSubjects(subjMenu, activity, subjectDatabase);

        // Add New Subject button
        MenuItem newSubj = subjMenu.add(R.string.m3_new_subject);
        newSubj.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            UIFunctions.showNewSubject(activity, subjectDatabase);
            return true;
        });

        // Add Import Subject button
        MenuItem subjImport = subjMenu.add(R.string.m3_data_import);
        subjImport.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            new Handler().post(() -> ImportSubjectStatic.displayImportDialog(activity));
            return true;
        });
    }

    /** Adds all the available subjects to the submenu. **/
    private static void addSubjects(SubMenu subjMenu, MainActivity activity,
                                    @NonNull SubjectDatabase subjectDatabase) {
        List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        for (final NotesSubject subject: subjectList) {
            MenuItem subjItem = subjMenu.add(subject.title);
            // This is to prevent menu items from disappearing
            subjItem.setOnMenuItemClickListener(item -> {
                // Opens subject when clicked
                activity.closeDrawer();
                activity.safeOnBackPressed();
                activity.displayFragment(NotesSubjectFragment.newInstance(subject.subjectId));
                return true;
            });
        }
    }

    /** Adds the project menu to the nav view. **/
    private static void addProjectMenu(@NonNull Menu currentMenu, MainActivity activity) {
        // Add projects
        SubMenu projMenu = currentMenu.addSubMenu(R.string.projects);
        ProjectDatabase projectDatabase = DatabaseFunctions.getProjectDatabase(activity);
        addProjects(projMenu, activity, projectDatabase);

        // Add New Project Button
        MenuItem newProj = projMenu.add(R.string.m3_new_project);
        newProj.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            activity.displayFragment(new ProjectCreateFragment());
            return true;
        });

        addImportProject(projMenu, activity);
    }

    /** Adds all projects to the submenu. **/
    private static void addProjects(SubMenu projMenu, MainActivity activity,
                                    @NonNull ProjectDatabase projectDatabase) {
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
    }

    /** Adds the import project button to the submenu. **/
    private static void addImportProject(@NonNull SubMenu projMenu, MainActivity activity) {
        MenuItem projImport = projMenu.add(R.string.m3_data_import);
        projImport.setOnMenuItemClickListener(item -> {
            // TODO: Import projects
            activity.closeDrawer();
            return true;
        });
    }

    /** Adds the other menu to the nav view. **/
    private static void addOtherMenu(@NonNull Menu currentMenu, MainActivity activity) {
        SubMenu otherMenu = currentMenu.addSubMenu(R.string.others);

        // About button
        MenuItem aboutItem = otherMenu.add(R.string.m_about);
        aboutItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            activity.displayFragment(new AboutFragment());
            return true;
        });

        // Exit button
        MenuItem exitItem = otherMenu.add(R.string.exit);
        exitItem.setOnMenuItemClickListener(item -> {
            activity.closeDrawer();
            activity.safeOnBackPressed();
            GeneralFunctions.exitApp(activity);
            return true;
        });
    }
}
