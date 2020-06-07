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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.SubjectSelectFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.utils.misc.SortingComparators;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class NotesSubjectFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_SUBJECT = "noteSubject",
            ARG_PREV = "previousOrder";

    static final int[] sortingList = new int[]{NotesSubject.SORT_ALPHABETICAL_ASC,
        NotesSubject.SORT_ALPHABETICAL_DES, NotesSubject.SORT_DATE_ASC, NotesSubject.SORT_DATE_DES};
    static final int[] sortingTitles = new int[]{R.string.n2_sort_alpha_asc, R.string.n2_sort_alpha_des,
            R.string.n2_sort_date_asc, R.string.n2_sort_date_des};
    static final int[] sortingImgs = new int[]{R.drawable.ic_sort_atz, R.drawable.ic_sort_zta,
            R.drawable.ic_sort_num_asc, R.drawable.ic_sort_num_des};

    List<NotesContent> notesList;
    NotesSubject currentSubject;
    int previousNote;

    /** Default constructor. **/
    public NotesSubjectFragment() {
        // Default constructor.
    }

    /** Used in all except when returning from a NotesViewFragment.
     * @param subjectId is the subject that is displayed. **/
    @NonNull
    public static NotesSubjectFragment newInstance(int subjectId) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SUBJECT, subjectId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Used when returning from a NotesViewFragment.
     * @param subjectId is the ID of the subject that is displayed.
     * @param previousNote is the ID of the note that was shown. **/
    @NonNull
    public static NotesSubjectFragment newInstance(int subjectId, int previousNote) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SUBJECT, subjectId);
        args.putInt(ARG_PREV, previousNote);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Retrieves all of the notes of the subject from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(requireActivity());
        // Get basic info & set title
        if (getArguments() != null) {
            int subjectId = getArguments().getInt(ARG_SUBJECT);
            currentSubject = database.SubjectDao().searchById(subjectId);
            notesList = database.ContentDao().searchBySubject(subjectId);
            previousNote = getArguments().getInt(ARG_PREV);
            checkExpiredNote();
            sortNotes();
        }
        requireActivity().setTitle(currentSubject.title);
        database.close();
        setHasOptionsMenu(true);
    }

    /** Checks for any alerts in notes that are expired. **/
    private void checkExpiredNote() {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(requireActivity());
        for (NotesContent note: notesList) {
            Calendar storedDate = Calendar.getInstance();
            if (note.alertDate != null && note.alertDate.before(storedDate.getTime())) {
                note.alertDate = null;
                note.alertCode = null;
                database.ContentDao().update(note);
            }
        }
        database.close();
    }

    /** Due to the large size of this function, this is passed on to a class of its own. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return new NotesSubjectFragmentCreateView(NotesSubjectFragment.this)
                .onCreateView(inflater, container);
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Returns to
     * @see SubjectSelectFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new SubjectSelectFragment());
        return true;
    }

    /** Sort the notes based on the sorting format given.
     * @see NotesSubject
     * @see SortingComparators **/
    void sortNotes() {
        SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(requireActivity());
        int sortOrder = currentSubject.sortOrder;
        if (sortOrder == NotesSubject.SORT_ALPHABETICAL_DES) {
            // Sort by alphabetical order, descending
            Collections.sort(notesList, SortingComparators.noteTitleComparator);
            Collections.reverse(notesList);
        } else if (sortOrder == NotesSubject.SORT_DATE_ASC) {
            Collections.sort(notesList, SortingComparators.noteDateComparator);
        } else if (sortOrder == NotesSubject.SORT_DATE_DES) {
            Collections.sort(notesList, SortingComparators.noteDateComparator);
            Collections.reverse(notesList);
        } else {
            // Sort by alphabetical order, ascending
            if (sortOrder != NotesSubject.SORT_ALPHABETICAL_ASC) {
                // Default to this if sortOrder is invalid
                currentSubject.sortOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
            }
            Collections.sort(notesList, SortingComparators.noteTitleComparator);
        }
        database.close();
    }
}
