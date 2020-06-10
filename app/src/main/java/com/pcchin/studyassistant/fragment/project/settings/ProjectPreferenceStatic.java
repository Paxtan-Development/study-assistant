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

package com.pcchin.studyassistant.fragment.project.settings;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.pcchin.dtpreference.DatePreference;
import com.pcchin.dtpreference.dialog.DatePreferenceDialog;
import com.pcchin.studyassistant.preference.PreferenceString;

import java.util.Calendar;

/** Static functions that are used in
 * @see ProjectSettingsFragment **/
final class ProjectPreferenceStatic {
    private ProjectPreferenceStatic() {
        throw new IllegalStateException("Utility class");
    }


    /** Gets the dialog fragments for date preferences. **/
    static DatePreferenceDialog getDatePreferenceDialog(ProjectSettingsFragment fragment,
                                                        @NonNull DatePreference preference) {
        long currentDate = Calendar.getInstance().getTimeInMillis();
        switch (preference.getKey()) {
            case PreferenceString.PREF_SET_EXPECTED_START:
            case PreferenceString.PREF_UPDATE_EXPECTED_START:
                return changeExpectedStart(fragment, preference);
            case PreferenceString.PREF_SET_EXPECTED_END:
            case PreferenceString.PREF_UPDATE_EXPECTED_END:
                return changeExpectedEnd(fragment, preference);
            case PreferenceString.PREF_SET_ACTUAL_START:
            case PreferenceString.PREF_UPDATE_ACTUAL_START:
                return changeActualStart(fragment, preference, currentDate);
            case PreferenceString.PREF_SET_ACTUAL_END:
            case PreferenceString.PREF_UPDATE_ACTUAL_END:
                return changeActualEnd(fragment, preference, currentDate);
        }
        return null;
    }

    /** Returns a dialog that changes the expected start date. **/
    private static DatePreferenceDialog changeExpectedStart(@NonNull ProjectSettingsFragment fragment,
                                                            Preference preference) {
        // Expected start date must be before expected end date
        if (fragment.project.expectedEndDate == null) {
            return DatePreferenceDialog.newInstance(preference.getKey());
        } else {
            return DatePreferenceDialog.newInstance(preference.getKey(),
                    -1, fragment.project.expectedEndDate.getTime())
                    .setInitialDate(fragment.project.expectedStartDate.getTime());
        }
    }

    /** Returns a dialog that changes the expected end date. **/
    private static DatePreferenceDialog changeExpectedEnd(@NonNull ProjectSettingsFragment fragment,
                                                          Preference preference) {
        // Expected end date must be after expected start date
        if (fragment.project.expectedStartDate == null) {
            return DatePreferenceDialog.newInstance(preference.getKey());
        } else {
            return DatePreferenceDialog.newInstance(preference.getKey(),
                    fragment.project.expectedStartDate.getTime(), -1)
                    .setInitialDate(fragment.project.expectedEndDate.getTime());
        }
    }

    /** Returns a dialog that changes the actual start date. **/
    private static DatePreferenceDialog changeActualStart(@NonNull ProjectSettingsFragment fragment,
                                                          Preference preference, long currentDate) {
        // Actual start date must be in the past and before the actual end date
        if (fragment.project.actualEndDate == null) {
            return DatePreferenceDialog.newInstance(preference.getKey(), -1, currentDate);
        } else {
            return DatePreferenceDialog.newInstance(preference.getKey(), -1, fragment.project.actualEndDate.getTime())
                    .setInitialDate(fragment.project.actualEndDate.getTime());
        }
    }

    /** Returns a dialog that changes the actual end date. **/
    private static DatePreferenceDialog changeActualEnd(@NonNull ProjectSettingsFragment fragment,
                                                        Preference preference, long currentDate) {
        // Actual end date must be in the past and after the actual start date
        if (fragment.project.actualStartDate == null) {
            return DatePreferenceDialog.newInstance(preference.getKey(), -1, currentDate);
        } else {
            return DatePreferenceDialog.newInstance(preference.getKey(),
                    fragment.project.actualStartDate.getTime(), currentDate)
                    .setInitialDate(fragment.project.actualStartDate.getTime());
        }
    }
}
