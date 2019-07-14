/*
 * Copyright 2019 PC Chin. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pcchin.studyassistant.notes;

import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.text.InputType;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.notes.database.NotesSubject;
import com.pcchin.studyassistant.notes.database.NotesSubjectMigration;
import com.pcchin.studyassistant.notes.database.SubjectDatabase;
import com.pcchin.studyassistant.notes.misc.NotesNotifyReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Random;

public class NotesViewFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_SUBJECT = "notesSubject";
    private static final String ARG_ORDER = "notesOrder";

    private ArrayList<String> notesInfo;
    private String notesSubject;
    private int notesOrder;
    private boolean isLocked;
    private boolean hasAlert;

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
            ArrayList<ArrayList<String>> allNotes = database
                    .SubjectDao().search(notesSubject).contents;

            // Check if notesOrder exists
            if (allNotes != null && notesOrder < allNotes.size()) {
                notesInfo = allNotes.get(notesOrder);
                // Error message not shown as it is displayed in NotesSubjectFragment
                FileFunctions.checkNoteIntegrity(notesInfo);
                isLocked = (notesInfo.get(3) != null);
                hasAlert = (notesInfo.get(4) != null);
            } else if (getActivity() != null) {
                // Return to subject
                Toast.makeText(getActivity(), R.string.n_error_corrupt,
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
        String contentText = notesInfo.get(2).replace("\n* ", "\n ● ");
        if (contentText.startsWith("* ")) {
            contentText = contentText.replaceFirst("\\* ", " ● ");
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

        if (!hasAlert) {
            menu.findItem(R.id.n3_notif).setVisible(true);
            menu.findItem(R.id.n3_cancel_notif).setVisible(false);
        } else {
            menu.findItem(R.id.n3_notif).setVisible(false);
            menu.findItem(R.id.n3_cancel_notif).setVisible(true);
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
        String outputText = FileFunctions.generateValidFile("/storage/emulated/0/Download/"
            + notesInfo.get(0), ".txt");
        FileFunctions.exportTxt(outputText, notesInfo.get(2));
        Toast.makeText(getContext(), getString(R.string.n3_note_exported) + outputText,
                Toast.LENGTH_SHORT).show();
    }

    /** Prevents the note from being able to be edited. **/
    public void onLockPressed() {
        if (getContext() != null) {
            @SuppressLint("InflateParams") TextInputLayout inputLayout =
                    (TextInputLayout) getLayoutInflater().inflate(R.layout.popup_edittext, null);
            if (inputLayout.getEditText() != null) {
                inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            inputLayout.setEndIconActivated(true);
            inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            inputLayout.setHintEnabled(true);
            inputLayout.setHint(getString(R.string.n_password_set));
            new AlertDialog.Builder(getContext())
                    .setTitle(getString(R.string.n3_lock_password))
                    .setView(inputLayout)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        // Get values from database
                        String inputText = "";
                        if (inputLayout.getEditText() != null) {
                            inputText = inputLayout.getEditText().getText().toString();
                        }
                        SubjectDatabase database = Room.databaseBuilder(getContext(),
                                SubjectDatabase.class, "notesSubject")
                                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                .allowMainThreadQueries().build();
                        NotesSubject subject = database.SubjectDao().search(notesSubject);
                        ArrayList<ArrayList<String>> contents = subject.contents;

                        // Update values to database
                        if (contents != null && contents.size() > notesOrder) {
                            FileFunctions.checkNoteIntegrity(contents.get(notesOrder));
                            if (inputText.length() == 0) {
                                contents.get(notesOrder).set(3, "");
                            } else {
                                contents.get(notesOrder).set(3, SecurityFunctions.notesHash(inputText));
                            }
                            subject.contents = contents;
                            database.SubjectDao().update(subject);
                            Toast.makeText(getContext(), R.string.n3_note_locked, Toast.LENGTH_SHORT).show();
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
            ArrayList<ArrayList<String>> contents = subject.contents;

            if (contents != null && contents.size() > notesOrder) {
                FileFunctions.checkNoteIntegrity(contents.get(notesOrder));
                if (contents.get(notesOrder).get(3) != null &&
                        contents.get(notesOrder).get(3).length() > 0) {
                    // Set up input layout
                    @SuppressLint("InflateParams") TextInputLayout inputLayout =
                            (TextInputLayout) getLayoutInflater()
                                    .inflate(R.layout.popup_edittext, null);
                    if (inputLayout.getEditText() != null) {
                        inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                                InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                    inputLayout.setEndIconActivated(true);
                    inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
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
                            String inputText = "";
                            if (inputLayout.getEditText() != null) {
                                inputText = inputLayout.getEditText().getText().toString();
                            }
                            if (Objects.equals(SecurityFunctions.notesHash(inputText),
                                    contents.get(notesOrder).get(3))) {
                                // Removes password
                                dialogInterface.dismiss();
                                removeLock(contents, database, subject);
                            } else {
                                // Show error dialog
                                inputLayout.setErrorEnabled(true);
                                inputLayout.setError(getString(R.string.error_password_incorrect));
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

    /** Sets up the alert to notify users at a specific time,
     * separated selectTime(Calendar targetDateTime) for clarity. **/
    public void onAlertPressed() {
        // Select date
        if (getContext() != null) {
            Toast.makeText(getContext(), R.string.n3_set_date, Toast.LENGTH_SHORT).show();
            Calendar targetDateTime = Calendar.getInstance();
            Calendar currentCalendar = Calendar.getInstance();
            DatePickerDialog dateDialog = new DatePickerDialog(getContext(), (datePicker, i, i1, i2) -> {
                targetDateTime.set(Calendar.YEAR, i);
                targetDateTime.set(Calendar.MONTH, i1);
                targetDateTime.set(Calendar.DAY_OF_MONTH, i2);
                selectTime(targetDateTime);
            }, currentCalendar.get(Calendar.YEAR), currentCalendar.get(Calendar.MONTH),
                    currentCalendar.get(Calendar.DAY_OF_MONTH));
            // Set minimum date so that alert cannot be created for the past
            // -10000 as minimum time cannot be now/in the future
            dateDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 10000);
            dateDialog.show();
        }
    }

    /** Cancels the existing alert. **/
    public void onCancelAlertPressed() {
        if (getContext() != null) {
            AlarmManager manager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
            PendingIntent cancelIntent = getNotifyReceiverIntent(Integer.valueOf(notesInfo.get(5)));

            if (manager != null) {
                manager.cancel(cancelIntent);
                // Update values in database
                if (getActivity() != null) {
                    notesInfo.set(4, null);
                    notesInfo.set(5, null);
                    SubjectDatabase database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                            "notesSubject").allowMainThreadQueries()
                            .addMigrations(NotesSubjectMigration.MIGRATION_1_2).build();
                    NotesSubject subject = database.SubjectDao().search(notesSubject);
                    if (subject != null) {
                        ArrayList<ArrayList<String>> updateArray = subject.contents;
                        updateArray.set(notesOrder, notesInfo);
                        database.SubjectDao().update(subject);
                    }
                    database.close();
                    getActivity().invalidateOptionsMenu();
                }
                hasAlert = false;
                Toast.makeText(getContext(), R.string.n3_alert_cancelled, Toast.LENGTH_SHORT).show();

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
                        if (getActivity() != null) {
                            // Delete listener
                            AlarmManager manager = (AlarmManager) getActivity()
                                    .getSystemService(Context.ALARM_SERVICE);
                            if (manager != null && notesInfo.get(5) != null) {
                                PendingIntent alertIntent = getNotifyReceiverIntent(
                                        Integer.valueOf(notesInfo.get(5)));
                                manager.cancel(alertIntent);
                            }

                            SubjectDatabase database = Room.databaseBuilder(getContext(),
                                SubjectDatabase.class, "notesSubject")
                                .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                .allowMainThreadQueries().build();
                            NotesSubject currentSubject = database.SubjectDao().search(notesSubject);
                            if (notesSubject != null) {
                                // Check if contents is valid
                                ArrayList<ArrayList<String>> contents = currentSubject.contents;
                                if (contents != null) {
                                    if (notesOrder < contents.size()) {
                                        contents.remove(notesOrder);
                                    }
                                } else {
                                    contents = new ArrayList<>();
                                }
                                // Update value in database
                                currentSubject.contents = contents;
                                database.SubjectDao().update(currentSubject);
                                database.close();
                                ((MainActivity) getActivity()).displayFragment
                                        (NotesSubjectFragment.newInstance(notesSubject));
                            } else {
                                // In case the note somehow doesn't have a subject
                                ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                            }
                            Toast.makeText(getContext(), R.string.n3_deleted, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
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

    /** Removes the lock for the note and refreshes the menu.  **/
    private void removeLock(@NonNull ArrayList<ArrayList<String>> contents,
                            @NonNull SubjectDatabase database,
                            @NonNull NotesSubject subject) {
        if (getActivity() != null) {
            contents.get(notesOrder).set(3, null);
            subject.contents = contents;
            database.SubjectDao().update(subject);
            database.close();
            Toast.makeText(getContext(), R.string.n3_note_unlocked, Toast.LENGTH_SHORT).show();
            isLocked = false;
            getActivity().invalidateOptionsMenu();
        }
    }

    /** Allows the user to choose the time of the alert,
     * separated from onAlertPressed() for clarity. **/
    private void selectTime(Calendar targetDateTime) {
        Toast.makeText(getContext(), R.string.n3_set_time, Toast.LENGTH_SHORT).show();
        TimePickerDialog timeDialog = new TimePickerDialog(getContext(), (timePicker, i, i1) -> {
            // Get time from time picker
            targetDateTime.set(Calendar.HOUR_OF_DAY, i);
            targetDateTime.set(Calendar.MINUTE, i1);
            targetDateTime.set(Calendar.SECOND, 0);

            if (getActivity() != null) {
                // Set alert
                int requestCode = new Random().nextInt();
                AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                PendingIntent alarmIntent = getNotifyReceiverIntent(requestCode);

                // Save value to database
                notesInfo.set(4, GeneralFunctions.standardDateTimeFormat.format(targetDateTime.getTime()));
                notesInfo.set(5, String.valueOf(requestCode));
                SubjectDatabase database = Room.databaseBuilder(getActivity(), SubjectDatabase.class,
                        "notesSubject")
                        .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                        .allowMainThreadQueries().build();
                NotesSubject subject = database.SubjectDao().search(notesSubject);
                if (subject != null) {
                    ArrayList<ArrayList<String>> updateArray = subject.contents;
                    updateArray.set(notesOrder, notesInfo);
                    database.SubjectDao().update(subject);
                }
                database.close();

                // Insert alarm and reset menu
                if (manager != null) {
                    manager.setWindow(AlarmManager.RTC, targetDateTime.getTimeInMillis(), 10000, alarmIntent);
                    hasAlert = true;
                    Toast.makeText(getContext(), R.string.n3_alert_set, Toast.LENGTH_SHORT).show();
                }
                getActivity().invalidateOptionsMenu();
            }
        }, Calendar.getInstance().get(Calendar.HOUR), Calendar.getInstance().get(Calendar.MINUTE),
                DateFormat.is24HourFormat(getContext()));
        timeDialog.show();
    }

    /** An intent that passes on the information of the note to the notification receiver.
     * @see NotesNotifyReceiver **/
    private PendingIntent getNotifyReceiverIntent(int requestCode) {
        Intent intent = new Intent(getActivity(), NotesNotifyReceiver.class);
        intent.putExtra("title", notesInfo.get(0));
        intent.putExtra("message", notesInfo.get(2));
        return PendingIntent.getBroadcast(getActivity(), requestCode, intent, 0);
    }
}
