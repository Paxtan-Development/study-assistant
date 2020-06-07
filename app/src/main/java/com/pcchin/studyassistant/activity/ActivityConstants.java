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

package com.pcchin.studyassistant.activity;

/** Constants used in MainActivity. **/
public final class ActivityConstants {
    private ActivityConstants() {
        throw new IllegalStateException("Utility class");
    }

    // Shared preference constants
    public static final String SHAREDPREF_APP_UPDATE_PATH = "AppUpdatePath";
    public static final String SHAREDPREF_LAST_UPDATE_CHECK = "lastUpdateCheck";
    public static final String SHAREDPREF_FEEDBACK_ISSUE_LIST = "feedbackIssueList";
    public static final String SHAREDPREF_BUG_ISSUE_LIST = "bugIssueList";
    public static final String SHAREDPREF_UID = "uid";

    // General intent constants
    public static final String INTENT_VALUE_DISPLAY_UPDATE = "displayUpdate";
    public static final String INTENT_VALUE_START_FRAGMENT = "startFragment";
    public static final String INTENT_VALUE_NOTE_ID = "noteId";

    // Intent codes
    public static final int SELECT_ZIP_FILE = 300;
    public static final int SELECT_SUBJECT_FILE = 301;

    // Other constants
    public static final String DATABASE_NOTES = "notesSubject";
    public static final String DATABASE_PROJECT = "projectDatabase";
    public static final String LOG_APP_NAME = "StudyAssistant";
    public static final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+" +
            "/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|" +
            "\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)" +
            "+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.)" +
            "{3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b" +
            "\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

    // Permission codes
    static final int EXTERNAL_STORAGE_PERMISSION = 200;
}
