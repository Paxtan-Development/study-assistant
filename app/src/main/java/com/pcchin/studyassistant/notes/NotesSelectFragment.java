package com.pcchin.studyassistant.notes;

import android.annotation.SuppressLint;
import androidx.room.Room;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.main.MainFragment;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.List;

public class NotesSelectFragment extends Fragment implements FragmentOnBackPressed {
    private SubjectDatabase subjectDatabase;

    /** Default constructor. **/
    public NotesSelectFragment() {}

    /** Initializes the fragment and the database. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getContext() != null && getActivity() != null) {
            subjectDatabase = Room.databaseBuilder(getContext(), SubjectDatabase.class, "notesSubject")
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
            getActivity().setTitle(R.string.app_name);
        }
        setHasOptionsMenu(true);
    }

    /** Creates the fragment. Add existing subjects to the fragment. **/
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
            subjectBtn.setOnClickListener(v -> {
                // Go to notesSubjectFragment
                if (getActivity() != null) {
                    subjectDatabase.close();
                    ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment.newInstance(
                            subjectList.get(finalI).title
                    ));
                }
            });
            ((LinearLayout) returnView.findViewById(R.id.n1_notes_list)).addView(subjectBtn, i);
        }
        return returnView;
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n1, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Creates a new subject. **/
    public void onNewSubjectPressed() {
        if (getContext() != null && getActivity() != null) {
            SubjectDatabase subjectDatabase = Room.databaseBuilder(getContext(),
                                    SubjectDatabase.class, "notesSubject")
                                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                    .allowMainThreadQueries().build();
            GeneralFunctions.showNewSubject(getContext(), ((MainActivity) getActivity()), subjectDatabase);
        }
    }

    /** Imports an existing zip file. **/
    public void onImportPressed() {
        // TODO: Import
    }

    /** Returns to
     * @see MainFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(new MainFragment());
            return true;
        }
        return false;
    }
}
