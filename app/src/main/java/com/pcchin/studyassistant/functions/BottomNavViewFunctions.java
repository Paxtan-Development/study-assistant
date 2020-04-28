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

import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.member.ProjectMemberListFragment;
import com.pcchin.studyassistant.fragment.project.role.ProjectRoleFragment;
import com.pcchin.studyassistant.fragment.project.status.ProjectStatusFragment;
import com.pcchin.studyassistant.fragment.project.task.ProjectTaskFragment;
import com.pcchin.studyassistant.activity.MainActivity;

/** Functions used for updating the bottom navigation view within the app. **/
public final class BottomNavViewFunctions {
    /** Updates the bottom NavigationView to either the project's menu or the media selection's menu. **/
    public static void updateBottomNavView(MainActivity activity, int res, ProjectData project,
                                           MemberData member, RoleData role) {
        // Only member or role are allowed to be null, not both at once
        if (member == null && role == null) throw new IllegalArgumentException("MemberData member " +
                "and RoleData role cannot be null in the same arguments");

        // No else statement needed as error is thrown in if statement
        activity.bottomNavView.getMenu().clear();
        activity.bottomNavView.inflateMenu(res);
        Menu navViewMenu = activity.bottomNavView.getMenu();
        ProjectDatabase database = GeneralFunctions.getProjectDatabase(activity);

        // Bottom nav view
        if (res == R.menu.menu_p_bottom) {
            initHomeButton(navViewMenu, activity, project, member, role);
            initMembersButton(navViewMenu, activity, database, project, member);
            initTasksButton(navViewMenu, activity, database, project, member, role);
            initRolesButton(navViewMenu, activity, database, project, member, role);
            initStatusButton(navViewMenu, activity, database, project, member, role);
        } else {
            // TODO: File Manager menu
        }

        database.close();
        activity.bottomNavView.setVisibility(View.VISIBLE);
    }

    /** Initializes the home button on the bottom nav view. **/
    private static void initHomeButton(@NonNull Menu navViewMenu, MainActivity activity,
                                       ProjectData project, MemberData member, RoleData role) {
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
    }

    /** Initializes the members button on the bottom nav view. **/
    private static void initMembersButton(Menu navViewMenu, MainActivity activity,
                                          ProjectDatabase database, @NonNull ProjectData project, MemberData member) {
        if (project.membersEnabled && member != null &&
                memberHasMemberPrivileges(database.RoleDao().searchByID(member.role))) {
            navViewMenu.findItem(R.id.p_bottom_members).setOnMenuItemClickListener(item -> {
                if (!(activity.currentFragment instanceof ProjectMemberListFragment))
                    activity.displayFragment(ProjectMemberListFragment
                            .newInstance(project.projectID, member.memberID, false));
                return true;
            });
        } else {
            navViewMenu.findItem(R.id.p_bottom_members).setVisible(false);
        }
    }

    /** Checks if the member has the required privileges to access the project members. **/
    private static boolean memberHasMemberPrivileges(RoleData role) {
        return role != null && (role.canViewOtherUser || role.canModifyOtherUser);
    }

    /** Initializes the tasks button on the bottom nav view. **/
    private static void initTasksButton(Menu navViewMenu, MainActivity activity, ProjectDatabase database,
                                        @NonNull ProjectData project, MemberData member, RoleData role) {
        if (project.taskEnabled && (memberHasTasksPrivileges(database, member) || roleHasTasksPrivileges(role))) {
            navViewMenu.findItem(R.id.p_bottom_task).setOnMenuItemClickListener(item -> {
                if (!(activity.currentFragment instanceof ProjectTaskFragment)) {
                    if (member != null) activity.displayFragment(ProjectTaskFragment
                                .newInstance(project.projectID, member.memberID, true, false));
                    else activity.displayFragment(ProjectTaskFragment
                                .newInstance(project.projectID, role.roleID, false, false));
                }
                return true;
            });
        } else {
            navViewMenu.findItem(R.id.p_bottom_task).setVisible(false);
        }
    }

    /** Check if the member has the required privileges to access the project tasks. **/
    private static boolean memberHasTasksPrivileges(ProjectDatabase database, MemberData member) {
        return member != null && roleHasTasksPrivileges(database.RoleDao().searchByID(member.role));
    }

    /** Check if the member has the required privileges to access the project tasks. **/
    private static boolean roleHasTasksPrivileges(RoleData role) {
        return role != null && (role.canViewOtherTask || role.canModifyOtherTask
                || role.canViewTask || role.canModifyOwnTask);
    }

    /** Initializes the roles button on the bottom nav view. **/
    private static void initRolesButton(Menu navViewMenu, MainActivity activity, ProjectDatabase database,
                                        @NonNull ProjectData project, MemberData member, RoleData role) {
        if (project.rolesEnabled && (memberHasRolesPrivileges(database, member)
                || roleHasRolesPrivileges(role))) {
            navViewMenu.findItem(R.id.p_bottom_role).setOnMenuItemClickListener(item -> {
                if (!(activity.currentFragment instanceof ProjectRoleFragment)) {
                    if (member != null) activity.displayFragment(ProjectRoleFragment
                                .newInstance(project.projectID, member.memberID, true, false));
                    else activity.displayFragment(ProjectRoleFragment
                                .newInstance(project.projectID, role.roleID, false, false));
                }
                return true;
            });
        } else {
            navViewMenu.findItem(R.id.p_bottom_role).setVisible(false);
        }
    }

    /** Check if the member has the required privileges to access the project roles. **/
    private static boolean memberHasRolesPrivileges(ProjectDatabase database, MemberData member) {
        return member != null && roleHasRolesPrivileges(database.RoleDao().searchByID(member.role));
    }

    /** Check if the role has the required privileges to access the project roles. **/
    private static boolean roleHasRolesPrivileges(RoleData role) {
        return role != null && (role.canViewRole || role.canModifyRole);
    }

    /** Initialize the status button on the bottom nav view. **/
    private static void initStatusButton(Menu navViewMenu, MainActivity activity,
                                         ProjectDatabase database, @NonNull ProjectData project,
                                         MemberData member, RoleData role) {
        if (project.statusEnabled && (memberHasStatusPrivileges(database, member)
                || roleHasStatusPrivileges(role))) {
            navViewMenu.findItem(R.id.p_bottom_status).setOnMenuItemClickListener(item -> {
                if (!(activity.currentFragment instanceof ProjectStatusFragment)) {
                    if (member != null) activity.displayFragment(ProjectStatusFragment
                                .newInstance(project.projectID, member.memberID, true, false));
                    else activity.displayFragment(ProjectStatusFragment
                                .newInstance(project.projectID, role.roleID, false, false));
                }
                return true;
            });
        } else {
            navViewMenu.findItem(R.id.p_bottom_status).setVisible(false);
        }
    }

    /** Check if the member mas the required privileges to access the project status. **/
    private static boolean memberHasStatusPrivileges(ProjectDatabase database, MemberData member) {
        return member != null && roleHasStatusPrivileges(database.RoleDao().searchByID(member.role));
    }

    /** Check if the role mas the required privileges to access the project status. **/
    private static boolean roleHasStatusPrivileges(RoleData role) {
        return role != null &&
                (role.canModifyOtherStatus || role.canPostStatus || role.canViewStatus);
    }
}
