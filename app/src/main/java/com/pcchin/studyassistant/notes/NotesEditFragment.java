package com.pcchin.studyassistant.notes;

import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class NotesEditFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SubjectDatabase database;
    private NotesSubject subject;
    private ArrayList<ArrayList<String>> subjContents;
    private LinearLayout currentView;

    private boolean hasParent;
    private String notesSubject;
    private String notesTitle;

    // Used only if hasParent
    private int notesOrder;

    private boolean subjModified = false;
    // Used only if subjModified
    private String targetNotesSubject;
    private ArrayList<ArrayList<String>> targetSubjContents;

    private TextWatcher syncTitleTextWatcher = new TextWatcher() {
        // A TextWatcher that automatically syncs its text to the title
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Clears previous error
            ((TextView) currentView.findViewById(R.id.n4_title_error))
                    .setText(R.string.blank);
        }

        @Override
        public void afterTextChanged(@NonNull Editable s) {
            if (getActivity() != null) {
                getActivity().setTitle(s.toString());
            }
            if (s.toString().length() == 0) {
                // Check if title is empty
                ((TextView) currentView.findViewById(R.id.n4_title_error))
                        .setText(R.string.n2_error_note_title_empty);
            }
        }
    };

    public NotesEditFragment() {}

    // Title is the title of the new note, without subject
    public static NotesEditFragment newInstance(String subject, String title) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putString(ARG_PARAM2, title);
        fragment.hasParent = false;
        fragment.setArguments(args);
        return fragment;
    }

    // Subject is the title of the selected subject, order is the order of the note in the subject
    public static NotesEditFragment newInstance(String subject, int order) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putInt(ARG_PARAM2, order);
        fragment.hasParent = true;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getActivity() != null) {
            database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                    "notesSubject").allowMainThreadQueries().build();
            // Get values from newInstance
            notesSubject = getArguments().getString(ARG_PARAM1);
            subject = database.SubjectDao().search(notesSubject);
            subjContents = GeneralFunctions.jsonToArray(subject.contents);
            if (hasParent) {
                // Set title
                notesOrder = getArguments().getInt(ARG_PARAM2);
                getActivity().setTitle(subject.title);

                if (subjContents != null && notesOrder < subjContents.size()) {
                    notesTitle = subjContents.get(notesOrder).get(0);
                }
            } else {
                // Get values from newInstance
                notesTitle = getArguments().getString(ARG_PARAM2);
                getActivity().setTitle(notesTitle);
            }
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        database.close();
    }

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
        } else if (!hasParent) {
            // Change activity title when note subject changed
            ((EditText) returnView.findViewById(R.id.n4_title))
                    .addTextChangedListener(syncTitleTextWatcher);
        }

        // Set min height to 65% of screen size
        if (getActivity() != null) {
            Point endPt = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(endPt);
            ((EditText) returnView.findViewById(R.id.n4_edit)).setMinHeight(endPt.y * 65 / 100);
        }
        currentView = (LinearLayout) returnView;
        return returnView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n4, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

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
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n_change_subj)
                    .setView(subjListSpinner)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            subjModified = true;
                            targetNotesSubject = subjListSpinner.getSelectedItem().toString();
                            System.out.println(subjListSpinner.getSelectedItem());
                            targetSubjContents = GeneralFunctions.jsonToArray(database.SubjectDao()
                                    .search(targetNotesSubject).contents);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
    }

    public void onSavePressed() {
        // Check if title is empty
        if (getActivity() != null && getView() != null && ((EditText) getView()
                .findViewById(R.id.n4_title)).getText().length() > 0) {
            // Save original as ArrayList
            ArrayList<String> updatedNote = new ArrayList<>();
            updatedNote.add(((EditText) getView().findViewById(R.id.n4_title)).getText().toString());
            updatedNote.add(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
                    .format(new Date()));
            updatedNote.add(((EditText) getView().findViewById(R.id.n4_edit)).getText().toString());

            // Toast at start as different objects have different displayFragments
            Toast.makeText(getContext(), getString(R.string.n4_note_saved), Toast.LENGTH_SHORT).show();
            if (subjModified && !Objects.equals(targetNotesSubject, notesSubject)) {
                if (hasParent) {
                    // Delete original
                    subjContents.remove(notesOrder);
                    subject.contents = GeneralFunctions.arrayToJson(subjContents);
                    database.SubjectDao().update(subject);
                }
                // Add new note to new subject
                targetSubjContents.add(updatedNote);
                NotesSubject targetSubject = database.SubjectDao().search(targetNotesSubject);
                if (targetSubject != null) {
                    targetSubject.contents = GeneralFunctions.arrayToJson(targetSubjContents);
                }
                database.SubjectDao().update(targetSubject);

                // Go to NotesViewFragment
                ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(targetNotesSubject, targetSubjContents.size() - 1));

            } else {
                if (hasParent) {
                    // Modify original
                    subjContents.set(notesOrder, updatedNote);
                    subject.contents = GeneralFunctions.arrayToJson(subjContents);
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, notesOrder));
                } else {
                    // Add new note
                    subjContents.add(updatedNote);
                    subject.contents = GeneralFunctions.arrayToJson(subjContents);
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, subjContents.size() - 1));
                }
            }
            database.close();
        } else {
            Toast.makeText(getContext(), getString(R.string.n2_error_note_title_empty),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onCancelPressed() {
        // Go back to NotesViewFragment of subject
        if (getActivity() != null) {
            if (hasParent) {
                ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, notesOrder));
            } else {
                ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                        .newInstance(notesSubject));
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
