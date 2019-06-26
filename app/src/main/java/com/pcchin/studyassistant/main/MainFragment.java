package com.pcchin.studyassistant.main;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.notes.NotesSelectFragment;
import com.pcchin.studyassistant.project.ProjectSelectFragment;

public class MainFragment extends Fragment implements FragmentOnBackPressed {

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.app_name);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment and set up listeners
        View returnView = inflater.inflate(R.layout.fragment_main, container, false);

        returnView.findViewById(R.id.m1_notes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                }
            }
        });

        returnView.findViewById(R.id.m1_projects).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).displayFragment(new ProjectSelectFragment());
                }
            }
        });

        returnView.findViewById(R.id.m1_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    ((MainActivity) getActivity()).displayFragment(new AboutFragment());
                }
            }
        });
        return returnView;
    }

    @Override
    public boolean onBackPressed() {
        GeneralFunctions.displayExit((MainActivity) getActivity());
        return true;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
