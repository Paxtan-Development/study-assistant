package com.pcchin.studyassistant.notes;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.List;

public class NotesSelectFragment extends Fragment {
    private SubjectDatabase subjectDatabase;

    public NotesSelectFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null && getActivity() != null) {
            subjectDatabase = Room.databaseBuilder(getContext(), SubjectDatabase.class, "notesSubject")
                    .allowMainThreadQueries().build();
            getActivity().setTitle(R.string.app_name);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View returnView = inflater.inflate(R.layout.fragment_notes_select, container, false);

        // Add existing subjects
        final List<NotesSubject> subjectList = subjectDatabase.SubjectDao().getAll();
        for (int i = 0; i < subjectList.size(); i++) {
            @SuppressLint("InflateParams") Button subjectBtn = (Button) getLayoutInflater()
                    .inflate(R.layout.hyperlink_btn, null);
            subjectBtn.setText(subjectList.get(i).title);
            final int finalI = i;
            subjectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to notesSubjectFragment
                    if (getActivity() != null) {
                        subjectDatabase.close();
                        ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment.newInstance(
                                subjectList.get(finalI).title
                        ));
                    }
                }
            });
            ((LinearLayout) returnView.findViewById(R.id.n1_notes_list)).addView(subjectBtn, i);
        }

        returnView.findViewById(R.id.n1_create).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                // Ask for title
                if (getActivity() != null) {
                    GeneralFunctions.showNewSubject(getContext(), (MainActivity) getActivity(), subjectDatabase);
                }
            }
        });

        returnView.findViewById(R.id.n1_import).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Import
            }
        });
        return returnView;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
