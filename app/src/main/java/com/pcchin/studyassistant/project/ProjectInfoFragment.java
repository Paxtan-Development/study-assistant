/*
 * Copyright 2019 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.project;


import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.room.Room;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.MemberData;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.database.project.data.RoleData;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.display.AutoDismissDialog;
import com.pcchin.studyassistant.display.ExtendedFragment;
import com.pcchin.studyassistant.notes.NotesSubjectFragment;
import com.pcchin.studyassistant.project.member.ProjectMemberFragment;

import java.io.File;
import java.util.Date;

public class ProjectInfoFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_ID = "projectID";
    private static final String ARG_ID2 = "ID2";
    private static final String ARG_IS_MEMBER = "isMember";
    private static final String ARG_UPDATE_NAV_VIEW = "updateNavView";
    private ProjectDatabase projectDatabase;
    private ProjectData project;

    // Mutually exclusive unless the project has both of those enabled
    private MemberData member;
    private RoleData role;

    /** Default constructor. **/
    public ProjectInfoFragment() {
    }

    /** Used in all instances when creating new project.
     * @param ID2 can be either the role ID or member ID depending on the project.
     * @param isMember determines whether ID2 is a member ID or a role ID. If ID2 is none.
     * @param updateNavView determines whether the navigation view will be updated. **/
    public static ProjectInfoFragment newInstance(String projectID, String ID2, boolean isMember,
                                                  boolean updateNavView) {
        ProjectInfoFragment fragment = new ProjectInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ID, projectID);
        args.putString(ARG_ID2, ID2);
        args.putBoolean(ARG_IS_MEMBER, isMember);
        args.putBoolean(ARG_UPDATE_NAV_VIEW, updateNavView);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment and the project info. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null && getArguments() != null) {
            String projectID = getArguments().getString(ARG_ID), id2 = getArguments().getString(ARG_ID2);
            boolean isMember = getArguments().getBoolean(ARG_IS_MEMBER),
                    updateNavView = getArguments().getBoolean(ARG_UPDATE_NAV_VIEW);
            boolean hasError = false;

            projectDatabase = Room.databaseBuilder(getActivity(), ProjectDatabase.class,
                    MainActivity.DATABASE_PROJECT)
                    .fallbackToDestructiveMigrationFrom(1, 2, 3, 4)
                    .allowMainThreadQueries().build();
            project = projectDatabase.ProjectDao().searchByID(projectID);
            // Set title
            if (project == null) {
                // Project is somehow missing
                Toast.makeText(getActivity(), R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
                hasError = true;
            } else {
                getActivity().setTitle(project.projectTitle);
                if (isMember) {
                    member = projectDatabase.MemberDao().searchByID(id2);
                    if (member == null) {
                        // Member is somehow missing
                        Toast.makeText(getActivity(), R.string.p_error_member_not_found, Toast.LENGTH_SHORT).show();
                        hasError = true;
                    } else if (project.rolesEnabled) {
                        // Get the associated role if needed
                        role = projectDatabase.RoleDao().searchByID(member.role);
                        if (role == null) {
                            // Role is somehow missing
                            Toast.makeText(getActivity(), R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                            hasError = true;
                        }
                    }
                } else {
                    // We can safely assume that members are disabled
                    // Get the associated role if needed
                    role = projectDatabase.RoleDao().searchByID(id2);
                    if (role == null) {
                        // Role is somehow missing
                        Toast.makeText(getActivity(), R.string.p_error_role_not_found, Toast.LENGTH_SHORT).show();
                        hasError = true;
                    }
                }
            }

            if (hasError) {
                // Return to ProjectSelectFragment (Same as onBackPressed) if any error is found
                onBackPressed();
            } else if (updateNavView) {
                // Set up navigation menu for members and roles respectively only if requested
                if (isMember) {
                    GeneralFunctions.updateBottomNavView((MainActivity) getActivity(),
                            R.menu.menu_p_bottom, project, member, null);
                } else {
                    GeneralFunctions.updateBottomNavView((MainActivity) getActivity(),
                            R.menu.menu_p_bottom, project, null, role);
                }
            }
        } else {
            // getActivity() is somehow null, returns to previous fragment.
            onBackPressed();
        }
        setHasOptionsMenu(true);
    }

    /** Sets up the layout for the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View returnView;
        if (project.displayedInfo == ProjectData.DISPLAYED_NONE) {
            returnView = inflater.inflate(R.layout.fragment_project_info_notable, container, false);
        } else {
            returnView = inflater.inflate(R.layout.fragment_project_info, container, false);
            // TODO: set up table
        }

        if (getContext() != null) {
            // Set up layout
            ((TextView) returnView.findViewById(R.id.p2_title)).setText(project.projectTitle);
            if (project.description.length() == 0) {
                returnView.findViewById(R.id.p2_desc).setVisibility(View.GONE);
            } else {
                ((TextView) returnView.findViewById(R.id.p2_desc)).setText(project.description);
            }

            // Set icon
            if (project.hasIcon) {
                ((ImageView) returnView.findViewById(R.id.p2_icon)).setImageURI(
                        Uri.fromFile(new File(getContext().getFilesDir()
                                + "/icons/project/" + project.projectID + ".jpg")));
            } else {
                // Center the title if there is no icon
                returnView.findViewById(R.id.p2_icon).setVisibility(View.GONE);
                ((TextView) returnView.findViewById(R.id.p2_title)).setGravity(Gravity.CENTER_HORIZONTAL);
            }

            // Set status
            TextView statusView = returnView.findViewById(R.id.p2_status);
            Date currentDate = new Date();
            if (project.projectOngoing) {
                if (project.expectedStartDate != null && project.expectedStartDate.after(currentDate)
                        && (project.actualStartDate == null || project.actualStartDate.after(currentDate))) {
                    // Project not started if expected start date is in the future
                    // and actual start date is not set or is in the future
                    statusView.setText(R.string.p_status_future);
                } else if (project.expectedEndDate != null && currentDate.after(project.expectedEndDate)
                        && (project.actualEndDate == null || project.actualEndDate.after(currentDate))) {
                    // Project is considered delayed if current date is past the expected end date
                    // and actual end date is not set or in the future
                    statusView.setText(R.string.p_status_delayed);
                } else if (project.actualEndDate != null && currentDate.after(project.actualEndDate)) {
                    // Project is considered completed if actual end date is past the current date
                    statusView.setText(R.string.p_status_completed);
                } else {
                    statusView.setText(R.string.p_status_ongoing);
                }
            } else {
                statusView.setText(R.string.p_status_completed);
            }

            // Set dates
            if (project.expectedStartDate != null) {
                ((TextView) returnView.findViewById(R.id.p2_expected_start)).setText(
                        String.format("Expected Start Date: %s",
                                ConverterFunctions.standardDateFormat.format(project.expectedStartDate)));
            }
            if (project.expectedEndDate != null) {
                ((TextView) returnView.findViewById(R.id.p2_expected_end)).setText(
                        String.format("Expected End Date: %s",
                                ConverterFunctions.standardDateFormat.format(project.expectedEndDate)));
            }
            if (project.actualStartDate != null) {
                ((TextView) returnView.findViewById(R.id.p2_actual_start)).setText(
                        String.format("Actual Start Date: %s",
                                ConverterFunctions.standardDateFormat.format(project.actualStartDate)));
            }
            if (project.actualEndDate != null) {
                ((TextView) returnView.findViewById(R.id.p2_actual_start)).setText(
                        String.format("Actual End Date: %s",
                                ConverterFunctions.standardDateFormat.format(project.actualEndDate)));
            }
        }
        return returnView;
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_p2, menu);
        // Disable user if project does not have user
        if (!project.membersEnabled) {
            menu.findItem(R.id.p2_menu_user).setVisible(false);
        }
        // Disable associated subject if the project does not have one
        if (project.associatedSubject == null) {
            menu.findItem(R.id.p2_menu_notes).setVisible(false);
        }

        // Update menu items according to user privileges
        boolean modifyInfo = true, viewMedia = true;
        if (project.rolesEnabled && role != null) {
            modifyInfo = role.canModifyInfo;
            viewMedia = role.canViewMedia;
        } else if (member != null) {
            // Disable settings and export for user without permissions
            RoleData userRole = projectDatabase.RoleDao().searchByID(member.role);
            if (userRole != null) {
                modifyInfo = userRole.canModifyInfo;
                viewMedia = userRole.canViewMedia;
            }
        }
        menu.findItem(R.id.p2_menu_settings).setVisible(modifyInfo);
        menu.findItem(R.id.p2_menu_export).setVisible(modifyInfo);
        menu.findItem(R.id.p2_menu_media).setVisible(viewMedia);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Goes to
     * @see com.pcchin.studyassistant.project.member.ProjectMemberFragment for the member.**/
    public void onUserPressed() {
        if (getActivity() != null && member != null) {
            projectDatabase.close();
            ((MainActivity) getActivity()).displayFragment(ProjectMemberFragment
                    .newInstance(project.projectID, member.memberID, member.memberID, false));
        }
    }

    /** Goes to
     * @see com.pcchin.studyassistant.notes.NotesSubjectFragment for the related subject.
     * If the subject is not found, an alert will display asking if the user would
     * like to delete the related subject from the project. **/
    public void onNotesPressed() {
        if (project.associatedSubject != null && getActivity() != null && getFragmentManager() != null) {
            // Opens subject database
            SubjectDatabase subjDatabase = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                    MainActivity.DATABASE_NOTES)
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
            NotesSubject targetSubject = subjDatabase.SubjectDao().search(project.associatedSubject);
            if (targetSubject == null) {
                // Ask the user whether to remove the associated subject
                AutoDismissDialog subjDialog = new AutoDismissDialog(getString(R.string.p2_subject_missing),
                        getString(R.string.p2_subject_missing_desc),
                        new String[]{getString(android.R.string.yes),
                        getString(android.R.string.no), ""},
                        new DialogInterface.OnClickListener[]{(dialogInterface, i) -> {
                            project.associatedSubject = null;
                            projectDatabase.ProjectDao().update(project);
                            getActivity().invalidateOptionsMenu();
                        }, null, null});
                subjDialog.setDismissListener(dialogInterface -> subjDatabase.close());
                subjDialog.show(getFragmentManager(), "ProjectInfoFragment.1");
            } else {
                subjDatabase.close();
                projectDatabase.close();
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                            .newInstance(project.associatedSubject));
                }
            }
        }
    }

    /** Goes to
     * @see ProjectSettingsFragment for the project. **/
    public void onSettingsPressed() {
        if (getActivity() != null) {
            projectDatabase.close();
            if (member != null) {
                ((MainActivity) getActivity()).displayFragment(ProjectSettingsFragment
                        .newInstance(project.projectID, member.memberID, true));
            } else {
                ((MainActivity) getActivity()).displayFragment(ProjectSettingsFragment
                        .newInstance(project.projectID, role.roleID, false));
            }
        }
    }

    /** Access the media for the note at
     * @see ProjectMediaFragment . **/
    public void onMediaPressed() {
        if (getActivity() != null) {
            projectDatabase.close();
            if (member != null) {
                ((MainActivity) getActivity()).displayFragment(ProjectMediaFragment
                        .newInstance(project.projectID, member.memberID, true));
            } else {
                ((MainActivity) getActivity()).displayFragment(ProjectMediaFragment
                        .newInstance(project.projectID, role.roleID, false));
            }
        }
    }

    /** Exports the subject to either a .project file or a ZIP file. **/
    public void onExportPressed() {
        // TODO: Export project
    }

    /** Returns to
     * @see ProjectSelectFragment **/
    @Override
    public boolean onBackPressed() {
        projectDatabase.close();
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            return true;
        }
        return false;
    }
}
