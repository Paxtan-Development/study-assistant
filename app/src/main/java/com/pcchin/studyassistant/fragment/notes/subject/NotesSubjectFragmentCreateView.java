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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.functions.ConverterFunctions;

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

        // Add each note to the view
        for (int i = 0; i < fragment.notesList.size(); i++) {
            NotesContent currentNote = fragment.notesList.get(i);
            // Add note to view
            @SuppressLint("InflateParams") LinearLayout miniNote = (LinearLayout) fragment.getLayoutInflater()
                    .inflate(R.layout.n2_notes_mini, null);
            initMiniNote(miniNote, currentNote);
            initMiniNoteListener(miniNote, returnView, i);
            if (currentNote.noteId == fragment.previousNote) {
                returnScroll.post(() -> returnScroll.scrollTo(0, miniNote.getTop()));
            }
        }
        return returnScroll;
    }

    /** Initializes the mini note view. **/
    private void initMiniNote(@NonNull LinearLayout miniNote, NotesContent note) {
        ((TextView) miniNote.findViewById(R.id.n2_mini_title)).setText(note.noteTitle);
        ((TextView) miniNote.findViewById(R.id.n2_mini_date)).setText(String.format("%s%s",
                fragment.getString(R.string.n_last_edited), ConverterFunctions.standardDateTimeFormat.format(note.lastEdited)));
        String miniText = note.noteContent.replace("\n* ", "\n ● ");
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
            ((MainActivity) fragment.requireActivity()).displayNotes(fragment.currentSubject.subjectId);
            ((MainActivity) fragment.requireActivity()).pager.setCurrentItem(i, false);
        };

        miniNote.setOnClickListener(displayNoteListener);
        miniNote.findViewById(R.id.n2_mini_content).setOnClickListener(displayNoteListener);
        returnView.addView(miniNote);
    }

    /** Initializes the mini icons for the note. **/
    private void initNoteIcons(LinearLayout miniNote, @NonNull NotesContent note) {
        // Check if note is locked
        if (note.lockedPass == null) {
            miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.GONE);
        } else {
            miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.VISIBLE);
        }

        // Check if note has a alert attached
        if (note.alertDate == null) {
            miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.GONE);
        } else {
            miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.VISIBLE);
        }
        miniNote.findViewById(R.id.n2_mini_content).setVerticalScrollBarEnabled(false);
    }
}
