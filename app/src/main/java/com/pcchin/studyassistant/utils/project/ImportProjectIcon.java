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

package com.pcchin.studyassistant.utils.project;

import com.pcchin.studyassistant.ui.MainActivity;

/** A class used to import the image icon for a specific project.
 * As this class extends Thread, it should be used as such as well. **/
public class ImportProjectIcon extends Thread {
    private MainActivity activity;

    /** The constructor for the class as activity needs to be passed on. **/
    public ImportProjectIcon(MainActivity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {

    }
}
