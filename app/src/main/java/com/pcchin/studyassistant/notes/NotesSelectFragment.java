package com.pcchin.studyassistant.notes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.main.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;
import java.util.List;

public class NotesSelectFragment extends Fragment {
    private SubjectDatabase subjectDatabase;

    public NotesSelectFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null) {
            subjectDatabase = Room.databaseBuilder(getContext(), SubjectDatabase.class, "notesSubject")
                    .allowMainThreadQueries().build();
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


        @SuppressLint("InflateParams") final View popupView = getLayoutInflater()
                .inflate(R.layout.n1_popup_new_subject, null);
        returnView.findViewById(R.id.n1_create).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("InflateParams")
            @Override
            public void onClick(View v) {
                // Ask for title
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.n1_new_subject)
                        .setView(popupView)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String inputText = ((EditText) popupView
                                        .findViewById(R.id.n1_popup_input)).getText().toString();
                                TextView errorText = popupView.findViewById(R.id.n1_popup_error);

                                // Preliminary checks if subject name is taken or is empty
                                if (inputText.replaceAll("\\s+", "").length() == 0) {
                                    errorText.setText(R.string.n1_error_subject_empty);
                                } else if (subjectDatabase.SubjectDao().search(inputText) != null) {
                                    errorText.setText(R.string.n1_error_subject_exists);
                                } else {
                                    // Create subject
                                    if (getActivity() != null) {
                                        subjectDatabase.SubjectDao().insert(
                                                new NotesSubject(inputText,
                                                        GeneralFunctions.arrayToJson(
                                                                new ArrayList<String>())));
                                        subjectDatabase.close();
                                        ((MainActivity) getActivity()).displayFragment(
                                                NotesSubjectFragment.newInstance(inputText));
                                    }
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
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
