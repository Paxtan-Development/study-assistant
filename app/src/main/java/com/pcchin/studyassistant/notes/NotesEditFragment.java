package com.pcchin.studyassistant.notes;

import android.arch.persistence.room.Room;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;

public class NotesEditFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SubjectDatabase database;
    private NotesSubject currentSubject;
    private ArrayList<ArrayList<String>> subjContents;

    private boolean hasParent;
    // Used if note hasParent
    private String notesSubject;
    private int notesOrder;
    // Used if note !hasParent
    private String notesTitle;

    public NotesEditFragment() {}

    // Title is the title of the new note, without subject
    public static NotesEditFragment newInstance(String title) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, title);
        fragment.hasParent = false;
        fragment.setArguments(args);
        return fragment;
    }

    // Subject is the title of the selected subject, order is the order of the note in the subject
    public static NotesEditFragment newInstance(String subject, String order) {
        NotesEditFragment fragment = new NotesEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, subject);
        args.putInt(ARG_PARAM2, Integer.valueOf(order));
        fragment.hasParent = false;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getActivity() != null) {
            database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                    "notesSubject").allowMainThreadQueries().build();
            if (hasParent) {
                notesSubject = getArguments().getString(ARG_PARAM1);
                notesOrder = getArguments().getInt(ARG_PARAM2);
                currentSubject = database.SubjectDao().search(notesSubject);

                // Set title
                getActivity().setTitle(currentSubject.title);

                // Pass values to MainActivity
                ((MainActivity) getActivity()).activityVal1 = notesSubject;
                ((MainActivity) getActivity()).activityVal2 = String.valueOf(notesOrder);

                // Used value to prevent jsonToArray from being called multiple times
                subjContents = GeneralFunctions
                        .jsonToArray(currentSubject.contents);
                if (subjContents != null && notesOrder < subjContents.size()) {
                    notesTitle = subjContents.get(notesOrder).get(0);
                }
            } else {
                notesTitle = getArguments().getString(ARG_PARAM1);
                getActivity().setTitle(notesTitle);

                // Pass values to MainActivity
                ((MainActivity) getActivity()).activityVal1 = notesTitle;
                ((MainActivity) getActivity()).activityVal2 = null;
            }
        }
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
        }

        // Set min height to 80% of screen size
        if (getActivity() != null) {
            Point endPt = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(endPt);
            ((EditText) returnView.findViewById(R.id.n4_edit)).setMinHeight(endPt.y * 7 / 10);
        }
        return returnView;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
