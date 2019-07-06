package com.pcchin.studyassistant.notes;

import android.app.AlertDialog;
import androidx.room.Room;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class NotesEditFragment extends Fragment implements FragmentOnBackPressed {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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
    public static NotesEditFragment newInstance(String subject, int order) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putInt(ARG_PARAM2, order);
        fragment.hasParent = true;
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the data of the notes from the database. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getActivity() != null) {
            database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                    "notesSubject")
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
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

    /** Closes the database before the fragment exits. **/
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
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n_change_subj)
                    .setView(subjListSpinner)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        subjModified = true;
                        targetNotesSubject = subjListSpinner.getSelectedItem().toString();
                        if (getActivity() != null) {
                            getActivity().setTitle(targetNotesSubject);
                        }
                        targetSubjContents = database.SubjectDao()
                                .search(targetNotesSubject).contents;
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    /** Saves the note to the subject selected. **/
    public void onSavePressed() {
        // Check if title is empty
        if (getActivity() != null && getView() != null && ((EditText) getView()
                .findViewById(R.id.n4_title)).getText().toString()
                .replaceAll("\\s+", "").length() > 0) {
            // Save original as ArrayList
            ArrayList<String> updatedNote = new ArrayList<>();
            updatedNote.add(((EditText) getView().findViewById(R.id.n4_title)).getText().toString());
            updatedNote.add(GeneralFunctions.standardDateTimeFormat.format(new Date()));
            updatedNote.add(((EditText) getView().findViewById(R.id.n4_edit)).getText().toString());
            updatedNote.add(null);

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
                ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(targetNotesSubject, targetSubjContents.size() - 1));

            } else {
                if (hasParent) {
                    // Modify original
                    subjContents.set(notesOrder, updatedNote);
                    subject.contents = subjContents;
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, notesOrder));
                } else {
                    // Add new note
                    subjContents.add(updatedNote);
                    subject.contents = subjContents;
                    database.SubjectDao().update(subject);
                    ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, subjContents.size() - 1));
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
                ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                        .newInstance(notesSubject, notesOrder));
            } else {
                ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                        .newInstance(notesSubject));
            }
        }
    }

    /** Forwards to onSavePressed() to ensure consistency when dealing with AlertDialogs.
     * @see MainActivity showGitlabUpdateNotif(JSONArray response) **/
    @Override
    public boolean onBackPressed() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.n4_return)
                .setMessage(R.string.n4_save_note)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> onSavePressed())
                .setNegativeButton(R.string.no, (dialogInterface, i) -> onCancelPressed())
                .setNeutralButton(android.R.string.cancel, ((dialogInterface, i) -> dialogInterface.dismiss()))
                .create().show();
        return true;
    }
}
