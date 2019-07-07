package com.pcchin.studyassistant.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.project.ProjectSelectFragment;

public class MainFragment extends Fragment implements FragmentOnBackPressed {

    /** Default constructor. **/
    public MainFragment() {}

    /** Initialize fragment. Nothing to see here. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.app_name);
        }
    }

    /** Creates the fragment and sets up the listener for the buttons. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and set up listeners
        View returnView = inflater.inflate(R.layout.fragment_main, container, false);

        returnView.findViewById(R.id.m1_notes).setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
            }
        });

        returnView.findViewById(R.id.m1_projects).setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
            }
        });

        returnView.findViewById(R.id.m1_about).setOnClickListener(v -> {
            if (getActivity() != null) {
                ((MainActivity) getActivity()).displayFragment(new AboutFragment());
            }
        });
        return returnView;
    }

    /** Display the exit dialog when the 'Back' button is pressed. **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            GeneralFunctions.displayExit(getActivity());
        }
        return true;
    }
}
