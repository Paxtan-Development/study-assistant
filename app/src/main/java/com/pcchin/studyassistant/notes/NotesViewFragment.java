package com.pcchin.studyassistant.notes;

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
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;

public class NotesViewFragment extends Fragment {
    private static final String ARG_SUBJECT = "notesSubject";
    private static final String ARG_ORDER = "notesOrder";

    private ArrayList<String> notesInfo;
    private String notesSubject;
    private int notesOrder;

    public NotesViewFragment() { }

    // Subject is the title of the subject, while order is the order of the note in the list
    public static NotesViewFragment newInstance(String subject, int order) {
        NotesViewFragment fragment = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putInt(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notesSubject = getArguments().getString(ARG_SUBJECT);
            notesOrder = getArguments().getInt(ARG_ORDER);
        }

        if (getContext() != null) {
            // Get notes required from database
            SubjectDatabase database = Room.databaseBuilder(getContext(), SubjectDatabase.class,
                    "notesSubject")
                    .allowMainThreadQueries().build();
            ArrayList<ArrayList<String>> allNotes = GeneralFunctions
                    .jsonToArray(database.SubjectDao().search(notesSubject).contents);

            // Check if notesOrder exists
            if (allNotes != null && notesOrder < allNotes.size()) {
                notesInfo = allNotes.get(notesOrder);
                // Error message not shown as it is displayed in NotesSubjectFragment
                while (notesInfo.size() < 3) {
                    notesInfo.add("");
                }
            } else if (getActivity() != null) {
                // Return to subject
                Toast.makeText(getActivity(), getString(R.string.n_error_corrupt),
                        Toast.LENGTH_SHORT).show();
                ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                        .newInstance(notesSubject));
            }
            database.close();
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView returnView = (ScrollView) inflater.inflate(
                R.layout.fragment_notes_view, container, false);
        ((EditText) returnView.findViewById(R.id.n3_text)).setText(notesInfo.get(2));
        ((TextView) returnView.findViewById(R.id.n3_last_edited)).setText(String.format("%s%s",
                getString(R.string.n_last_edited), notesInfo.get(1)));

        // Set title
        if (getActivity() != null) {
            getActivity().setTitle(notesInfo.get(0));
        }
        return returnView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n3, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onEditPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(NotesEditFragment
                    .newInstance(notesSubject, notesOrder));
        }
    }

    public void onExportPressed() {
        // TODO: Export
    }

    public void onDeletePressed() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.del)
                    .setMessage(R.string.n3_del_confirm)
                    .setPositiveButton(R.string.del, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (getContext() != null && getActivity() != null) {
                                SubjectDatabase database = Room.databaseBuilder(getContext(),
                                        SubjectDatabase.class, "notesSubject")
                                        .allowMainThreadQueries().build();
                                NotesSubject currentSubject = database.SubjectDao().search(notesSubject);
                                if (notesSubject != null) {
                                    // Check if contents is valid
                                    ArrayList<ArrayList<String>> contents = GeneralFunctions
                                            .jsonToArray(currentSubject.contents);
                                    if (contents != null) {
                                        if (notesOrder < contents.size()) {
                                            contents.remove(notesOrder);
                                        }
                                    } else {
                                        contents = new ArrayList<>();
                                    }
                                    // Update value in database
                                    currentSubject.contents = GeneralFunctions.arrayToJson(contents);
                                    database.SubjectDao().update(currentSubject);
                                    database.close();
                                    ((MainActivity) getActivity()).displayFragment
                                            (NotesSubjectFragment.newInstance(notesSubject));
                                } else {
                                    // In case the note somehow doesn't have a subject
                                    ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                                }
                                Toast.makeText(getContext(), getString(
                                        R.string.n3_deleted), Toast.LENGTH_SHORT).show();
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
