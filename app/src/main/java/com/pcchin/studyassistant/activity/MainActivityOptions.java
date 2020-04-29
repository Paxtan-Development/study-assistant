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

package com.pcchin.studyassistant.activity;

import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragment;
import com.pcchin.studyassistant.fragment.notes.NotesSelectFragment;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragmentClick;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragmentClick1;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragmentClick2;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragment;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragmentClick1;
import com.pcchin.studyassistant.fragment.notes.view.NotesViewFragmentClick2;
import com.pcchin.studyassistant.fragment.project.ProjectInfoFragment;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;

/** A class created specifically for processing the options in the options menu of
 * @see MainActivity **/
final class MainActivityOptions {
    private MainActivityOptions() {
        throw new IllegalStateException("Utility class");
    }

    /** Process the options in the options menu of MainActivity. **/
    static void processOption(int itemId, Fragment currentFragment) {
        switch(itemId) {
            // When NotesSelectFragment or ProjectSelectFragment is activated
            case R.id.menu_new_subj:
                if (currentFragment instanceof NotesSelectFragment) {
                    ((NotesSelectFragment) currentFragment).onNewSubjectPressed();
                } else {
                    ((ProjectSelectFragment) currentFragment).onNewProjectPressed();
                }
                break;
            case R.id.menu_import:
                if (currentFragment instanceof  NotesSelectFragment) {
                    ((NotesSelectFragment) currentFragment).onImportPressed();
                } else {
                    ((ProjectSelectFragment) currentFragment).onImportPressed();
                }
                break;

            // When NotesSubjectFragment is activated
            case R.id.n2_new_note:
                new NotesSubjectFragmentClick1((NotesSubjectFragment) currentFragment).onNewNotePressed();
                break;
            case R.id.n2_sort:
                new NotesSubjectFragmentClick1((NotesSubjectFragment) currentFragment).onSortPressed();
                break;
            case R.id.n2_rename:
                new NotesSubjectFragmentClick2((NotesSubjectFragment) currentFragment).onRenamePressed();
                break;
            case R.id.n2_export:
                new NotesSubjectFragmentClick2((NotesSubjectFragment) currentFragment).onExportPressed();
                break;
            case R.id.n2_del:
                new NotesSubjectFragmentClick2((NotesSubjectFragment) currentFragment).onDeletePressed();
                break;

            // When NotesViewFragment is activated
            case R.id.n3_edit:
                new NotesViewFragmentClick1((NotesViewFragment) currentFragment).onEditPressed();
                break;
            case R.id.n3_export:
                new NotesViewFragmentClick1((NotesViewFragment) currentFragment).onExportPressed();
                break;
            case R.id.n3_lock:
                new NotesViewFragmentClick1((NotesViewFragment) currentFragment).onLockPressed();
                break;
            case R.id.n3_unlock:
                new NotesViewFragmentClick1((NotesViewFragment) currentFragment).onUnlockPressed();
                break;
            case R.id.n3_notif:
                new NotesViewFragmentClick2((NotesViewFragment) currentFragment).onAlertPressed();
                break;
            case R.id.n3_cancel_notif:
                new NotesViewFragmentClick2((NotesViewFragment) currentFragment).onCancelAlertPressed();
                break;
            case R.id.n3_del:
                new NotesViewFragmentClick2((NotesViewFragment) currentFragment).onDeletePressed();
                break;

            // When NotesEditFragment is selected
            case R.id.n4_subj:
                new NotesEditFragmentClick((NotesEditFragment) currentFragment).onSubjPressed();
                break;
            case R.id.n4_save:
                new NotesEditFragmentClick((NotesEditFragment) currentFragment).onSavePressed();
                break;
            case R.id.n4_cancel:
                new NotesEditFragmentClick((NotesEditFragment) currentFragment).onCancelPressed();
                break;

            // When ProjectInfoFragment is selected
            case R.id.p2_menu_user:
                ((ProjectInfoFragment) currentFragment).onUserPressed();
                break;
            case R.id.p2_menu_notes:
                ((ProjectInfoFragment) currentFragment).onNotesPressed();
                break;
            case R.id.p2_menu_settings:
                ((ProjectInfoFragment) currentFragment).onSettingsPressed();
                break;
            case R.id.p2_menu_media:
                ((ProjectInfoFragment) currentFragment).onMediaPressed();
                break;
            case R.id.p2_menu_export:
                ((ProjectInfoFragment) currentFragment).onExportPressed();
                break;
        }
    }
}
