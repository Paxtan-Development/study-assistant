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

package com.pcchin.studyassistant.preference;

/** Stores all the string preferences used in the app. **/
public final class PreferenceString {
    // String literals for menus
    public static final String PREF_MENU_ROOT = "pref_menu_root",
            PREF_MENU_GENERAL = "pref_menu_general",
            PREF_MENU_FEATURES = "pref_menu_features",
            PREF_MENU_DATE = "pref_menu_date",
            PREF_MENU_SECURITY = "pref_menu_security";

    // String literals for general preferences
    public static final String PREF_UPDATE_TITLE = "pref_update_title",
            PREF_UPDATE_DESC = "pref_update_desc",
            PREF_SET_ICON = "pref_set_icon",
            PREF_UPDATE_ICON = "pref_update_icon",
            PREF_REMOVE_ICON = "pref_remove_icon",
            PREF_COMPLETED = "pref_completed";

    // String literals for feature preferences
    public static final String PREF_MEMBERS = "pref_members",
            PREF_ROLES = "pref_roles",
            PREF_TASKS = "pref_tasks",
            PREF_STATUS = "pref_status",
            PREF_MERGE_TASK_STATUS = "pref_merge_task_status",
            PREF_STATUS_ICON = "pref_status_icon",
            PREF_RELATED_SUBJECT = "pref_related_subject";

    // String literals for date preferences
    public static final String PREF_SET_EXPECTED_START = "pref_set_expected_start",
            PREF_UPDATE_EXPECTED_START = "pref_update_expected_start",
            PREF_REMOVE_EXPECTED_START = "pref_remove_expected_start",
            PREF_SET_EXPECTED_END = "pref_set_expected_end",
            PREF_UPDATE_EXPECTED_END = "pref_update_expected_end",
            PREF_REMOVE_EXPECTED_END = "pref_remove_expected_end",
            PREF_SET_ACTUAL_START = "pref_set_actual_start",
            PREF_UPDATE_ACTUAL_START = "pref_update_actual_start",
            PREF_REMOVE_ACTUAL_START = "pref_remove_actual_start",
            PREF_SET_ACTUAL_END = "pref_set_actual_end",
            PREF_UPDATE_ACTUAL_END = "pref_update_actual_end",
            PREF_REMOVE_ACTUAL_END = "pref_remove_actual_end";

    // String literals for security preferences
    public static final String PREF_SET_PW = "pref_set_password",
            PREF_UPDATE_PW = "pref_update_password",
            PREF_REMOVE_PW = "pref_remove_password",
            PREF_DELETE_PROJECT = "pref_del_project";
}
