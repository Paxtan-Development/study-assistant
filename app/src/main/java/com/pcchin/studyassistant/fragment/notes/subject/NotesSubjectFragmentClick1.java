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

package com.pcchin.studyassistant.fragment.notes.subject;

import android.annotation.SuppressLint;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.customdialog.DismissibleDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.fragment.notes.edit.NotesEditFragment;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.utils.notes.NotesSortAdaptor;

/** 1st class for the functions for the onClickListeners in the fragment. **/
public class NotesSubjectFragmentClick1 {
    private NotesSubjectFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    public NotesSubjectFragmentClick1(NotesSubjectFragment fragment) {
        this.fragment = fragment;
    }

    /** Creates a new note with a given title. **/
    public void onNewNotePressed() {
        @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout)
                fragment.getLayoutInflater().inflate(R.layout.popup_edittext, null);
        popupView.setHint(fragment.getString(R.string.title));
        // End icon has been set in XML file
        DismissibleDialogFragment dismissibleFragment = new DismissibleDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.n2_new_note)
                .setView(popupView)
                .create());
        dismissibleFragment.setPositiveButton(fragment.getString(android.R.string.ok),
                v -> createNewNote(dismissibleFragment, popupView));
        dismissibleFragment.setNegativeButton(fragment.getString(android.R.string.cancel),
                v -> dismissibleFragment.dismiss());
        dismissibleFragment.show(fragment.getParentFragmentManager(), "NotesSubjectFragment.1");
    }

    /** Creates a new note based on the given title. **/
    private void createNewNote(DismissibleDialogFragment dismissibleFragment, @NonNull TextInputLayout popupView) {
        String popupInputText = "";
        if (popupView.getEditText() != null) {
            popupInputText = popupView.getEditText().getText().toString();
        }

        // Check if input is blank
        if (popupInputText.replaceAll("\\s+", "")
                .length() == 0) {
            popupView.setErrorEnabled(true);
            popupView.setError(fragment.getString(R.string.n2_error_note_title_empty));
        } else {
            // Edit new note
            dismissibleFragment.dismiss();
            fragment.subjectDatabase.close();
            ((MainActivity) fragment.requireActivity()).displayFragment
                    (NotesEditFragment.newInstance(fragment.notesSubject, popupInputText));
        }
    }

    /** Change the method which the notes are sorted. **/
    public void onSortPressed() {
        @SuppressLint("InflateParams") final Spinner sortingSpinner = (Spinner) fragment.getLayoutInflater().inflate
                (R.layout.n2_sorting_spinner, null);

        // Get current order
        sortingSpinner.setAdapter(new NotesSortAdaptor(fragment.requireContext(),
                NotesSubjectFragment.sortingTitles, NotesSubjectFragment.sortingImgs));
        NotesSubject subject = fragment.subjectDatabase.SubjectDao().search(fragment.notesSubject);
        int currentOrder = subject.sortOrder;
        for (int i = 0; i < NotesSubjectFragment.sortingList.length; i++) {
            // Sort spinner to current order
            if (NotesSubjectFragment.sortingList[i] == currentOrder) {
                sortingSpinner.setSelection(i);
            }
        }
        displaySortDialog(sortingSpinner);
    }

    /** Display the dialog for selecting a sorting method. **/
    private void displaySortDialog(Spinner sortingSpinner) {
        new DefaultDialogFragment(new AlertDialog.Builder(fragment.requireContext())
                .setTitle(R.string.sorting_method)
                .setView(sortingSpinner)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    // Update value in notes
                    NotesSubject subject1 = fragment.subjectDatabase.SubjectDao().search(fragment.notesSubject);
                    subject1.sortOrder = NotesSubjectFragment.sortingList[sortingSpinner.getSelectedItemPosition()];
                    fragment.subjectDatabase.SubjectDao().update(subject1);
                    dialogInterface.dismiss();
                    fragment.sortNotes(subject1);
                    GeneralFunctions.reloadFragment(fragment);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create())
                .show(fragment.getParentFragmentManager(), "NotesSubjectFragment.2");
    }
}
