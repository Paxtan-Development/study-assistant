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

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.fragment.project.ProjectSelectFragment;

/** Functions used for processing data throughout the app. **/
public final class DataFunctions {
    private DataFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Function that is called if the project appears to be missing. **/
    public static void onProjectMissing(MainActivity activity, @NonNull ProjectDatabase projectDatabase) {
        // Go back to project selection
        Toast.makeText(activity, R.string.p_error_project_not_found, Toast.LENGTH_SHORT).show();
        projectDatabase.close();
        activity.displayFragment(new ProjectSelectFragment());
    }
}
