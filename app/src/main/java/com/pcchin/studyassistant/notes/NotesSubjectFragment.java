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

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import com.pcchin.studyassistant.functions.FileFunctions;
import com.pcchin.studyassistant.misc.AutoDismissDialog;
import com.pcchin.studyassistant.misc.FragmentOnBackPressed;
import com.pcchin.studyassistant.functions.GeneralFunctions;
import com.pcchin.studyassistant.functions.SecurityFunctions;
import com.pcchin.studyassistant.main.MainActivity;
import com.pcchin.studyassistant.misc.SortingComparators;
import com.pcchin.studyassistant.database.notes.NotesSubjectMigration;
import com.pcchin.studyassistant.database.notes.NotesSubject;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;
import com.pcchin.studyassistant.notes.misc.NotesNotifyReceiver;
import com.pcchin.studyassistant.notes.misc.NotesSortAdaptor;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class NotesSubjectFragment extends Fragment implements FragmentOnBackPressed {
    private static final String ARG_SUBJECT = "noteSubject";
    private static final String ARG_PREV = "previousOrder";
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
    private int previousOrder;

    /** Default constructor. **/
    public NotesSubjectFragment() {}

    /** Used in all except when returning from a NotesViewFragment.
     * @param subject is the subject that is displayed. **/
    public static NotesSubjectFragment newInstance(String subject) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        fragment.setArguments(args);
        return fragment;
    }

    /** Used when returning from a NotesViewFragment.
     * @param subject is the subject that is displayed.
     * @param previousOrder is the order of the note that was shown. **/
    static NotesSubjectFragment newInstance(String subject, int previousOrder) {
        NotesSubjectFragment fragment = new NotesSubjectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT, subject);
        args.putInt(ARG_PREV, previousOrder);
        fragment.setArguments(args);
        return fragment;
    }

    /** Initializes the fragment. Retrieves all of the notes of the subject from the notes. **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            subjectDatabase = Room.databaseBuilder(getActivity(),
                                    SubjectDatabase.class, MainActivity.DATABASE_NOTES)
                                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                    .allowMainThreadQueries().build();

            // Get basic info & set title
            if (getArguments() != null) {
                notesSubject = getArguments().getString(ARG_SUBJECT);
                previousOrder = getArguments().getInt(ARG_PREV);
            }

            // Check if subject exists in notes
            NotesSubject currentSubject = subjectDatabase.SubjectDao().search(notesSubject);
            if (currentSubject == null) {
                Toast.makeText(getContext(), R.string.n2_error_missing_subject,
                        Toast.LENGTH_SHORT).show();
                // Return to NotesSelectFragment if not
                subjectDatabase.close();
                ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());

            } else {
                // Get notes from notes
                getActivity().setTitle(notesSubject);
                NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
                notesArray = subject.contents;
                if (notesArray != null) {
                    // Sort notes just in case
                    sortNotes(subject);

                    // Check for any note that is expired
                    Calendar storedDate;
                    Date tempDate;
                    for (ArrayList<String> note: notesArray) {
                        FileFunctions.checkNoteIntegrity(note);
                        storedDate = Calendar.getInstance();
                        if (note.get(4) != null) {
                            try {
                                tempDate = ConverterFunctions.standardDateTimeFormat.parse(note.get(4));
                                if (tempDate != null) {
                                    storedDate.setTime(tempDate);
                                    // Delete alert if time passed
                                    if (storedDate.before(Calendar.getInstance())) {
                                        note.set(4, null);
                                        note.set(5, null);
                                    }
                                }
                            } catch (ParseException e) {
                                Log.w(MainActivity.LOG_APP_NAME, "Parse Error: Failed to parse date "
                                        + note.get(4) + " as standard date time.");
                            }
                        }
                    }
                }
                subjectDatabase.SubjectDao().update(subject);
            }
        }

        setHasOptionsMenu(true);
    }

    /** Creates the fragment.
     * Display each note and center the note that was previously selected if needed. **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView returnScroll = (ScrollView) inflater.inflate(R.layout.blank_list,
                container, false);
        LinearLayout returnView = returnScroll.findViewById(R.id.blank_linear);

        // Check if stored data may be corrupt
        if (notesArray == null) {
            Toast.makeText(getContext(), R.string.n_error_corrupt, Toast.LENGTH_SHORT).show();
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
            // Implemented separately for backwards compatibility
            if (note.size() < 6) {
                note.add(null);
            }

            // Add note to view
            @SuppressLint("InflateParams") LinearLayout miniNote = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.n2_notes_mini, null);
            ((TextView) miniNote.findViewById(R.id.n2_mini_title)).setText(note.get(0));
            ((TextView) miniNote.findViewById(R.id.n2_mini_date)).setText(String.format("%s%s",
                    getString(R.string.n_last_edited), note.get(1)));
            String miniText = note.get(2).replace("\n* ", "\n ● ");
            if (miniText.startsWith("* ")) {
                miniText = miniText.replaceFirst("\\* ", " ● ");
            }
            ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setText(miniText);
            ((TextView) miniNote.findViewById(R.id.n2_mini_content)).setMaxLines(MAXLINES);

            // Check if note is locked
            if (note.get(3) == null) {
                miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.GONE);
            } else {
                miniNote.findViewById(R.id.n2_mini_lock).setVisibility(View.VISIBLE);
            }

            // Check if note has a alert attached
            if (note.get(4) == null) {
                miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.GONE);
            } else {
                miniNote.findViewById(R.id.n2_mini_notif).setVisibility(View.VISIBLE);
            }
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

            if (previousOrder == i) {
                // Scroll to last seen view
                returnScroll.post(() -> returnScroll.scrollTo(0, miniNote.getTop()));
            }
        }

        if (anyCorrupt) {
            Toast.makeText(getContext(), R.string.n2_error_some_corrupt, Toast.LENGTH_SHORT).show();
        }
        return returnScroll;
    }

    /** Sets up the menu for the fragment. **/
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_n2, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    /** Creates a new note with a given title. **/
    public void onNewNotePressed() {
        if (getActivity() != null && getFragmentManager() != null) {
            @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout)
                    getLayoutInflater().inflate(R.layout.popup_edittext, null);
            // End icon has been set in XML file
            DialogInterface.OnShowListener nListener = dialog -> {
                popupView.setHint(getString(R.string.title));
                ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String popupInputText = "";
                            if (popupView.getEditText() != null) {
                                popupInputText = popupView.getEditText().getText().toString();
                            }

                            // Check if input is blank
                            if (popupInputText.replaceAll("\\s+", "")
                                    .length() == 0) {
                                popupView.setErrorEnabled(true);
                                popupView.setError(getString(R.string.n2_error_note_title_empty));
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
            };
            new AutoDismissDialog(getString(R.string.n2_new_note), popupView,
                    new String[]{getString(android.R.string.ok),
                            getString(android.R.string.cancel), ""}, nListener)
                    .show(getFragmentManager(), "NotesSubjectFragment.1");
        }
    }

    /** Change the method which the notes are sorted. **/
    public void onSortPressed() {
        if (getContext() != null && getFragmentManager() != null) {
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
            new AutoDismissDialog(getString(R.string.n2_sorting_method), sortingSpinner,
                    new String[]{getString(android.R.string.ok),
                            getString(android.R.string.cancel), ""},
                    new DialogInterface.OnClickListener[]{(dialogInterface, i) -> {
                        // Update value in notes
                        NotesSubject subject1 = subjectDatabase.SubjectDao().search(notesSubject);
                        subject1.sortOrder = sortingList[sortingSpinner.getSelectedItemPosition()];
                        subjectDatabase.SubjectDao().update(subject1);
                        dialogInterface.dismiss();
                        sortNotes(subject1);
                        GeneralFunctions.reloadFragment(this);
                    }, (dialogInterface, i) -> dialogInterface.dismiss(), null})
                    .show(getFragmentManager(), "NotesSubjectFragment.2");
        }
    }

    /** Renames the subject to another one. **/
    public void onRenamePressed() {
        if (getActivity() != null && getFragmentManager() != null) {
            @SuppressLint("InflateParams") final TextInputLayout popupView = (TextInputLayout)
                    getLayoutInflater().inflate(R.layout.popup_edittext, null);
            // End icon has been set in XML file
            if (popupView.getEditText() != null) {
                popupView.getEditText().setText(notesSubject);
            }
            DialogInterface.OnShowListener nListener = dialogInterface -> {
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            String popupInputText = popupView.getEditText().getText().toString();
                            // Check if input is blank
                            if (popupInputText.replaceAll("\\s+", "")
                                    .length() == 0) {
                                popupView.setErrorEnabled(true);
                                popupView.setError(getString(R.string.n_error_subject_empty));
                            } else if (subjectDatabase.SubjectDao().search(popupInputText) != null) {
                                popupView.setErrorEnabled(true);
                                popupView.setError(getString(R.string.error_subject_exists));
                            } else {
                                // Move subject
                                dialogInterface.dismiss();
                                NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
                                NotesSubject newSubject = new NotesSubject(popupInputText,
                                        subject.contents, subject.sortOrder);
                                subjectDatabase.SubjectDao().insert(newSubject);
                                subjectDatabase.SubjectDao().delete(subject);
                                subjectDatabase.close();

                                // Display new subject
                                Toast.makeText(getActivity(), R.string.n2_subject_renamed,
                                        Toast.LENGTH_SHORT).show();
                                GeneralFunctions.updateNavView((MainActivity) getActivity());
                                ((MainActivity) getActivity()).displayFragment(NotesSubjectFragment
                                        .newInstance(popupInputText));
                            }
                        });
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(view -> dialogInterface.dismiss());
            };
            new AutoDismissDialog(getString(R.string.rename_subject), popupView,
                    new String[]{getString(R.string.rename),
                            getString(android.R.string.cancel), ""}, nListener)
                    .show(getFragmentManager(), "NotesSubjectFragment.3");
        }
    }

    /** Export all the notes of the subject into a ZIP file,
     * askZipPassword() and exportSubject() separated for clarity. **/
    public void onExportPressed() {
        if (getContext() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat
                .checkSelfPermission(getContext(), Manifest.permission
                        .WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), R.string
                    .error_write_permission_denied, Toast.LENGTH_SHORT).show();
        } else if (getFragmentManager() != null) {
            new AutoDismissDialog(getString(R.string.n2_export_format),
                    R.array.n_import_subject_format, (dialogInterface, i) ->
                    new Handler().post(() -> {
                        if (i == 0) {
                            askZipPassword();
                        } else {
                            exportSubject();
                        }
                    }), new String[]{"", getString(android.R.string.cancel), ""},
                    new DialogInterface.OnClickListener[]{null, null, null})
                    .show(getFragmentManager(), "NotesSubjectFragment.4");
        }
    }

    /** Asks users whether to export the ZIP file with a password.
     * Separated from onExportPressed() for clarity,
     * exportZip() separated for clarity. **/
    private void askZipPassword() {
        if (getFragmentManager() != null) {
            @SuppressLint("InflateParams") TextInputLayout inputLayout = (TextInputLayout)
                    getLayoutInflater().inflate(R.layout.popup_edittext, null);
            if (inputLayout.getEditText() != null) {
                inputLayout.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            inputLayout.setEndIconActivated(true);
            inputLayout.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            inputLayout.setHintEnabled(true);
            inputLayout.setHint(getString(R.string.n_password_set));

            DialogInterface.OnShowListener passwordListener = dialogInterface -> {
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                    String inputText = "";
                    if (inputLayout.getEditText() != null) {
                        inputText = inputLayout.getEditText().getText().toString();
                    }
                    if (inputText.length() == 0 || inputText.length() >= 8) {
                        exportZip(inputText);
                    } else {
                        inputLayout.setErrorEnabled(true);
                        inputLayout.setError(getString(R.string.error_password_short));
                    }
                    dialogInterface.dismiss();
                });
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE)
                        .setOnClickListener(view -> dialogInterface.dismiss());
            };
            new AutoDismissDialog(getString(R.string.enter_password), inputLayout, new String[]{
                    getString(android.R.string.ok), getString(android.R.string.cancel), ""},
                    passwordListener).show(getFragmentManager(), "NotesSubjectFragment.5");
        }
    }

    /** Export the subject to a ZIP file.
     * Separated from onExportPressed() for clarity. **/
    private void exportZip(String password) {
        if (getContext() != null) {
            // Generate valid paths for temp storage folder
            String tempExportFolder = FileFunctions.generateValidFile(getContext()
                    .getFilesDir().getAbsolutePath() + "/tempZip", "");
            try {
                // Creates ZIP file
                String exportFilePath = FileFunctions.generateValidFile(
                        "/storage/emulated/0/Download/" + notesSubject, ".zip");
                ZipFile exportFile;
                if (password.length() >= 8) {
                    exportFile = new ZipFile(exportFilePath, password.toCharArray());
                } else {
                    exportFile = new ZipFile(exportFilePath);
                }

                if (new File(tempExportFolder).mkdir()) {
                    ArrayList<File> exportFilesList = new ArrayList<>();

                    // Export all the note's data to a text .subj file
                    try {
                        String infoTempOutputPath = FileFunctions.generateValidFile(
                                tempExportFolder + notesSubject, ".subj");
                        FileWriter infoTempOutput = new FileWriter(infoTempOutputPath);
                        NotesSubject subject = subjectDatabase.SubjectDao().search(notesSubject);
                        infoTempOutput.write(notesSubject + "\n" + subject.sortOrder + "\n");

                        for (int i = 0; i < notesArray.size(); i++) {
                            // Export the note to the output folder
                            String currentPath = FileFunctions.generateValidFile(
                                    tempExportFolder + "/" + notesArray.get(i).get(0),
                                    ".txt");
                            FileFunctions.exportTxt(currentPath, notesArray.get(i).get(2));
                            exportFilesList.add(new File(currentPath));

                            // Record info about the note to the .subj file
                            FileFunctions.checkNoteIntegrity(notesArray.get(i));
                            for (int j = 0; j < notesArray.get(i).size(); j++) {
                                // Convert null to "NULL" string for them to be understood when read
                                if (notesArray.get(i).get(j) == null) {
                                    notesArray.get(i).set(j, "NULL");
                                }
                            }
                            infoTempOutput.write(new File(currentPath).getName()
                                    + "\n" + notesArray.get(i).get(0) + "\n"
                                    + notesArray.get(i).get(3) + "\n" + notesArray.get(i).get(4) + "\n"
                                    + notesArray.get(i).get(5) + "\n");
                        }
                        infoTempOutput.flush();
                        infoTempOutput.close();

                        // Rename temp info output path to .subj file
                        exportFilesList.add(new File(infoTempOutputPath));
                    } catch (IOException e) {
                        Log.w(MainActivity.LOG_APP_NAME, "File Error: Writing subject " + notesSubject
                                + " failed. Stack trace is");
                        e.printStackTrace();
                    }

                    if (password.length() >= 8) {
                        // Encrypts ZIP file with password
                        ZipParameters passwordParams = new ZipParameters();
                        passwordParams.setEncryptFiles(true);
                        passwordParams.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
                        exportFile.addFiles(exportFilesList, passwordParams);
                    } else if (exportFilesList.size() > 0) {
                        // Adds files to ZIP file
                        exportFile.addFiles(exportFilesList);
                    }

                    // Delete temp folder
                    if (!FileFunctions.deleteDir(new File(tempExportFolder))) {
                        Log.w(MainActivity.LOG_APP_NAME, "File Error: Temporary folder "
                                + tempExportFolder + " could not be deleted.");
                    }
                    Toast.makeText(getContext(), getString(R.string.subject_exported)
                            + exportFilePath, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(MainActivity.LOG_APP_NAME, "File Error: Folder " + tempExportFolder
                            + " cannot be created.");
                    Toast.makeText(getContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
                }
            } catch (ZipException e) {
                Log.e(MainActivity.LOG_APP_NAME, "File error: ZIP processing error occurred while " +
                        "exporting a subject. Stack trace is ");
                e.printStackTrace();
                Toast.makeText(getContext(), R.string.file_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /** Export the subject as a password-protected byte[] as a .subject file,
     * separated from onExportSubject() for clarity. **/
    private void exportSubject() {
        if (getFragmentManager() != null) {
            @SuppressLint("InflateParams") TextInputLayout inputText = (TextInputLayout) getLayoutInflater()
                    .inflate(R.layout.popup_edittext, null);
            if (inputText.getEditText() != null) {
                inputText.getEditText().setInputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            inputText.setEndIconActivated(true);
            inputText.setEndIconMode(TextInputLayout.END_ICON_PASSWORD_TOGGLE);
            inputText.setHintEnabled(true);
            inputText.setHint(getString(R.string.n_password_set));
            DialogInterface.OnShowListener exportListener = dialogInterface -> {
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    // Check if password is too short, must be 8 characters in length
                    String responseText = "";
                    if (inputText.getEditText() != null) {
                        responseText = inputText.getEditText().getText().toString();
                    }
                    if (responseText.length() == 0 || responseText.length() >= 8) {
                        // Set output file name
                        String outputFileName = "/storage/emulated/0/Download/" + notesSubject
                                + ".subject";
                        int count = 0;
                        while (new File(outputFileName).exists()) {
                            outputFileName = "/storage/emulated/0/Download/" + notesSubject
                                    + "(" + count + ").subject";
                        }

                        // Check if the file can be created
                        String finalOutputFileName = outputFileName;
                        String finalResponseText = responseText;
                        dialogInterface.dismiss();
                        String finalResponseText1 = responseText;
                        new Handler().post(() -> {
                            try {
                                // Get permission to read and write files
                                File outputFile = new File(finalOutputFileName);
                                if (outputFile.createNewFile()) {
                                    Toast.makeText(getContext(), R.string.n2_exporting_subject,
                                            Toast.LENGTH_SHORT).show();

                                    // Export the file
                                    // The length of the title is exported first, followed by the title.
                                    // Then, the subject's sort order is listed
                                    // and the encrypted contents are stored.
                                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                                    outputStream.write(ConverterFunctions.intToBytes(notesSubject
                                            .getBytes().length));
                                    outputStream.write(notesSubject.getBytes());
                                    outputStream.write(ConverterFunctions
                                            .intToBytes(subjectDatabase.SubjectDao()
                                                    .search(notesSubject).sortOrder));
                                    if (finalResponseText1.length() >= 8) {
                                        outputStream.write(1);
                                        outputStream.write(SecurityFunctions.subjectEncrypt(notesSubject,
                                                finalResponseText, notesArray));
                                    } else {
                                        outputStream.write(0);
                                        outputStream.write(ConverterFunctions
                                                .doubleArrayToJson(notesArray).getBytes());
                                    }
                                    outputStream.flush();
                                    outputStream.close();
                                    Toast.makeText(getContext(), getString(R.string.subject_exported)
                                            + outputFile, Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(MainActivity.LOG_APP_NAME, "File Error: File "
                                            + finalOutputFileName + " cannot be created.");
                                    Toast.makeText(getContext(), R.string.n2_error_file_not_created,
                                            Toast.LENGTH_SHORT).show();
                                }
                            } catch (FileNotFoundException e) {
                                Log.e(MainActivity.LOG_APP_NAME, "File Error: File "
                                        + finalOutputFileName + " not found, stack trace is");
                                e.printStackTrace();
                                dialogInterface.dismiss();
                            } catch (IOException e) {
                                Log.e(MainActivity.LOG_APP_NAME, "File Error: An IO Exception"
                                        + " occurred on file " + finalOutputFileName + ", stack trace is");
                                e.printStackTrace();
                                dialogInterface.dismiss();
                            }
                        });
                    } else {
                        inputText.setErrorEnabled(true);
                        inputText.setError(getString(R.string.error_password_short));
                    }
                });
                ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view ->
                        dialogInterface.dismiss());
            };
            new AutoDismissDialog(getString(R.string.n2_password_export), inputText,
                    new String[]{getString(android.R.string.ok),
                            getString(android.R.string.cancel), ""}, exportListener)
                    .show(getFragmentManager(), "NotesSubjectFragment.6");
        }
    }

    /** Deletes the current subject and returns to
     * @see NotesSelectFragment **/
    public void onDeletePressed() {
        if (getFragmentManager() != null) {
            new AutoDismissDialog(getString(R.string.del), getString(R.string.n2_del_confirm),
                    new String[]{getString(R.string.del),
                            getString(android.R.string.cancel), ""},
                    new DialogInterface.OnClickListener[]{(dialog, which) -> {
                        if (getActivity() != null) {
                            // Delete phantom alerts
                            for (ArrayList<String> note: notesArray) {
                                AlarmManager manager = (AlarmManager) getActivity()
                                        .getSystemService(Context.ALARM_SERVICE);
                                if (manager != null && note.size() >= 6 && note.get(5) != null
                                        && note.get(0) != null && note.get(2) != null) {
                                    // Get PendingIntent for note alert
                                    Intent intent = new Intent(getActivity(), NotesNotifyReceiver.class);
                                    intent.putExtra(MainActivity.INTENT_VALUE_TITLE, note.get(0));
                                    intent.putExtra(MainActivity.INTENT_VALUE_MESSAGE, note.get(2));
                                    intent.putExtra(MainActivity.INTENT_VALUE_REQUEST_CODE, note.get(5));
                                    PendingIntent alertIntent = PendingIntent.getBroadcast(
                                            getActivity(), Integer.valueOf(note.get(5)), intent, 0);

                                    manager.cancel(alertIntent);
                                }
                            }

                            // Delete subject
                            SubjectDatabase database = Room.databaseBuilder(getActivity(),
                                    SubjectDatabase.class, MainActivity.DATABASE_NOTES)
                                    .addMigrations(NotesSubjectMigration.MIGRATION_1_2)
                                    .allowMainThreadQueries().build();
                            NotesSubject delTarget = database.SubjectDao().search(notesSubject);
                            if (delTarget != null) {
                                database.SubjectDao().delete(delTarget);
                            }
                            database.close();
                            // Return to NotesSelectFragment
                            Toast.makeText(getContext(), R.string.n2_deleted, Toast.LENGTH_SHORT).show();
                            GeneralFunctions.updateNavView((MainActivity) getActivity());
                            subjectDatabase.close();
                            ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
                        }
                    }, (dialog, which) -> dialog.dismiss(), null})
                    .show(getFragmentManager(), "NotesSubjectFragment.7");
        }
    }

    /** Returns to
     * @see NotesSelectFragment **/
    @Override
    public boolean onBackPressed() {
        if (getActivity() != null) {
            subjectDatabase.close();
            ((MainActivity) getActivity()).displayFragment(new NotesSelectFragment());
            return true;
        }
        return false;
    }

    /** Sort the notes based on the sorting format given.
     * @see NotesSubject
     * @see SortingComparators **/
    private void sortNotes(@NonNull NotesSubject subject) {
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
        subject.contents = notesArray;
        subjectDatabase.SubjectDao().update(subject);
    }
}
