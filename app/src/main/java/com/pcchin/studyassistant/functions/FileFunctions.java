package com.pcchin.studyassistant.functions;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Functions used in managing files. **/
public class FileFunctions {
    /** The function used to import subjects, either via a ZIP file or a .subject file. **/
    public static void importSubject() {
        // TODO: Pick file using intent
    }

    /** Generates a .txt file based on a path and its contents. **/
    public static void exportTxt(String path, String contents) {
        try {
            FileWriter outputNote = new FileWriter(path);
            outputNote.write(contents);
            outputNote.flush();
            outputNote.close();
        } catch (IOException e) {
            Log.d("StudyAssistant", "File Error: IO Exception occurred when exporting "
                    + "note with path , stack trace is");
            e.printStackTrace();
        }
    }

    /** Generates a valid file in the required directory.
     * If a file with the same name exists,
     * a file with incrementing number will be added to the file.
     * @param extension needs to include the . at the front.**/
    public static String generateValidFile(String filename, String extension) {
        String returnFile = filename + extension;
        int i = 0;
        while (new File(returnFile).exists()) {
            returnFile = filename + "(" + i + ")" + extension;

        }
        return returnFile;
    }

    /** For deleting the directory inside list of files and inner Directory.
     * Placed here despite only used once as self calling is needed. **/
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /** @return a string of text from specific text files in the assets folder **/
    @SuppressWarnings("SameParameterValue")
    @NonNull
    public static String getTxt(@NonNull Context context, String textFileName) {
        String text;
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(textFileName);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((text = bufferedReader.readLine()) != null) {
                stringBuilder.append(text);
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

}
