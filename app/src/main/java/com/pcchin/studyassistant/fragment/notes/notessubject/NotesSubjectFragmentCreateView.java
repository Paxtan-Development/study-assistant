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

package com.pcchin.studyassistant.fragment.notes.notessubject;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.ui.MainActivity;

import java.util.ArrayList;

/** Functions for handling the onCreateView function in
 * @see NotesSubjectFragment **/
class NotesSubjectFragmentCreateView {
    private static final int MAXLINES = 4;
    private NotesSubjectFragment fragment;

    /** The constructor for the class as fragment needs to be passed on. **/
    NotesSubjectFragmentCreateView(NotesSubjectFragment fragment) {
        this.fragment = fragment;
    }

    /** Creates the fragment.
     * Display each note and center the note that was previously selected if needed. **/
    View onCreateView(LayoutInflater inflater, ViewGroup container) {
        // Inflate the layout for this fragment
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.blank_list,
                container, false);
        LinearLayout returnView = returnScroll.findViewById(R.id.blank_linear);

        // Check if stored data may be corrupt
        if (fragment.notesArray == null) {
            Toast.makeText(fragment.getContext(), R.string.n_error_corrupt, Toast.LENGTH_SHORT).show();
            fragment.notesArray = new ArrayList<>();
        }

        // Check if any of the notes is corrupt (Each note has 3 Strings: Title, Date, Contents)
        boolean anyCorrupt = checkNoteCorrupt(returnScroll, returnView);

        if (anyCorrupt) {
            Toast.makeText(fragment.getContext(), R.string.n2_error_some_corrupt, Toast.LENGTH_SHORT).show();
        }
        return returnScroll;
    }

    /** Check if there are any notes that are corrupt, and if there are, return true; **/
    private boolean checkNoteCorrupt(ScrollView returnScroll, LinearLayout returnView) {
        for (int i = 0; i < fragment.notesArray.size(); i++) {
            ArrayList<String> note = fragment.notesArray.get(i);
            if (note.size() < 3) {
                // Filling in nonexistent values
                for (int j = 0; j < 3 - note.size(); j++) note.add("");
                return true;
            }
            // Implemented separately for backwards compatibility
            if (note.size() < 6) note.add(null);

            // Add note to view
            @SuppressLint("InflateParams") LinearLayout miniNote = (LinearLayout) fragment.getLayoutInflater()
                    .inflate(R.layout.n2_notes_mini, null);
            initMiniNote(miniNote, note);
            initMiniNoteListener(miniNote, returnView, i);

            // Scroll to last seen view
            if (fragment.previousOrder == i) returnScroll.post(() -> returnScroll.scrollTo(0, miniNote.getTop()));
        }
        return false;
    }

    /** Initializes the mini note view. **/
    private void initMiniNote(@NonNull LinearLayout miniNote, @NonNull ArrayList<String> note) {
        ((TextView) miniNote.findViewById(R.id.n2_mini_title)).setText(note.get(0));
        ((TextView) miniNote.findViewById(R.id.n2_mini_date)).setText(String.format("%s%s",
                fragment.getString(R.string.n_last_edited), note.get(1)));
        String miniText = note.get(2).replace("\n* ", "\n ● ");
        if (miniText.startsWith("* ")) {
            miniText = miniText.replaceFirst("\\* ", " ● ");
        }
        ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setText(miniText);
        ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setMaxLines(MAXLINES);
        initNoteIcons(miniNote, note);

        // Conversion formula: px = sp / dpi + padding between lines
        ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setHeight
                ((int) (MAXLINES * 18 * fragment.getResources().getDisplayMetrics().density) +
                        (int) ((MAXLINES - 1)* 18 * ((TextView) miniNote.findViewById(R.id.n2_mini_content))
                                .getLineSpacingMultiplier()));
    }

    /** Initializes the listener for the mini note. **/
    private void initMiniNoteListener(@NonNull LinearLayout miniNote, @NonNull LinearLayout returnView, final int i) {
        // Set on click listener
        View.OnClickListener displayNoteListener = v -> {
            if (fragment.getActivity() != null) {
                fragment.subjectDatabase.close();
                ((MainActivity) fragment.getActivity()).displayNotes(fragment.notesSubject, fragment.notesArray.size());
                ((MainActivity) fragment.getActivity()).pager.setCurrentItem(i, false);
            }
        };

        miniNote.setOnClickListener(displayNoteListener);
        miniNote.findViewById(R.id.n2_mini_content).setOnClickListener(displayNoteListener);
        returnView.addView(miniNote);
    }

    /** Initializes the mini icons for the note. **/
    private void initNoteIcons(LinearLayout miniNote, @NonNull ArrayList<String> note) {
        // Check if note is locked
        if (note.get(3) == null) {
            miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.GONE);
        } else {
            miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.VISIBLE);
        }

        // Check if note has a alert attached
        if (note.get(4) == null) {
            miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.GONE);
        } else {
            miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.VISIBLE);
        }
        miniNote.findViewById(R.id.n2_mini_content).setVerticalScrollBarEnabled(false);
    }
}
