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

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.NotesSelectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.utils.misc.SortingComparators;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class NotesSubjectFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_SUBJECT = "noteSubject",
            ARG_PREV = "previousOrder";

    static final int[] sortingList = new int[]{NotesSubject.SORT_ALPHABETICAL_ASC,
        NotesSubject.SORT_ALPHABETICAL_DES, NotesSubject.SORT_DATE_ASC, NotesSubject.SORT_DATE_DES};
    static final int[] sortingTitles = new int[]{R.string.n2_sort_alpha_asc, R.string.n2_sort_alpha_des,
            R.string.n2_sort_date_asc, R.string.n2_sort_date_des};
    static final int[] sortingImgs = new int[]{R.drawable.ic_sort_atz, R.drawable.ic_sort_zta,
            R.drawable.ic_sort_num_asc, R.drawable.ic_sort_num_des};

    SubjectDatabase subjectDatabase;
    ArrayList<ArrayList<String>> notesArray;
    String notesSubject;
    int previousOrder;

    /** Default constructor. **/
    public NotesSubjectFragment() {
        // Default constructor.
    }

    /** Used in all except when returning from a NotesViewFragment.
     * @param subject is the subject that is displayed. **/
    @NonNull
    public static NotesSubjectFragment newInstance(String subject) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        fragment.setArguments(args);
        return fragment;
    }

    /** Used when returning from a NotesViewFragment.
     * @param subject is the subject that is displayed.
     * @param previousOrder is the order of the note that was shown. **/
    @NonNull
    public static NotesSubjectFragment newInstance(String subject, int previousOrder) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putInt(ARG_PREV, previousOrder);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Retrieves all of the notes of the subject from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subjectDatabase = DatabaseFunctions.getSubjectDatabase(requireActivity());

        // Get basic info & set title
        if (getArguments() != null) {
            notesSubject = getArguments().getString(ARG_SUBJECT);
            previousOrder = getArguments().getInt(ARG_PREV);
        }
        checkSubjectExists(requireActivity());
        setHasOptionsMenu(true);
    }

    /** Check if the subject selected exists. **/
    private void checkSubjectExists(Activity activity) {
        NotesSubject currentSubject = subjectDatabase.SubjectDao().search(notesSubject);
        if (currentSubject == null) {
            Toast.makeText(requireContext(), R.string.n2_error_missing_subject,
                    Toast.LENGTH_SHORT).show();
            // Return to NotesSelectFragment if not
            subjectDatabase.close();
            ((MainActivity) activity).displayFragment(new NotesSelectFragment());

        } else {
            // Get notes from notes
            activity.setTitle(notesSubject);
            NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
            notesArray = subject.contents;
            if (notesArray != null) {
                // Sort notes just in case
                sortNotes(subject);
                checkExpiredNote();
            }
            subjectDatabase.SubjectDao().update(subject);
        }
    }

    /** Checks for any notes that are expired. **/
    private void checkExpiredNote() {
        for (ArrayList<String> note: notesArray) {
            FileFunctions.checkNoteIntegrity(note);
            Calendar storedDate = Calendar.getInstance();
            if (note.get(4) != null) {
                try {
                    Date tempDate = ConverterFunctions.standardDateTimeFormat.parse(note.get(4));
                    if (tempDate != null) {
                        storedDate.setTime(tempDate);
                        // Delete alert if time passed
                        if (storedDate.before(Calendar.getInstance())) {
                            note.set(4, null);
                            note.set(5, null);
                        }
                    }
                } catch (ParseException e) {
                    Log.w(ActivityConstants.LOG_APP_NAME, "Parse Error: Failed to parse date "
                            + note.get(4) + " as standard date time.");
                }
            }
        }
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
     * @see NotesSelectFragment **/
    @Override
    public boolean onBackPressed() {
        subjectDatabase.close();
        ((MainActivity) requireActivity()).displayFragment(new NotesSelectFragment());
        return true;
    }

    /** Sort the notes based on the sorting format given.
     * @see NotesSubject
     * @see SortingComparators **/
    void sortNotes(@NonNull NotesSubject subject) {
        int sortOrder = subject.sortOrder;
        if (sortOrder == NotesSubject.SORT_ALPHABETICAL_DES) {
            // Sort by alphabetical order, descending
            Collections.sort(notesArray, SortingComparators.firstValComparator);
            Collections.reverse(notesArray);
        } else if (sortOrder == NotesSubject.SORT_DATE_ASC) {
            Collections.sort(notesArray, SortingComparators.secondValDateComparator);
        } else if (sortOrder == NotesSubject.SORT_DATE_DES) {
            Collections.sort(notesArray, SortingComparators.secondValDateComparator);
            Collections.reverse(notesArray);
        } else {
            // Sort by alphabetical order, ascending
            if (sortOrder != NotesSubject.SORT_ALPHABETICAL_ASC) {
                // Default to this if sortOrder is invalid
                subject.sortOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
            }
            Collections.sort(notesArray, SortingComparators.firstValComparator);
        }
        subject.contents = notesArray;
        subjectDatabase.SubjectDao().update(subject);
    }
}
