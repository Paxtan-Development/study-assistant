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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.fragment.notes.subject.NotesSubjectFragment;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.ui.AutoDismissDialog;
import com.pcchin.studyassistant.ui.ExtendedFragment;
import com.pcchin.studyassistant.ui.MainActivity;
import com.pcchin.studyassistant.utils.notes.NotesNotifyReceiver;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NotesEditFragment extends Fragment implements ExtendedFragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1",
            ARG_PARAM2 = "param2";

    private SubjectDatabase database;
    private NotesSubject subject;
    private ArrayList<ArrayList<String>> subjContents;

    private boolean hasParent;
    private String notesSubject;
    private String notesTitle;

    // Used only if hasParent
    private int notesOrder;

    private boolean subjModified = false;
    // Used only if subjModified
    private String targetNotesSubject;
    private ArrayList<ArrayList<String>> targetSubjContents;

    /** Default constructor. **/
    public NotesEditFragment() {}

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

        if (getArguments() != null && getActivity() != null) {
            database = GeneralFunctions.getSubjectDatabase(getActivity());
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
            getActivity().setTitle(notesSubject);
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

        // Set min height to match that of the scrollView
        if (getActivity() != null) {
            returnView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    returnView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    View scrollView = returnView.findViewById(R.id.n4_scroll);
                    ((EditText) returnView.findViewById(R.id.n4_edit)).setMinHeight(scrollView.getHeight());
                }
            });
        }
        return returnView;
    }

    /** Sets the menu for the fragment **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n4, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Changes the subject that the note will be saved to. **/
    public void onSubjPressed() {
        if (getContext() != null) {
            final Spinner subjListSpinner = new Spinner(getContext());
            // Get all subject titles
            List<String> subjTitleList = new ArrayList<>();
            if (subjModified) {
                subjTitleList.add(targetNotesSubject);
            } else {
                subjTitleList.add(notesSubject);
            }
            List<NotesSubject> allSubjList = database.SubjectDao().getAll();
            for (NotesSubject subject : allSubjList) {
                if ((subjModified && !Objects.equals(subject.title, targetNotesSubject))
                || (!subjModified && !Objects.equals(subject.title, notesSubject))) {
                    subjTitleList.add(subject.title);
                }
            }

            // Set spinner adaptor
            ArrayAdapter<String> subjAdaptor = new ArrayAdapter<>
                    (getContext(), android.R.layout.simple_spinner_item, subjTitleList);
            subjAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subjListSpinner.setAdapter(subjAdaptor);

            // Show dialog
            new AutoDismissDialog(getString(R.string.n_change_subj), subjListSpinner,
                    new DialogInterface.OnClickListener[]{(dialog, which) -> {
                        subjModified = true;
                        targetNotesSubject = subjListSpinner.getSelectedItem().toString();
                        if (getActivity() != null) {
                            getActivity().setTitle(targetNotesSubject);
                        }
                        targetSubjContents = database.SubjectDao()
                                .search(targetNotesSubject).contents;
                    }, (dialog, which) -> dialog.dismiss(), null})
                    .show(getParentFragmentManager(), "NotesEditFragment.1");
        }
    }

    /** Saves the note to the subject selected. **/
    public void onSavePressed() {
        // Check if title is empty
        if (getActivity() != null && getView() != null && ((EditText) getView()
                .findViewById(R.id.n4_title)).getText().toString()
                .replaceAll("\\s+", "").length() > 0) {
            // Save original as ArrayList
            ArrayList<String> previousNote;
            if (hasParent) {
                previousNote = subjContents.get(notesOrder);
            } else {
                previousNote = new ArrayList<>();
            }
            ArrayList<String> updatedNote = new ArrayList<>();
            FileFunctions.checkNoteIntegrity(updatedNote);
            updatedNote.set(0, ((EditText) getView().findViewById(R.id.n4_title)).getText().toString());
            updatedNote.set(1, ConverterFunctions.standardDateTimeFormat.format(new Date()));
            updatedNote.set(2, ((EditText) getView().findViewById(R.id.n4_edit)).getText().toString());

            AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            if (manager != null && previousNote.size() >= 6 && previousNote.get(5) != null) {
                // Delete old notification
                Intent previousIntent = new Intent(getActivity(), NotesNotifyReceiver.class);
                previousIntent.putExtra(MainActivity.INTENT_VALUE_TITLE, previousNote.get(0));
                previousIntent.putExtra(MainActivity.INTENT_VALUE_MESSAGE, previousNote.get(2));
                previousIntent.putExtra(MainActivity.INTENT_VALUE_SUBJECT, notesSubject);
                previousIntent.putExtra(MainActivity.INTENT_VALUE_REQUEST_CODE, previousNote.get(5));
                manager.cancel(PendingIntent.getBroadcast(getContext(), Integer.parseInt(
                        previousNote.get(5)), previousIntent, 0));

                // Set new notification
                Intent newIntent = new Intent(getActivity(), NotesNotifyReceiver.class);
                newIntent.putExtra(MainActivity.INTENT_VALUE_TITLE, updatedNote.get(0));
                newIntent.putExtra(MainActivity.INTENT_VALUE_MESSAGE, updatedNote.get(2));
                newIntent.putExtra(MainActivity.INTENT_VALUE_SUBJECT, targetNotesSubject);
                newIntent.putExtra(MainActivity.INTENT_VALUE_REQUEST_CODE, previousNote.get(5));
                try {
                    Date targetDate = ConverterFunctions.standardDateTimeFormat.parse(previousNote.get(4));
                    if (targetDate != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            manager.setExactAndAllowWhileIdle(AlarmManager.RTC, targetDate.getTime(),
                                    PendingIntent.getBroadcast(getContext(),
                                            Integer.parseInt(previousNote.get(5)), newIntent, 0));
                        } else {
                            manager.setExact(AlarmManager.RTC, targetDate.getTime(), PendingIntent
                                    .getBroadcast(getContext(),
                                            Integer.parseInt(previousNote.get(5)), newIntent, 0));
                        }
                        // Updates value to note
                        updatedNote.set(4, ConverterFunctions.standardDateTimeFormat.format(targetDate));
                        updatedNote.set(5, previousNote.get(5));
                    }
                } catch (ParseException e) {
                    Log.w(MainActivity.LOG_APP_NAME, "Parse Error: Date " + previousNote.get(4)
                            + " could not be parsed under standard date time format.");
                }
            }

            // Toast at start as different objects have different displayFragments
            Toast.makeText(getContext(), R.string.n4_note_saved, Toast.LENGTH_SHORT).show();
            if (subjModified && !Objects.equals(targetNotesSubject, notesSubject)) {
                if (hasParent) {
                    // Delete original
                    subjContents.remove(notesOrder);
                    subject.contents = subjContents;
                    database.SubjectDao().update(subject);
                }
                // Add new note to new subject
                targetSubjContents.add(updatedNote);
                NotesSubject targetSubject = database.SubjectDao().search(targetNotesSubject);
                if (targetSubject != null) {
                    targetSubject.contents = targetSubjContents;
                }
                database.SubjectDao().update(targetSubject);

                // Go to NotesViewFragment
                ((MainActivity) getActivity()).displayNotes(targetNotesSubject, targetSubjContents.size());
                ((MainActivity) getActivity()).pager.setCurrentItem(targetSubjContents.size() - 1);

            } else {
                if (hasParent) {
                    // Modify original
                    subjContents.set(notesOrder, updatedNote);
                    subject.contents = subjContents;
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayNotes(notesSubject, subjContents.size());
                    ((MainActivity) getActivity()).pager.setCurrentItem(notesOrder);
                } else {
                    // Add new note
                    subjContents.add(updatedNote);
                    subject.contents = subjContents;
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayNotes(notesSubject, subjContents.size());
                    ((MainActivity) getActivity()).pager.setCurrentItem(subjContents.size() - 1);
                }
            }
            database.close();
        } else {
            Toast.makeText(getContext(), R.string.n2_error_note_title_empty, Toast.LENGTH_SHORT).show();
        }
    }

    /** Cancel all the changes and return to
     * @see NotesSubjectFragment **/
    public void onCancelPressed() {
        // Go back to NotesViewFragment of subject
        if (getActivity() != null) {
            if (hasParent) {
                ((MainActivity) getActivity()).displayNotes(notesSubject, subjContents.size());
                ((MainActivity) getActivity()).pager.setCurrentItem(notesOrder, false);
            } else {
                ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                        .newInstance(notesSubject));
            }
        }
    }

    /** Forwards to onSavePressed() to ensure consistency when dealing with AlertDialogs.
     * @see MainActivity safeOnBackPressed() **/
    @Override
    public boolean onBackPressed() {
        new AutoDismissDialog(getString(R.string.return_val), getString(R.string.n4_save_note),
                new String[]{getString(R.string.yes), getString(R.string.no),
                        getString(android.R.string.cancel)},
                new DialogInterface.OnClickListener[]{
                        (dialogInterface, i) -> onSavePressed(),
                        (dialogInterface, i) -> onCancelPressed(),
                        (dialogInterface, i) -> dialogInterface.dismiss()})
                .show(getParentFragmentManager(), "NotesEditFragment.2");
        return true;
    }
}
