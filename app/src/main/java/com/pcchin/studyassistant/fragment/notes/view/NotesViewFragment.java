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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.activity.MainActivity;

import java.util.ArrayList;

public class NotesViewFragment extends Fragment implements ExtendedFragment {
    private static final String ARG_SUBJECT = "noteSubject";
    private static final String ARG_ORDER = "noteOrder";

    ArrayList<String> notesInfo;
    String notesSubject;
    int notesOrder;
    boolean isLocked;
    boolean hasAlert;

    /** Default constructor. **/
    public NotesViewFragment() {
        // Default constructor.
    }

    /** Used when viewing a note.
     * @param subject is the title of the subject.
     * @param order is the order of the note in the notes list of the subject. **/
    @NonNull
    public static NotesViewFragment newInstance(String subject, int order) {
        NotesViewFragment fragment = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putInt(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the contents of the notes from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notesSubject = getArguments().getString(ARG_SUBJECT);
            notesOrder = getArguments().getInt(ARG_ORDER);
        }

        if (getContext() != null) {
            getNotesRequired();
        }

        setHasOptionsMenu(true);
    }

    /** Sets up the notes required and falls back to NotesSubjectFragment if an error occured. **/
    private void getNotesRequired() {
        SubjectDatabase database = GeneralFunctions.getSubjectDatabase(getActivity());
        ArrayList<ArrayList<String>> allNotes = database
                .SubjectDao().search(notesSubject).contents;

        // Check if notesOrder exists
        if (notesOrder < allNotes.size()) {
            notesInfo = allNotes.get(notesOrder);
            // Error message not shown as it is displayed in NotesSubjectFragment
            FileFunctions.checkNoteIntegrity(notesInfo);
            isLocked = (notesInfo.get(3) != null);
            hasAlert = (notesInfo.get(4) != null);
        } else if (getActivity() != null) {
            // Return to subject
            Toast.makeText(getActivity(), R.string.n_error_corrupt,
                    Toast.LENGTH_SHORT).show();
            ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                    .newInstance(notesSubject));
        }
        database.close();
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
        if (getActivity() != null) {
            getActivity().setTitle(notesSubject);
            Point endPt = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(endPt);
            // Height is set by Total height - bottom of last edited - navigation header height
            ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setMinLayoutHeight(returnView, endPt, this);
                }
            };
            returnView.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
        }
        return returnView;
    }

    /** Display the data for the fragment. **/
    private void displayFragmentData(@NonNull View returnView) {
        ((TextView) returnView.findViewById(R.id.n3_title)).setText(notesInfo.get(0));
        String contentText = notesInfo.get(2).replace("\n* ", "\n ● ");
        if (contentText.startsWith("* ")) {
            contentText = contentText.replaceFirst("\\* ", " ● ");
        }
        ((TextView) returnView.findViewById(R.id.n3_text)).setText(contentText);
        ((TextView) returnView.findViewById(R.id.n3_last_edited)).setText(String.format("%s%s",
                getString(R.string.n_last_edited), notesInfo.get(1)));
        if (notesInfo.size() >= 5 && notesInfo.get(4) != null && notesInfo.get(4).length() > 0) {
            ((TextView) returnView.findViewById(R.id.n3_notif_time)).setText(String.format("%s%s",
                    getString(R.string.n3_notif_time), notesInfo.get(4)));
        } else {
            returnView.findViewById(R.id.n3_notif_time).setVisibility(View.GONE);
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
        if (isLocked) {
            inflater.inflate(R.menu.menu_n3_locked, menu);
        } else {
            inflater.inflate(R.menu.menu_n3_unlocked, menu);
        }

        if (!hasAlert) {
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
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                    .newInstance(notesSubject, notesOrder));
            return true;
        }
        return false;
    }
}
