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

package com.pcchin.studyassistant.fragment.notes;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.file.notes.importsubj.ImportSubjectStatic;
import com.pcchin.studyassistant.fragment.main.MainFragment;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.DatabaseFunctions;
import com.pcchin.studyassistant.functions.UIFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;

import java.util.List;

public class SubjectSelectFragment extends Fragment implements ExtendedFragment {
    /** Default constructor. **/
    public SubjectSelectFragment() {
        // Default constructor.
    }

    /** Initializes the fragment and the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().setTitle(R.string.notes);
        setHasOptionsMenu(true);
    }

    /** Creates the fragment. Add existing subjects to the fragment. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.blank_list, container, false);

        // Add existing subjects
        SubjectDatabase subjectDatabase = DatabaseFunctions.getSubjectDatabase(requireActivity());
        final List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        subjectDatabase.close();
        for (int i = 0; i < subjectList.size(); i++) {
            @SuppressLint("InflateParams") Button subjectBtn = (Button) getLayoutInflater()
                    .inflate(R.layout.hyperlink_btn, null);
            subjectBtn.setText(subjectList.get(i).title);
            final int finalI = i;
            subjectBtn.setOnClickListener(v -> {
                // Go to notesSubjectFragment
                ((MainActivity) requireActivity()).displayFragment(NotesSubjectFragment.newInstance(
                        subjectList.get(finalI).subjectId));
            });
            ((LinearLayout) returnView.findViewById(R.id.blank_linear)).addView(subjectBtn, i);
        }
        return returnView;
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_selector, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Creates a new subject. **/
    public void onNewSubjectPressed() {
        UIFunctions.showNewSubject(((MainActivity) requireActivity()));
    }

    /** Imports an existing zip/.subject file. **/
    public void onImportPressed() {
        new Handler().post(() -> ImportSubjectStatic.displayImportDialog((MainActivity) requireActivity()));
    }

    /** Returns to
     * @see MainFragment **/
    @Override
    public boolean onBackPressed() {
        ((MainActivity) requireActivity()).displayFragment(new MainFragment());
        return true;
    }
}
