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

package com.pcchin.studyassistant.database.project.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/** The entity for each member. **/
@Entity
public class MemberData {
    /** The ID for each user, serves as a unique key and is randomly generated **/
    @PrimaryKey
    @NonNull
    @ColumnInfo(name="_memberID")
    public String memberID = "";

    /** The project ID which contains the user. **/
    public String parentProject;

    /** The username of the member, if needed. **/
    public String username;

    /** The full name of the member. **/
    public String fullName;

    /** The salt for the member used specifically to protect the password. **/
    public String salt;

    /** A hashed password, if needed, used by members to access the project. **/
    public String memberPass;

    /** The role for each user in the project. **/
    public String role;

    /** Default constructor. **/
    @Ignore
    public MemberData() {

    }

    /** Constructor used when creating a new account. **/
    public MemberData(@NonNull String memberID, String parentProject, String username, String fullName,
                      String salt, String memberPass, String role) {
        this.memberID = memberID;
        this.parentProject = parentProject;
        this.username = username;
        this.fullName = fullName;
        this.salt = salt;
        this.memberPass = memberPass;
        this.role = role;
    }
}
