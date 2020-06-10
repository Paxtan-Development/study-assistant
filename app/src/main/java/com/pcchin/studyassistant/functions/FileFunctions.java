/*
 * Copyright 2020 PC Chin. All rights reserved.
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

package com.pcchin.studyassistant.functions;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.activity.ActivityConstants;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import io.sentry.Sentry;

/** Functions used in managing files. **/
public final class FileFunctions {
    private FileFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Generates a .txt file based on a path and its contents. **/
    public static void exportTxt(String path, String contents) {
        // Get permission to read and write files
        try (FileWriter outputNote = new FileWriter(path)) {
            outputNote.write(contents);
            outputNote.flush();
        } catch (IOException e) {
            Log.d(ActivityConstants.LOG_APP_NAME, "File Error: IO Exception occurred when exporting "
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
        int i = 1;
        while (new File(returnFile).exists() && i < Integer.MAX_VALUE) {
            returnFile = filename + "(" + i + ")" + extension;
            i++;
        }
        return returnFile;
    }

    /** For deleting the directory inside list of files and inner Directory.
     * Placed here despite only used once as recursion is needed. **/
    public static boolean deleteDir(@NonNull File dir) {
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

    /** Gets the external download directory of the app.
     * The download directory is assumed to be /storage/emulated/0/Download or /storage/emulated/0/Downloads.
     * If it doesn't exist, fall back to getInternalDownloadDir.
     * The path will always end in '/'. **/
    @NonNull
    public static String getExternalDownloadDir(@NonNull Context context) {
        File downloadDir = new File("/storage/emulated/0/Download");
        File downloadDir2 = new File("/storage/emulated/0/Downloads");
        return downloadDir.exists() && downloadDir.isDirectory() && downloadDir.canWrite()
                ? "/storage/emulated/0/Download/" : downloadDir2.exists()
                && downloadDir2.isDirectory() && downloadDir2.canWrite() ?
                "/storage/emulated/0/Downloads/" : getInternalDownloadDir(context);
    }

    /** Get the internal download directory of the app.
     * Falls back to the root directory if no such download directory could be found.
     * The path will always end in '/'. **/
    @NonNull
    public static String getInternalDownloadDir(@NonNull Context context) {
        File downloadDirFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (downloadDirFile == null) {
            downloadDirFile = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }
        return downloadDirFile == null ? "/storage/emulated/0/" : downloadDirFile.getAbsolutePath() + "/";
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

    /** Gets the number of bytes required of data from a file.
     * A Toast is created when it fails and it returns an empty array. **/
    @NonNull
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static byte[] getBytesFromFile(int byteAmt, @NonNull InputStream stream) {
        byte[] returnByte = new byte[byteAmt];
        try {
            stream.read(returnByte);
            return returnByte;
        } catch (IOException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: byte[] of size " + byteAmt + " could not "
                    + "be retrieved from input stream of file. Stack trace is");
            e.printStackTrace();
            return new byte[0];
        }
    }

    /** Gets all the remaining bytes of data from a file.
     * A Toast is created when it fails and it returns an empty array. **/
    @NonNull
    public static byte[] getRemainingBytesFromFile(@NonNull InputStream stream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            while(stream.available() != 0){
                buffer.write(stream.read());
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Remaining bytes of input stream of file "
                    + stream + " not able to be read. Stack trace is");
            e.printStackTrace();
            return new byte[0];
        }
    }

    /** Returns the absolute path of a path from the given URI.
     * If the Uri is invalid or no such file exists, it would return null.
     * This is done through copying the file to the temp directory and return the temp file. **/
    public static String getRealPathFromUri(Context context, Uri uri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                return null;
            } else {
                String outputFile = generateValidFile(context.getFilesDir().getAbsolutePath()
                        + "/temp/", getFileNameFromUri(context, uri));
                copyFile(inputStream, new File(outputFile));
                return outputFile;
            }
        } catch (IOException e) {
            Log.e(ActivityConstants.LOG_APP_NAME, "File Error: Could not read from URI "
                    + uri.toString() + ". Stack trace is");
            e.printStackTrace();
            Sentry.capture(e);
            return null;
        }
    }

    /** Copies the file from a source to its destination. **/
    public static void copyFile(File source, File destination) throws IOException {
        try (InputStream input = new FileInputStream(source)) {
            copyFile(input, destination);
        }
    }

    /** Copies the file from a given InputStream to its destination.
     * This process would fail with an IOException if the size of the byte array reaches 100MB.
     * This is to prevent the user from importing an extremely large file which may crash the app. **/
    private static void copyFile(@NonNull InputStream input, File destination) throws IOException {
        try (OutputStream output = new FileOutputStream(destination)) {
            long totalLength = 0;
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                output.write(buf, 0, len);
                totalLength += len;
                if (totalLength > (100 * 1000 * 1000)) {
                    throw new IOException("Input size exceeds 100MB");
                }
            }
        }
    }

    /** Gets the file name from a specified URI.
     * Answer from https://stackoverflow.com/a/25005243 **/
    private static String getFileNameFromUri(Context context, @NonNull Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null && uri.getPath() != null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /** Gets the name of the file without its trailing extension.
     * The file name is first reversed, then the first value of the array split by . is taken,
     * then the string is reversed back.
     * If the file name is blank, its extension would be returned. **/
    @NonNull
    public static String getFileName(String fileName) {
        String[] splitString = new StringBuilder(fileName)
                .reverse().toString()
                .split("\\.", 2);
        if (splitString.length > 1) {
            return new StringBuilder(splitString[1]).reverse().toString();
        } else if (splitString.length == 1) {
            return new StringBuilder(splitString[0]).reverse().toString();
        } else {
            return "";
        }
    }
}
