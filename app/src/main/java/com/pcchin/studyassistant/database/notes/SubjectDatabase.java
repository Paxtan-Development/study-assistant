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

package com.pcchin.studyassistant.database.notes;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.pcchin.studyassistant.functions.ConverterFunctions;

/** The notes layer for the subjects. **/
@Database(entities = {NotesSubject.class}, version = 2)
@TypeConverters({ConverterFunctions.class})
public abstract class SubjectDatabase extends RoomDatabase {
    /** References the SQL requests in the interface. **/
    public abstract SubjectDao SubjectDao();
}
