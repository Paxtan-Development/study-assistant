package com.pcchin.studyassistant.notes;

import android.arch.persistence.room.Room;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout currentView;

    private boolean hasParent;
    private String notesSubject;
    private String notesTitle;
    // Used only if hasParent
    private int notesOrder;

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
            // Get values from newInstance
            notesSubject = getArguments().getString(ARG_PARAM1);
            currentSubject = database.SubjectDao().search(notesSubject);
            if (hasParent) {
                // Set title
                notesOrder = getArguments().getInt(ARG_PARAM2);
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
                // Get values from newInstance
                notesTitle = getArguments().getString(ARG_PARAM2);
                getActivity().setTitle(notesTitle);

                // Pass values to MainActivity
                ((MainActivity) getActivity()).activityVal1 = notesSubject;
                ((MainActivity) getActivity()).activityVal2 = notesTitle;
            }

            // Pass hasParent to MainActivity
            ((MainActivity) getActivity()).activityBool1 = hasParent;
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
            ((EditText) returnView.findViewById(R.id.n4_title)).addTextChangedListener(syncTitleTextWatcher);
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
