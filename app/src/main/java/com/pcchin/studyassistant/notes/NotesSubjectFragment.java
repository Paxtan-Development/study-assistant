package com.pcchin.studyassistant.notes;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.FragmentOnBackPressed;
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;

public class NotesSubjectFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_SUBJECT = "notesSubject";
    private static final int MAXLINES = 4;

    private SubjectDatabase subjectDatabase;
    private ArrayList<ArrayList<String>> notesArray;
    private String notesSubject;

    public NotesSubjectFragment() {}

    // Subject is the title fo the subject selected
    public static NotesSubjectFragment newInstance(String subject) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null && getActivity() != null) {
            subjectDatabase = Room.databaseBuilder(getContext(),
                    SubjectDatabase.class, "notesSubject")
                    .allowMainThreadQueries().build();

            // Get basic info & set title
            if (getArguments() != null) {
                notesSubject = getArguments().getString(ARG_SUBJECT);
            }

            // Check if subject exists in database
            NotesSubject currentSubject = subjectDatabase.SubjectDao().search(notesSubject);
            if (currentSubject == null) {
                Toast.makeText(getContext(), getString(R.string.n2_error_missing_subject),
                        Toast.LENGTH_SHORT).show();
                // Return to NotesSelectFragment if not
                subjectDatabase.close();
                ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());

            } else {
                // Get notes from database
                getActivity().setTitle(notesSubject);
                notesArray = GeneralFunctions.jsonToArray(subjectDatabase.SubjectDao()
                                .search(notesSubject).contents);
            }
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.fragment_notes_subject,
                container, false);
        LinearLayout returnView = returnScroll.findViewById(R.id.n2);

        // Check if stored data may be corrupt
        if (notesArray == null) {
            Toast.makeText(getContext(), getString(R.string.n_error_corrupt),
                    Toast.LENGTH_SHORT).show();
            notesArray = new ArrayList<>();
        }

        // Check if any of the notes is corrupt (Each note has 3 Strings: Title, Date, Contents)
        boolean anyCorrupt = false;
        for (int i = 0; i < notesArray.size(); i++) {
            ArrayList<String> note = notesArray.get(i);
            if (note.size() < 3) {
                // Filling in nonexistent values
                for (int j = 0; j < 3 - note.size(); j++) {
                    note.add("");
                }
                anyCorrupt = true;
            }

            // Add note to view
            @SuppressLint("InflateParams") LinearLayout miniNote = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.n2_notes_mini, null);
            ((TextView) miniNote.findViewById(R.id.n2_mini_title)).setText(note.get(0));
            ((TextView) miniNote.findViewById(R.id.n2_mini_date)).setText(String.format("%s%s",
                    getString(R.string.n_last_edited), note.get(1)));
            ((EditText) miniNote.findViewById(R.id.n2_mini_content)).setText(note.get(2));
            ((EditText) miniNote.findViewById(R.id.n2_mini_content)).setMaxLines(MAXLINES);

            // Conversion formula: px = sp / dpi + padding between lines
            ((EditText) miniNote.findViewById(R.id.n2_mini_content)).setHeight
                    ((int) (MAXLINES * 18 * getResources().getDisplayMetrics().density) +
                            (int) ((MAXLINES - 1)* 18 * ((EditText) miniNote.findViewById(R.id.n2_mini_content))
                                    .getLineSpacingMultiplier()));
            // Set on click listener
            final int finalI = i;
            View.OnClickListener displayNoteListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getActivity() != null) {
                        subjectDatabase.close();
                        ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                                .newInstance(notesSubject, finalI));
                    }
                }
            };

            miniNote.setOnClickListener(displayNoteListener);
            miniNote.findViewById(R.id.n2_mini_content).setOnClickListener(displayNoteListener);
            returnView.addView(miniNote);
        }

        if (anyCorrupt) {
            Toast.makeText(getContext(), getString(R.string.n2_error_some_corrupt)
                    , Toast.LENGTH_SHORT).show();
        }
        return returnScroll;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onNewNotePressed() {
        if (getContext() != null && getActivity() != null) {
            @SuppressLint("InflateParams") final View popupView = getLayoutInflater()
                    .inflate(R.layout.popup_new_title, null);
            AlertDialog newNoteDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n2_new_note)
                    .setView(popupView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            // OnClickListeners implemented separately to prevent
            // dialog from being dismissed after button click
            newNoteDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    ((EditText) popupView.findViewById(R.id.popup_input)).setHint(R.string.title);
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String popupInputText = ((EditText) popupView
                                            .findViewById(R.id.popup_input))
                                            .getText().toString();

                                    // Check if input is blank
                                    if (popupInputText.replaceAll("\\s+", "")
                                            .length() == 0) {
                                        ((TextView) popupView.findViewById(R.id.popup_error))
                                                .setText(R.string.n2_error_note_title_empty);
                                    } else if (getActivity() != null){
                                        // Edit new note
                                        dialog.dismiss();
                                        ((MainActivity) getActivity()).displayFragment
                                                (NotesEditFragment.newInstance(
                                                        notesSubject, popupInputText));
                                    }
                                }
                            });
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                }
            });
            newNoteDialog.show();
        }
    }

    public void onExportPressed() {
        // TODO: Export
    }

    public void onDeletePressed() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.del)
                    .setMessage(R.string.n2_del_confirm)
                    .setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getContext() != null && getActivity() != null) {
                                // Delete subject
                                SubjectDatabase database = Room.databaseBuilder(getContext(),
                                        SubjectDatabase.class, "notesSubject")
                                        .allowMainThreadQueries().build();
                                NotesSubject delTarget = database.SubjectDao().search(notesSubject);
                                if (delTarget != null) {
                                    database.SubjectDao().delete(delTarget);
                                }
                                database.close();
                                // Return to NotesSelectFragment
                                Toast.makeText(getContext(),
                                        getString(R.string.n2_deleted), Toast.LENGTH_SHORT).show();
                                GeneralFunctions.updateNavView((MainActivity) getActivity());
                                ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
            return true;
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
