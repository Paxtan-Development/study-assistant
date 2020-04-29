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

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.util.ArrayList;

public class NotesEditFragment extends Fragment implements ExtendedFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1",
            ARG_PARAM2 = "param2";

    SubjectDatabase database;
    NotesSubject subject;
    ArrayList<ArrayList<String>> subjContents;

    boolean hasParent;
    String notesSubject;
    private String notesTitle;

    // Used only if hasParent
    int notesOrder;

    boolean subjModified = false;
    // Used only if subjModified
    String targetNotesSubject;
    ArrayList<ArrayList<String>> targetSubjContents;

    /** Default constructor. **/
    public NotesEditFragment() {
        // Default constructor.
    }

    /** Used when creating a new note.
     * @param title is the title of the new note, without subject.
     * @param subject is the current subject that the note will save to. **/
    @NonNull
    public static NotesEditFragment newInstance(String subject, String title) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putString(ARG_PARAM2, title);
        fragment.hasParent = false;
        fragment.setArguments(args);
        return fragment;
    }

    /** Used when modifying an existing note.
     * @param subject is the title of the selected subject.
     * @param order is the order of the note in the subject. **/
    @NonNull
    public static NotesEditFragment newInstance(String subject, int order) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putInt(ARG_PARAM2, order);
        fragment.hasParent = true;
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the data of the notes from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            database = GeneralFunctions.getSubjectDatabase(requireActivity());
            // Get values from newInstance
            notesSubject = getArguments().getString(ARG_PARAM1);
            subject = database.SubjectDao().search(notesSubject);
            subjContents = subject.contents;
            if (hasParent) {
                // Set title
                notesOrder = getArguments().getInt(ARG_PARAM2);
                if (subjContents != null && notesOrder < subjContents.size()) {
                    notesTitle = subjContents.get(notesOrder).get(0);
                }
            } else {
                // Get values from newInstance
                notesTitle = getArguments().getString(ARG_PARAM2);
            }
            requireActivity().setTitle(notesSubject);
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
        ((EditText) returnView.findViewById(R.id.n4_title)).setText(notesTitle);
        if (hasParent && notesOrder < subjContents.size() && subjContents.get(notesOrder).size() >= 3) {
            ((EditText) returnView.findViewById(R.id.n4_edit)).setText(subjContents
                    .get(notesOrder).get(2));
        }
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
        new AutoDismissDialog(getString(R.string.return_val), getString(R.string.n4_save_note),
                new String[]{getString(R.string.yes), getString(R.string.no),
                        getString(android.R.string.cancel)},
                new DialogInterface.OnClickListener[]{
                        (dialogInterface, i) -> new NotesEditFragmentClick(NotesEditFragment.this).onSavePressed(),
                        (dialogInterface, i) -> new NotesEditFragmentClick(NotesEditFragment.this).onCancelPressed(),
                        (dialogInterface, i) -> dialogInterface.dismiss()})
                .show(getParentFragmentManager(), "NotesEditFragment.2");
        return true;
    }
}
