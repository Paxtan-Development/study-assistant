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

package com.pcchin.studyassistant.fragment.notes.edit;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Functions used when the fragment is clicked. **/
public final class NotesEditFragmentClick {
    private final NotesEditFragment fragment;
    private final View fragmentView;
    private final MainActivity activity;
    
    /** Constructor used as fragment needs to be passed on. **/
    public NotesEditFragmentClick(@NonNull NotesEditFragment fragment) {
        this.fragment = fragment;
        this.fragmentView = fragment.getView();
        this.activity = (MainActivity) fragment.requireActivity();
    }

    /** Changes the fragment.subject that the note will be saved to. **/
    public void onSubjPressed() {
        final Spinner subjListSpinner = new Spinner(activity);
        // Add the current title
        List<String> subjTitleList = new ArrayList<>();
        int currentSubject;
        if (fragment.subjModified) {
            currentSubject = fragment.targetSubjectId;
        } else {
            currentSubject = fragment.subjectId;
        }
        subjTitleList.add(fragment.database.SubjectDao().searchById(currentSubject).title);
        // Add the remaining titles (Order different thus int[] is needed
        List<NotesSubject> allSubjList = fragment.database.SubjectDao().getAll();
        int[] displayListArray = new int[allSubjList.size()];
        int displayListCount = 0;
        displayListArray[0] = currentSubject;
        for (NotesSubject subject : allSubjList) {
            if (!(subject.subjectId == currentSubject)) {
                subjTitleList.add(subject.title);
                displayListCount++;
                displayListArray[displayListCount] = subject.subjectId;
            }
        }
        showSubjDialog(subjListSpinner, subjTitleList, displayListArray);
    }
    
    /** Show the dialog for changing the subject of the note. **/
    private void showSubjDialog(@NonNull Spinner subjListSpinner, List<String> subjTitleList,
                                int[] displayListArray) {
        // Set spinner adaptor
        ArrayAdapter<String> subjAdaptor = new ArrayAdapter<>
                (activity, android.R.layout.simple_spinner_item, subjTitleList);
        subjAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjListSpinner.setAdapter(subjAdaptor);

        new DefaultDialogFragment(new AlertDialog.Builder(activity)
                .setTitle(R.string.n_change_subj)
                .setView(subjListSpinner)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    fragment.subjModified = true;
                    fragment.targetSubjectId = displayListArray[subjListSpinner.getSelectedItemPosition()];
                    activity.setTitle(subjListSpinner.getSelectedItem().toString());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create())
                .show(fragment.getParentFragmentManager(), "NotesEditFragment.1");
    }

    /** Saves the note to the fragment.subject selected. **/
    public void onSavePressed() {
        // Check if title is empty
        if (fragmentView != null && ((EditText) fragmentView
                .findViewById(R.id.n4_title)).getText().toString()
                .replaceAll("\\s+", "").length() > 0) {
            createUpdatedNote();
        } else {
            Toast.makeText(activity, R.string.n2_error_note_title_empty, Toast.LENGTH_SHORT).show();
        }
    }

    /** Creates an updated note. **/
    private void createUpdatedNote() {
        fragment.currentNote.noteTitle = ((EditText) fragmentView.findViewById(R.id.n4_title)).getText().toString();
        fragment.currentNote.lastEdited = new Date();
        fragment.currentNote.noteContent = ((EditText) fragmentView.findViewById(R.id.n4_edit)).getText().toString();

        // Toast at start as different objects have different displayFragments
        Toast.makeText(activity, R.string.n4_note_saved, Toast.LENGTH_SHORT).show();
        if (fragment.subjModified) {
            fragment.currentNote.subjectId = fragment.targetSubjectId;
        }
        if (fragment.hasParent) {
            fragment.database.ContentDao().update(fragment.currentNote);
        } else {
            fragment.database.ContentDao().insert(fragment.currentNote);
        }
        fragment.database.close();
        activity.displayNotes(fragment.currentNote.subjectId);
        activity.pager.setPagerOrder(fragment.currentNote.noteId);
    }

    /** Cancel all the changes and return to
     * @see NotesSubjectFragment **/
    public void onCancelPressed() {
        // Go back to NotesViewFragment of fragment.subject
        if (fragment.hasParent) {
            activity.displayNotes(fragment.subjectId);
            activity.pager.setPagerOrder(fragment.currentNote.noteId);
        } else {
            activity.displayFragment(NotesSubjectFragment.newInstance(fragment.subjectId));
        }
    }
}
