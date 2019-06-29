package com.pcchin.studyassistant.notes;

import androidx.room.Room;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class NotesViewFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_SUBJECT = "notesSubject";
    private static final String ARG_ORDER = "notesOrder";

    private ArrayList<String> notesInfo;
    private String notesSubject;
    private int notesOrder;
    private boolean isLocked;

    /** Default constructor. **/
    public NotesViewFragment() {}

    /** Used when viewing a note.
     * @param subject is the title of the subject.
     * @param order is the order of the note in the notes list of the subject. **/
    public static NotesViewFragment newInstance(String subject, int order) {
        NotesViewFragment fragment = new NotesViewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putInt(ARG_ORDER, order);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Gets the contents of the notes from the database. **/
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
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
            ArrayList<ArrayList<String>> allNotes = GeneralFunctions
                    .jsonToArray(database.SubjectDao().search(notesSubject).contents);

            // Check if notesOrder exists
            if (allNotes != null && notesOrder < allNotes.size()) {
                notesInfo = allNotes.get(notesOrder);
                // Error message not shown as it is displayed in NotesSubjectFragment
                checkNoteIntegrity(notesInfo);
                isLocked = (notesInfo.get(3) != null);
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

    /** Creates the fragment. The height of the content is updated based on the screen size. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView returnView = (ScrollView) inflater.inflate(
                R.layout.fragment_notes_view, container, false);
        ((TextView) returnView.findViewById(R.id.n3_title)).setText(notesInfo.get(0));
        String contentText = notesInfo.get(2).replace("\n* ", "\n● ");
        if (contentText.startsWith("* ")) {
            contentText = contentText.replaceFirst("\\* ", "● ");
        }
        ((TextView) returnView.findViewById(R.id.n3_text)).setText(contentText);
        ((TextView) returnView.findViewById(R.id.n3_last_edited)).setText(String.format("%s%s",
                                                                          getString(R.string.n_last_edited), notesInfo.get(1)));
        // Set title
        if (getActivity() != null) {
            getActivity().setTitle(notesSubject);
        }

        // Set min height corresponding to screen height
        if (getActivity() != null) {
            Point endPt = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(endPt);

            // Height is set by Total height - bottom of last edited - navigation header height
            returnView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    returnView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ((TextView) returnView.findViewById(R.id.n3_text)).setMinHeight(endPt.y
                            - returnView.findViewById(R.id.n3_last_edited).getBottom()
                            - (int) getResources().getDimension(R.dimen.nav_header_height));
                }
            });
        }
        return returnView;
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        if (isLocked) {
            inflater.inflate(R.menu.menu_n3_locked, menu);
        } else {
            inflater.inflate(R.menu.menu_n3_unlocked, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Edits the note.
     * @see NotesEditFragment **/
    public void onEditPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(NotesEditFragment
                    .newInstance(notesSubject, notesOrder));
        }
    }

    /** Exports the note to a txt file. **/
    public void onExportPressed() {
        // TODO: Export
    }

    /** Prevents the note from being able to be edited. **/
    public void onLockPressed() {
        if (getContext() != null) {
            @SuppressLint("InflateParams") LinearLayout inputLayout =
                    (LinearLayout) getLayoutInflater().inflate(R.layout.popup_edittext, null);
            ((EditText) inputLayout.findViewById(R.id.popup_input))
                    .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            ((TextView) inputLayout.findViewById(R.id.popup_error))
                    .setTextColor(getResources().getColor(android.R.color.black));
            ((TextView) inputLayout.findViewById(R.id.popup_error)).setText(R.string.n3_password_set);
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.n3_lock_password))
                    .setView(inputLayout)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        // Get values from database
                        String inputText = ((EditText) inputLayout.findViewById(R.id.popup_input))
                                .getText().toString();
                        SubjectDatabase database = Room.databaseBuilder(getContext(),
                                SubjectDatabase.class, "notesSubject")
                                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                .allowMainThreadQueries().build();
                        NotesSubject subject = database.SubjectDao().search(notesSubject);
                        ArrayList<ArrayList<String>> contents = GeneralFunctions
                                .jsonToArray(subject.contents);

                        // Update values to database
                        if (contents != null && contents.size() > notesOrder) {
                            checkNoteIntegrity(contents.get(notesOrder));
                            if (inputText.length() == 0) {
                                contents.get(notesOrder).set(3, "");
                            } else {
                                contents.get(notesOrder).set(3, SecurityFunctions.notesHash(inputText));
                            }
                            subject.contents = GeneralFunctions.arrayToJson(contents);
                            database.SubjectDao().update(subject);
                            Toast.makeText(getContext(), getString(R.string.n3_note_locked), Toast.LENGTH_SHORT).show();
                        }
                        database.close();
                        isLocked = true;
                        if (getActivity() != null) {
                            getActivity().invalidateOptionsMenu();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialogInterface, i) ->
                            dialogInterface.dismiss())
                    .create().show();
        }
    }

    /** Unlocks the note. If there is no password, the note will be unlocked immediately.
     * Or else, a popup will display asking the user to enter the password. **/
    public void onUnlockPressed() {
        if (getContext() != null) {
            SubjectDatabase database = Room.databaseBuilder(getContext(),
                    SubjectDatabase.class, "notesSubject")
                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                    .allowMainThreadQueries().build();
            NotesSubject subject = database.SubjectDao().search(notesSubject);
            ArrayList<ArrayList<String>> contents = GeneralFunctions
                    .jsonToArray(subject.contents);

            if (contents != null && contents.size() > notesOrder) {
                checkNoteIntegrity(contents.get(notesOrder));
                if (contents.get(notesOrder).get(3) != null &&
                        contents.get(notesOrder).get(3).length() > 0) {
                    // Set up input layout
                    @SuppressLint("InflateParams") LinearLayout inputLayout =
                            (LinearLayout) getLayoutInflater()
                                    .inflate(R.layout.popup_edittext, null);
                    ((EditText) inputLayout.findViewById(R.id.popup_input))
                            .setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    // Asks user for password
                    AlertDialog passwordDialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.n3_unlock_password)
                            .setView(inputLayout)
                            .setPositiveButton(android.R.string.ok, null)
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
                    // OnClickListeners implemented separately to prevent dialog from
                    // being dismissed after pressed
                    passwordDialog.setOnShowListener(dialogInterface -> {
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE)
                                .setOnClickListener(view -> {
                            String inputText = ((EditText) inputLayout.findViewById(R.id.popup_input))
                                    .getText().toString();
                            if (Objects.equals(SecurityFunctions.notesHash(inputText),
                                    contents.get(notesOrder).get(3))) {
                                // Removes password
                                dialogInterface.dismiss();
                                removeLock(contents, database, subject);
                            } else {
                                // Show error dialog
                                ((TextView) inputLayout.findViewById(R.id.popup_error))
                                        .setText(R.string.n3_password_incorrect);
                            }
                        });
                        ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(
                                view -> dialogInterface.dismiss());
                    });
                    passwordDialog.show();
                } else {
                    // Unlocks immediately
                    removeLock(contents, database, subject);
                }
            }
        }
    }

    /** Deletes the note from the subject. **/
    public void onDeletePressed() {
        if (getContext() != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.del)
                    .setMessage(R.string.n3_del_confirm)
                    .setPositiveButton(R.string.del, (dialog, which) -> {
                        if (getContext() != null && getActivity() != null) {
                            SubjectDatabase database = Room.databaseBuilder(getContext(),
                                SubjectDatabase.class, "notesSubject")
                                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
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
                    })
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    /** Returns to
     * @see NotesSubjectFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                    .newInstance(notesSubject, notesOrder));
            return true;
        }
        return false;
    }

    /** Checks the integrity of the note. **/
    private static void checkNoteIntegrity(@NonNull ArrayList<String> original) {
        while (original.size() < 3) {
            original.add("");
        }
        if (original.size() == 3) {
            original.add(null);
        }
    }

    /** Removes the lock for the note and refreshes the menu.  **/
    private void removeLock(@NonNull ArrayList<ArrayList<String>> contents,
                            @NonNull SubjectDatabase database,
                            @NonNull NotesSubject subject) {
        if (getActivity() != null) {
            contents.get(notesOrder).set(3, null);
            subject.contents = GeneralFunctions.arrayToJson(contents);
            database.SubjectDao().update(subject);
            database.close();
            Toast.makeText(getContext(), getString(R.string.n3_note_unlocked),
                    Toast.LENGTH_SHORT).show();
            isLocked = false;
            getActivity().invalidateOptionsMenu();
        }
    }
}
