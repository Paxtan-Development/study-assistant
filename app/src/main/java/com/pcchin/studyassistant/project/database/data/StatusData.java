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

package com.pcchin.studyassistant.project.database.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

/** The entity for each status. **/
@Entity
public class StatusData {
    /** The ID for each status, serves as a unique key and is randomly generated. **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_statusID")
    public String statusID = "";

    /** The date in which the status is published. **/
    public Date publishedDate;

    /** The title of the status. **/
    public String statusTitle;

    /** The type of the status. **/
    public String statusType;

    /** The content of the status. **/
    public String statusContent;

    /** The project that the status was published under. **/
    public String parentProject;

    /** The member that published the status. **/
    public String memberID;

    /** The colour used in the side icon of the status. **/
    public int statusColor;

    StatusData() {

    }
}
