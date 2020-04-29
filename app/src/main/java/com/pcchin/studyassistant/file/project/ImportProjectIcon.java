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

package com.pcchin.studyassistant.file.project;

import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.R;
import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.activity.MainActivity;
import com.pcchin.studyassistant.database.project.ProjectDatabase;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.functions.GeneralFunctions;

/** A class used to import the image icon for a specific project.
 * As this class extends Thread, it should be used as such as well. **/
public class ImportProjectIcon extends Thread {
    private ProjectDatabase database;
    private ProjectData project;
    private String id2;
    private boolean isMember;
    private MainActivity activity;

    /** The constructor for the class as activity needs to be passed on. **/
    public ImportProjectIcon(MainActivity activity, @NonNull Intent data) {
        this.activity = activity;
        this.database = GeneralFunctions.getProjectDatabase(activity);
        this.project = database.ProjectDao().searchByID(data.getStringExtra(ActivityConstants.INTENT_PROJECT_ID));
        this.id2 = data.getStringExtra(ActivityConstants.INTENT_ID2);
        this.isMember = data.getBooleanExtra(ActivityConstants.INTENT_IS_MEMBER, false);
    }

    @Override
    public void run() {
        // TODO: Complete
        // Copy the path of the file to the directory
        Toast.makeText(activity, R.string.p3_general_icon_updated, Toast.LENGTH_SHORT).show();
    }
}
