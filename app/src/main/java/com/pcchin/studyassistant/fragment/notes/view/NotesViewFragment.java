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

package com.pcchin.studyassistant.fragment.notes.view;

import android.annotation.SuppressLint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;

public class NotesViewFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_NOTE = "noteId";

    public NotesContent note;
    public NotesSubject notesSubject;

    /** Default constructor. **/
    public NotesViewFragment() {
        // Default constructor.
    }

    /** Used when viewing a note.
     * @param noteId is the ID of the note. **/
    @NonNull
    public static NotesViewFragment newInstance(int noteId) {
        NotesViewFragment fragment = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NOTE, noteId);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the contents of the notes from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int noteId = getArguments().getInt(ARG_NOTE);
            SubjectDatabase database = DatabaseFunctions.getSubjectDatabase(requireActivity());
            note = database.ContentDao().search(noteId);
            notesSubject = database.SubjectDao().searchById(note.subjectId);
        }
        setHasOptionsMenu(true);
    }

    /** Creates the fragment. The height of the content is updated based on the screen size. **/
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView returnView = (ScrollView) inflater.inflate(R.layout.fragment_notes_view, container, false);
        displayFragmentData(returnView);

        // Set min height corresponding to screen height
        requireActivity().setTitle(notesSubject.title);
        Point endPt = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(endPt);
        // Height is set by Total height - bottom of last edited - navigation header height
        ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMinLayoutHeight(returnView, endPt, this);
            }
        };
        returnView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        return returnView;
    }

    /** Display the data for the fragment. **/
    private void displayFragmentData(@NonNull View returnView) {
        ((TextView) returnView.findViewById(R.id.n3_title)).setText(note.noteTitle);
        String contentText = note.noteContent.replace("\n* ", "\n ● ");
        if (contentText.startsWith("* ")) {
            contentText = contentText.replaceFirst("\\* ", " ● ");
        }
        ((TextView) returnView.findViewById(R.id.n3_text)).setText(contentText);
        ((TextView) returnView.findViewById(R.id.n3_last_edited)).setText(String.format("%s%s",
                getString(R.string.n_last_edited), note.lastEdited));
        if (note.alertDate == null) {
            returnView.findViewById(R.id.n3_notif_time).setVisibility(View.GONE);
        } else {
            ((TextView) returnView.findViewById(R.id.n3_notif_time)).setText(String.format("%s%s",
                    getString(R.string.n3_notif_time), ConverterFunctions.standardDateTimeFormat
                            .format(note.alertDate)));
        }
    }

    /** Sets the minimum height of the layout **/
    private void setMinLayoutHeight(View returnView, Point endPt,
                                    ViewTreeObserver.OnGlobalLayoutListener listener) {
        int minHeight = 0;
        // Fragment may not be attached to context yet when the function is run,
        // and getResources() indirectly relies on getContext() as well
        if (getContext() != null) {
            int navBarId = getResources().getIdentifier("navigation_bar_height",
                    "dimen", "android");
            minHeight = endPt.y - returnView.findViewById(R.id.n3_notif_time).getBottom()
                    - (int) getResources().getDimension(R.dimen.nav_header_height);
            LinearLayout linearDisplay = returnView.findViewById(R.id.n3_linear);
            if (navBarId > 0) {
                minHeight -= getResources().getDimensionPixelSize(navBarId);
                linearDisplay.setPadding(0, 0, 0, 24 +
                        getResources().getDimensionPixelSize(navBarId));
            }
        }

        returnView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        ((TextView) returnView.findViewById(R.id.n3_text)).setMinHeight(minHeight);
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (note.lockedPass.length() == 0) {
            inflater.inflate(R.menu.menu_n3_unlocked, menu);
        } else {
            inflater.inflate(R.menu.menu_n3_locked, menu);
        }

        if (note.alertDate == null) {
            menu.findItem(R.id.n3_notif).setVisible(true);
            menu.findItem(R.id.n3_cancel_notif).setVisible(false);
        } else {
            menu.findItem(R.id.n3_notif).setVisible(false);
            menu.findItem(R.id.n3_cancel_notif).setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Returns to
     * @see NotesSubjectFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(NotesSubjectFragment
                .newInstance(notesSubject.subjectId, note.noteId));
        return true;
    }
}
