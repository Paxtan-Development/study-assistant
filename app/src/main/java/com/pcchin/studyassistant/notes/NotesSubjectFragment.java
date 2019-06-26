package com.pcchin.studyassistant.notes;

import android.annotation.SuppressLint;
import androidx.room.Room;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.functions.SortingComparators;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class NotesSubjectFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_SUBJECT = "notesSubject";
    private static final int MAXLINES = 4;

    private static final int[] sortingList = new int[]{NotesSubject.SORT_ALPHABETICAL_ASC,
        NotesSubject.SORT_ALPHABETICAL_DES, NotesSubject.SORT_DATE_ASC, NotesSubject.SORT_DATE_DES};
    private static final int[] sortingTitles = new int[]{R.string.n2_sort_alpha_asc, R.string.n2_sort_alpha_des,
            R.string.n2_sort_date_asc, R.string.n2_sort_date_des};
    private static final int[] sortingImgs = new int[]{R.drawable.ic_sort_atz, R.drawable.ic_sort_zta,
            R.drawable.ic_sort_num_asc, R.drawable.ic_sort_num_des};

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
                                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
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
                NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
                notesArray = GeneralFunctions.jsonToArray(subject.contents);

                if (notesArray != null) {
                    // Sort notes just in case
                    int sortOrder = subject.sortOrder;
                    if (sortOrder == NotesSubject.SORT_ALPHABETICAL_DES) {
                        // Sort by alphabetical order, descending
                        Collections.sort(notesArray, SortingComparators.firstValComparator);
                        Collections.reverse(notesArray);
                    } else if (sortOrder == NotesSubject.SORT_DATE_ASC) {
                        Collections.sort(notesArray, SortingComparators.secondValDateComparator);
                    } else if (sortOrder == NotesSubject.SORT_DATE_DES) {
                        Collections.sort(notesArray, SortingComparators.secondValDateComparator);
                        Collections.reverse(notesArray);
                    } else {
                        // Sort by alphabetical order, ascending
                        if (sortOrder != NotesSubject.SORT_ALPHABETICAL_ASC) {
                            // Default to this if sortOrder is invalid
                            subject.sortOrder = NotesSubject.SORT_ALPHABETICAL_ASC;
                        }
                        Collections.sort(notesArray, SortingComparators.firstValComparator);
                    }
                }
                subjectDatabase.SubjectDao().update(subject);
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
            ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setText(note.get(2));
            ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setMaxLines(MAXLINES);
            miniNote.findViewById(R.id.n2_mini_content).setVerticalScrollBarEnabled(false);

            // Conversion formula: px = sp / dpi + padding between lines
            ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setHeight
                    ((int) (MAXLINES * 18 * getResources().getDisplayMetrics().density) +
                            (int) ((MAXLINES - 1)* 18 * ((TextView) miniNote.findViewById(R.id.n2_mini_content))
                                    .getLineSpacingMultiplier()));
            // Set on click listener
            final int finalI = i;
            View.OnClickListener displayNoteListener = v -> {
                if (getActivity() != null) {
                    subjectDatabase.close();
                    ((MainActivity) getActivity()).displayFragment(NotesViewFragment
                            .newInstance(notesSubject, finalI));
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onNewNotePressed() {
        if (getContext() != null && getActivity() != null) {
            @SuppressLint("InflateParams") final View popupView = getLayoutInflater()
                    .inflate(R.layout.popup_edittext, null);
            AlertDialog newNoteDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n2_new_note)
                    .setView(popupView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(R.string.cancel, null)
                    .create();
            // OnClickListeners implemented separately to prevent
            // dialog from being dismissed after button click
            newNoteDialog.setOnShowListener(dialog -> {
                ((EditText) popupView.findViewById(R.id.popup_input)).setHint(R.string.title);
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
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
                                subjectDatabase.close();
                                ((MainActivity) getActivity()).displayFragment
                                        (NotesEditFragment.newInstance(
                                                notesSubject, popupInputText));
                            }
                        });
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(v -> dialog.dismiss());
            });
            newNoteDialog.show();
        }
    }

    public void onEditPressed() {
        if (getContext() != null) {
            @SuppressLint("InflateParams") final View popupView = getLayoutInflater()
                    .inflate(R.layout.popup_edittext, null);
            AlertDialog editSubjDialog = new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n2_edit_title)
                    .setView(popupView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
            // OnClickListeners implemented separately to prevent
            // dialog from being dismissed after button click
            editSubjDialog.setOnShowListener(dialog -> {
                final String popupInputText = ((EditText) popupView
                        .findViewById(R.id.popup_input))
                        .getText().toString();
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            // Check if input is blank
                            if (popupInputText.replaceAll("\\s+", "")
                                    .length() == 0) {
                                ((TextView) popupView.findViewById(R.id.popup_error))
                                        .setText(R.string.n_error_subject_empty);
                            } else if (subjectDatabase.SubjectDao().search(notesSubject) == null){
                                ((TextView) popupView.findViewById(R.id.popup_error))
                                        .setText(R.string.n2_error_title_taken);
                            } else {
                                // Edit Title
                                dialog.dismiss();
                                NotesSubject subject = subjectDatabase.SubjectDao()
                                        .search(notesSubject);
                                subject.title = popupInputText;
                                subjectDatabase.SubjectDao().update(subject);
                                Toast.makeText(getContext(),
                                        getString(R.string.n2_title_updated),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(view -> dialog.dismiss());
            });
            editSubjDialog.show();
        }
    }

    public void onSortPressed() {
        if (getContext() != null) {
            @SuppressLint("InflateParams") final Spinner sortingSpinner = (Spinner) getLayoutInflater().inflate
                    (R.layout.n2_sorting_spinner, null);

            // Get current order
            sortingSpinner.setAdapter(new NotesSortAdaptor(getContext(), sortingTitles, sortingImgs));
            NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
            int currentOrder = subject.sortOrder;
            for (int i = 0; i < sortingList.length; i++) {
                // Sort spinner to current order
                if (sortingList[i] == currentOrder) {
                    sortingSpinner.setSelection(i);
                }
            }

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.n2_sorting_method)
                    .setView(sortingSpinner)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        // Update value in database
                        NotesSubject subject1 = subjectDatabase.SubjectDao().search(notesSubject);
                        subject1.sortOrder = sortingList[sortingSpinner.getSelectedItemPosition()];
                        subjectDatabase.SubjectDao().update(subject1);
                        dialogInterface.dismiss();
                        if (getFragmentManager() != null) {
                            // Refreshes fragment
                            getFragmentManager().beginTransaction()
                                    .detach(NotesSubjectFragment.this)
                                    .attach(NotesSubjectFragment.this).commit();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                    .create().show();
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
                    .setPositiveButton(R.string.del, (dialog, which) -> {
                        if (getContext() != null && getActivity() != null) {
                            // Delete subject
                            SubjectDatabase database = Room.databaseBuilder(getContext(),
                                    SubjectDatabase.class, "notesSubject")
                                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
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
                            subjectDatabase.close();
                            ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                        }
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            subjectDatabase.close();
            ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
            return true;
        }
        return false;
    }
}
