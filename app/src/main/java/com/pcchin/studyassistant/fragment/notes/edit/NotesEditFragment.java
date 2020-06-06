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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.pcchin.customdialog.DefaultDialogFragment;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.utils.misc.RandomString;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NotesEditFragment extends Fragment implements ExtendedFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1", ARG_PARAM2 = "param2";

    SubjectDatabase database;
    NotesSubject subject;
    List<NotesContent> noteList;

    boolean hasParent;
    int subjectId;

    NotesContent currentNote;
    boolean subjModified = false;
    // Used only if subjModified
    int targetSubjectId;

    /** Default constructor. **/
    public NotesEditFragment() {
        // Default constructor.
    }

    /** Used when creating a new note.
     * @param title is the title of the new note, without subject.
     * @param subjectId is the ID of the current subject that the note will save to. **/
    @NonNull
    public static NotesEditFragment newInstance(int subjectId, String title) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, subjectId);
        args.putString(ARG_PARAM2, title);
        fragment.hasParent = false;
        fragment.setArguments(args);
        return fragment;
    }

    /** Used when modifying an existing note.
     * @param subjectId is the title of the selected subject.
     * @param noteId is the order of the note in the subject.
     * 1 Parameter cannot be used as the first parameter must be the ID for the subject. **/
    @NonNull
    public static NotesEditFragment newInstance(int subjectId, int noteId) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, subjectId);
        args.putInt(ARG_PARAM2, noteId);
        fragment.hasParent = true;
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the data of the notes from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            database = DatabaseFunctions.getSubjectDatabase(requireActivity());
            // Get values from newInstance
            subjectId = getArguments().getInt(ARG_PARAM1);
            subject = database.SubjectDao().searchById(subjectId);
            noteList = database.ContentDao().searchBySubject(subjectId);
            if (hasParent) {
                // Set title
                int notesId = getArguments().getInt(ARG_PARAM2);
                currentNote = database.ContentDao().search(notesId);
            } else {
                // Get values from newInstance and creates a note without inserting into database
                String notesTitle = getArguments().getString(ARG_PARAM2);
                currentNote = new NotesContent(DatabaseFunctions.generateValidId(database,
                        DatabaseFunctions.ID_TYPE.NOTE), subjectId, Objects.requireNonNull(notesTitle),
                        "", new Date(), new RandomString(40).nextString());
            }
            requireActivity().setTitle(subject.title);
        }
        setHasOptionsMenu(true);
    }

    /** Closes the notes before the fragment exits. **/
    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

    /** Creates the fragment. Sets the content and listeners for the note. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_notes_edit,
                container, false);
        // Set content and title
        ((EditText) returnView.findViewById(R.id.n4_title)).setText(currentNote.noteTitle);
        ((EditText) returnView.findViewById(R.id.n4_edit)).setText(currentNote.noteContent);
        setMinHeight(returnView);
        return returnView;
    }

    /** Set the minimum height of the returned view to match that of the scrollView **/
    private void setMinHeight(@NonNull View returnView) {
        returnView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                returnView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View scrollView = returnView.findViewById(R.id.n4_scroll);
                ((EditText) returnView.findViewById(R.id.n4_edit)).setMinHeight(scrollView.getHeight());
            }
        });
    }

    /** Sets the menu for the fragment **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n4, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Forwards to onSavePressed() to ensure consistency when dealing with AlertDialogs.
     * @see MainActivity safeOnBackPressed() **/
    @Override
    public boolean onBackPressed() {
        new DefaultDialogFragment(new AlertDialog.Builder(requireContext())
                .setTitle(R.string.return_val)
                .setMessage(R.string.n4_save_note)
                .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> new NotesEditFragmentClick(NotesEditFragment.this).onSavePressed())
                .setNegativeButton(android.R.string.no, (dialogInterface, i) -> new NotesEditFragmentClick(NotesEditFragment.this).onCancelPressed())
                .setNeutralButton(android.R.string.cancel, null)
                .create())
                .show(getParentFragmentManager(), "NotesEditFragment.2");
        return true;
    }
}
