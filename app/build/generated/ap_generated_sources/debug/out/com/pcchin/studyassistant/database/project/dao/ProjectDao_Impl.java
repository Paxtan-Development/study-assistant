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

package com.pcchin.studyassistant.database.project.dao;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pcchin.studyassistant.database.project.data.ProjectData;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class ProjectDao_Impl implements ProjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProjectData> __insertionAdapterOfProjectData;

  private final EntityDeletionOrUpdateAdapter<ProjectData> __deletionAdapterOfProjectData;

  private final EntityDeletionOrUpdateAdapter<ProjectData> __updateAdapterOfProjectData;

  public ProjectDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProjectData = new EntityInsertionAdapter<ProjectData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `ProjectData` (`_projectID`,`salt`,`projectTitle`,`projectPass`,`description`,`hasIcon`,`projectStatusIcon`,`expectedStartDate`,`expectedEndDate`,`actualStartDate`,`actualEndDate`,`projectProtected`,`memberSignupEnabled`,`memberDefaultRole`,`membersEnabled`,`rolesEnabled`,`taskEnabled`,`statusEnabled`,`mergeTaskStatus`,`displayedInfo`,`associatedSubject`,`projectOngoing`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ProjectData value) {
        if (value.projectID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.projectID);
        }
        if (value.salt == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.salt);
        }
        if (value.projectTitle == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.projectTitle);
        }
        if (value.projectPass == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.projectPass);
        }
        if (value.description == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.description);
        }
        final int _tmp;
        _tmp = value.hasIcon ? 1 : 0;
        stmt.bindLong(6, _tmp);
        stmt.bindLong(7, value.projectStatusIcon);
        final String _tmp_1;
        _tmp_1 = ConverterFunctions.dateToString(value.expectedStartDate);
        if (_tmp_1 == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, _tmp_1);
        }
        final String _tmp_2;
        _tmp_2 = ConverterFunctions.dateToString(value.expectedEndDate);
        if (_tmp_2 == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, _tmp_2);
        }
        final String _tmp_3;
        _tmp_3 = ConverterFunctions.dateToString(value.actualStartDate);
        if (_tmp_3 == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, _tmp_3);
        }
        final String _tmp_4;
        _tmp_4 = ConverterFunctions.dateToString(value.actualEndDate);
        if (_tmp_4 == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, _tmp_4);
        }
        final int _tmp_5;
        _tmp_5 = value.projectProtected ? 1 : 0;
        stmt.bindLong(12, _tmp_5);
        final int _tmp_6;
        _tmp_6 = value.memberSignupEnabled ? 1 : 0;
        stmt.bindLong(13, _tmp_6);
        if (value.memberDefaultRole == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.memberDefaultRole);
        }
        final int _tmp_7;
        _tmp_7 = value.membersEnabled ? 1 : 0;
        stmt.bindLong(15, _tmp_7);
        final int _tmp_8;
        _tmp_8 = value.rolesEnabled ? 1 : 0;
        stmt.bindLong(16, _tmp_8);
        final int _tmp_9;
        _tmp_9 = value.taskEnabled ? 1 : 0;
        stmt.bindLong(17, _tmp_9);
        final int _tmp_10;
        _tmp_10 = value.statusEnabled ? 1 : 0;
        stmt.bindLong(18, _tmp_10);
        final int _tmp_11;
        _tmp_11 = value.mergeTaskStatus ? 1 : 0;
        stmt.bindLong(19, _tmp_11);
        stmt.bindLong(20, value.displayedInfo);
        if (value.associatedSubject == null) {
          stmt.bindNull(21);
        } else {
          stmt.bindString(21, value.associatedSubject);
        }
        final int _tmp_12;
        _tmp_12 = value.projectOngoing ? 1 : 0;
        stmt.bindLong(22, _tmp_12);
      }
    };
    this.__deletionAdapterOfProjectData = new EntityDeletionOrUpdateAdapter<ProjectData>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `ProjectData` WHERE `_projectID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ProjectData value) {
        if (value.projectID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.projectID);
        }
      }
    };
    this.__updateAdapterOfProjectData = new EntityDeletionOrUpdateAdapter<ProjectData>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `ProjectData` SET `_projectID` = ?,`salt` = ?,`projectTitle` = ?,`projectPass` = ?,`description` = ?,`hasIcon` = ?,`projectStatusIcon` = ?,`expectedStartDate` = ?,`expectedEndDate` = ?,`actualStartDate` = ?,`actualEndDate` = ?,`projectProtected` = ?,`memberSignupEnabled` = ?,`memberDefaultRole` = ?,`membersEnabled` = ?,`rolesEnabled` = ?,`taskEnabled` = ?,`statusEnabled` = ?,`mergeTaskStatus` = ?,`displayedInfo` = ?,`associatedSubject` = ?,`projectOngoing` = ? WHERE `_projectID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, ProjectData value) {
        if (value.projectID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.projectID);
        }
        if (value.salt == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.salt);
        }
        if (value.projectTitle == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.projectTitle);
        }
        if (value.projectPass == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.projectPass);
        }
        if (value.description == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.description);
        }
        final int _tmp;
        _tmp = value.hasIcon ? 1 : 0;
        stmt.bindLong(6, _tmp);
        stmt.bindLong(7, value.projectStatusIcon);
        final String _tmp_1;
        _tmp_1 = ConverterFunctions.dateToString(value.expectedStartDate);
        if (_tmp_1 == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, _tmp_1);
        }
        final String _tmp_2;
        _tmp_2 = ConverterFunctions.dateToString(value.expectedEndDate);
        if (_tmp_2 == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, _tmp_2);
        }
        final String _tmp_3;
        _tmp_3 = ConverterFunctions.dateToString(value.actualStartDate);
        if (_tmp_3 == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, _tmp_3);
        }
        final String _tmp_4;
        _tmp_4 = ConverterFunctions.dateToString(value.actualEndDate);
        if (_tmp_4 == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, _tmp_4);
        }
        final int _tmp_5;
        _tmp_5 = value.projectProtected ? 1 : 0;
        stmt.bindLong(12, _tmp_5);
        final int _tmp_6;
        _tmp_6 = value.memberSignupEnabled ? 1 : 0;
        stmt.bindLong(13, _tmp_6);
        if (value.memberDefaultRole == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.memberDefaultRole);
        }
        final int _tmp_7;
        _tmp_7 = value.membersEnabled ? 1 : 0;
        stmt.bindLong(15, _tmp_7);
        final int _tmp_8;
        _tmp_8 = value.rolesEnabled ? 1 : 0;
        stmt.bindLong(16, _tmp_8);
        final int _tmp_9;
        _tmp_9 = value.taskEnabled ? 1 : 0;
        stmt.bindLong(17, _tmp_9);
        final int _tmp_10;
        _tmp_10 = value.statusEnabled ? 1 : 0;
        stmt.bindLong(18, _tmp_10);
        final int _tmp_11;
        _tmp_11 = value.mergeTaskStatus ? 1 : 0;
        stmt.bindLong(19, _tmp_11);
        stmt.bindLong(20, value.displayedInfo);
        if (value.associatedSubject == null) {
          stmt.bindNull(21);
        } else {
          stmt.bindString(21, value.associatedSubject);
        }
        final int _tmp_12;
        _tmp_12 = value.projectOngoing ? 1 : 0;
        stmt.bindLong(22, _tmp_12);
        if (value.projectID == null) {
          stmt.bindNull(23);
        } else {
          stmt.bindString(23, value.projectID);
        }
      }
    };
  }

  @Override
  public void insert(final ProjectData project) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfProjectData.insert(project);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final ProjectData project) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfProjectData.handle(project);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final ProjectData project) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfProjectData.handle(project);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public ProjectData searchByID(final String id) {
    final String _sql = "SELECT * FROM projectData WHERE _projectID = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (id == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, id);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProjectID = CursorUtil.getColumnIndexOrThrow(_cursor, "_projectID");
      final int _cursorIndexOfSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "salt");
      final int _cursorIndexOfProjectTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "projectTitle");
      final int _cursorIndexOfProjectPass = CursorUtil.getColumnIndexOrThrow(_cursor, "projectPass");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfHasIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "hasIcon");
      final int _cursorIndexOfProjectStatusIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "projectStatusIcon");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfProjectProtected = CursorUtil.getColumnIndexOrThrow(_cursor, "projectProtected");
      final int _cursorIndexOfMemberSignupEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "memberSignupEnabled");
      final int _cursorIndexOfMemberDefaultRole = CursorUtil.getColumnIndexOrThrow(_cursor, "memberDefaultRole");
      final int _cursorIndexOfMembersEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "membersEnabled");
      final int _cursorIndexOfRolesEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "rolesEnabled");
      final int _cursorIndexOfTaskEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "taskEnabled");
      final int _cursorIndexOfStatusEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "statusEnabled");
      final int _cursorIndexOfMergeTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "mergeTaskStatus");
      final int _cursorIndexOfDisplayedInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "displayedInfo");
      final int _cursorIndexOfAssociatedSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedSubject");
      final int _cursorIndexOfProjectOngoing = CursorUtil.getColumnIndexOrThrow(_cursor, "projectOngoing");
      final ProjectData _result;
      if(_cursor.moveToFirst()) {
        _result = new ProjectData();
        _result.projectID = _cursor.getString(_cursorIndexOfProjectID);
        _result.salt = _cursor.getString(_cursorIndexOfSalt);
        _result.projectTitle = _cursor.getString(_cursorIndexOfProjectTitle);
        _result.projectPass = _cursor.getString(_cursorIndexOfProjectPass);
        _result.description = _cursor.getString(_cursorIndexOfDescription);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfHasIcon);
        _result.hasIcon = _tmp != 0;
        _result.projectStatusIcon = _cursor.getInt(_cursorIndexOfProjectStatusIcon);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _result.expectedStartDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _result.expectedEndDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualStartDate);
        _result.actualStartDate = ConverterFunctions.stringToDate(_tmp_3);
        final String _tmp_4;
        _tmp_4 = _cursor.getString(_cursorIndexOfActualEndDate);
        _result.actualEndDate = ConverterFunctions.stringToDate(_tmp_4);
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfProjectProtected);
        _result.projectProtected = _tmp_5 != 0;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfMemberSignupEnabled);
        _result.memberSignupEnabled = _tmp_6 != 0;
        _result.memberDefaultRole = _cursor.getString(_cursorIndexOfMemberDefaultRole);
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfMembersEnabled);
        _result.membersEnabled = _tmp_7 != 0;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfRolesEnabled);
        _result.rolesEnabled = _tmp_8 != 0;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfTaskEnabled);
        _result.taskEnabled = _tmp_9 != 0;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfStatusEnabled);
        _result.statusEnabled = _tmp_10 != 0;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfMergeTaskStatus);
        _result.mergeTaskStatus = _tmp_11 != 0;
        _result.displayedInfo = _cursor.getInt(_cursorIndexOfDisplayedInfo);
        _result.associatedSubject = _cursor.getString(_cursorIndexOfAssociatedSubject);
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfProjectOngoing);
        _result.projectOngoing = _tmp_12 != 0;
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ProjectData> searchByTitle(final String title) {
    final String _sql = "SELECT * FROM projectData WHERE projectTitle = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (title == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, title);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProjectID = CursorUtil.getColumnIndexOrThrow(_cursor, "_projectID");
      final int _cursorIndexOfSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "salt");
      final int _cursorIndexOfProjectTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "projectTitle");
      final int _cursorIndexOfProjectPass = CursorUtil.getColumnIndexOrThrow(_cursor, "projectPass");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfHasIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "hasIcon");
      final int _cursorIndexOfProjectStatusIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "projectStatusIcon");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfProjectProtected = CursorUtil.getColumnIndexOrThrow(_cursor, "projectProtected");
      final int _cursorIndexOfMemberSignupEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "memberSignupEnabled");
      final int _cursorIndexOfMemberDefaultRole = CursorUtil.getColumnIndexOrThrow(_cursor, "memberDefaultRole");
      final int _cursorIndexOfMembersEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "membersEnabled");
      final int _cursorIndexOfRolesEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "rolesEnabled");
      final int _cursorIndexOfTaskEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "taskEnabled");
      final int _cursorIndexOfStatusEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "statusEnabled");
      final int _cursorIndexOfMergeTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "mergeTaskStatus");
      final int _cursorIndexOfDisplayedInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "displayedInfo");
      final int _cursorIndexOfAssociatedSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedSubject");
      final int _cursorIndexOfProjectOngoing = CursorUtil.getColumnIndexOrThrow(_cursor, "projectOngoing");
      final List<ProjectData> _result = new ArrayList<ProjectData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final ProjectData _item;
        _item = new ProjectData();
        _item.projectID = _cursor.getString(_cursorIndexOfProjectID);
        _item.salt = _cursor.getString(_cursorIndexOfSalt);
        _item.projectTitle = _cursor.getString(_cursorIndexOfProjectTitle);
        _item.projectPass = _cursor.getString(_cursorIndexOfProjectPass);
        _item.description = _cursor.getString(_cursorIndexOfDescription);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfHasIcon);
        _item.hasIcon = _tmp != 0;
        _item.projectStatusIcon = _cursor.getInt(_cursorIndexOfProjectStatusIcon);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_3);
        final String _tmp_4;
        _tmp_4 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_4);
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfProjectProtected);
        _item.projectProtected = _tmp_5 != 0;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfMemberSignupEnabled);
        _item.memberSignupEnabled = _tmp_6 != 0;
        _item.memberDefaultRole = _cursor.getString(_cursorIndexOfMemberDefaultRole);
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfMembersEnabled);
        _item.membersEnabled = _tmp_7 != 0;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfRolesEnabled);
        _item.rolesEnabled = _tmp_8 != 0;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfTaskEnabled);
        _item.taskEnabled = _tmp_9 != 0;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfStatusEnabled);
        _item.statusEnabled = _tmp_10 != 0;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfMergeTaskStatus);
        _item.mergeTaskStatus = _tmp_11 != 0;
        _item.displayedInfo = _cursor.getInt(_cursorIndexOfDisplayedInfo);
        _item.associatedSubject = _cursor.getString(_cursorIndexOfAssociatedSubject);
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfProjectOngoing);
        _item.projectOngoing = _tmp_12 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ProjectData> searchBySubject(final String subject) {
    final String _sql = "SELECT * FROM projectData WHERE associatedSubject = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (subject == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, subject);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProjectID = CursorUtil.getColumnIndexOrThrow(_cursor, "_projectID");
      final int _cursorIndexOfSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "salt");
      final int _cursorIndexOfProjectTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "projectTitle");
      final int _cursorIndexOfProjectPass = CursorUtil.getColumnIndexOrThrow(_cursor, "projectPass");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfHasIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "hasIcon");
      final int _cursorIndexOfProjectStatusIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "projectStatusIcon");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfProjectProtected = CursorUtil.getColumnIndexOrThrow(_cursor, "projectProtected");
      final int _cursorIndexOfMemberSignupEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "memberSignupEnabled");
      final int _cursorIndexOfMemberDefaultRole = CursorUtil.getColumnIndexOrThrow(_cursor, "memberDefaultRole");
      final int _cursorIndexOfMembersEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "membersEnabled");
      final int _cursorIndexOfRolesEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "rolesEnabled");
      final int _cursorIndexOfTaskEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "taskEnabled");
      final int _cursorIndexOfStatusEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "statusEnabled");
      final int _cursorIndexOfMergeTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "mergeTaskStatus");
      final int _cursorIndexOfDisplayedInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "displayedInfo");
      final int _cursorIndexOfAssociatedSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedSubject");
      final int _cursorIndexOfProjectOngoing = CursorUtil.getColumnIndexOrThrow(_cursor, "projectOngoing");
      final List<ProjectData> _result = new ArrayList<ProjectData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final ProjectData _item;
        _item = new ProjectData();
        _item.projectID = _cursor.getString(_cursorIndexOfProjectID);
        _item.salt = _cursor.getString(_cursorIndexOfSalt);
        _item.projectTitle = _cursor.getString(_cursorIndexOfProjectTitle);
        _item.projectPass = _cursor.getString(_cursorIndexOfProjectPass);
        _item.description = _cursor.getString(_cursorIndexOfDescription);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfHasIcon);
        _item.hasIcon = _tmp != 0;
        _item.projectStatusIcon = _cursor.getInt(_cursorIndexOfProjectStatusIcon);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_3);
        final String _tmp_4;
        _tmp_4 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_4);
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfProjectProtected);
        _item.projectProtected = _tmp_5 != 0;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfMemberSignupEnabled);
        _item.memberSignupEnabled = _tmp_6 != 0;
        _item.memberDefaultRole = _cursor.getString(_cursorIndexOfMemberDefaultRole);
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfMembersEnabled);
        _item.membersEnabled = _tmp_7 != 0;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfRolesEnabled);
        _item.rolesEnabled = _tmp_8 != 0;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfTaskEnabled);
        _item.taskEnabled = _tmp_9 != 0;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfStatusEnabled);
        _item.statusEnabled = _tmp_10 != 0;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfMergeTaskStatus);
        _item.mergeTaskStatus = _tmp_11 != 0;
        _item.displayedInfo = _cursor.getInt(_cursorIndexOfDisplayedInfo);
        _item.associatedSubject = _cursor.getString(_cursorIndexOfAssociatedSubject);
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfProjectOngoing);
        _item.projectOngoing = _tmp_12 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ProjectData> getAllProjects() {
    final String _sql = "SELECT * FROM projectData";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfProjectID = CursorUtil.getColumnIndexOrThrow(_cursor, "_projectID");
      final int _cursorIndexOfSalt = CursorUtil.getColumnIndexOrThrow(_cursor, "salt");
      final int _cursorIndexOfProjectTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "projectTitle");
      final int _cursorIndexOfProjectPass = CursorUtil.getColumnIndexOrThrow(_cursor, "projectPass");
      final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
      final int _cursorIndexOfHasIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "hasIcon");
      final int _cursorIndexOfProjectStatusIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "projectStatusIcon");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfProjectProtected = CursorUtil.getColumnIndexOrThrow(_cursor, "projectProtected");
      final int _cursorIndexOfMemberSignupEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "memberSignupEnabled");
      final int _cursorIndexOfMemberDefaultRole = CursorUtil.getColumnIndexOrThrow(_cursor, "memberDefaultRole");
      final int _cursorIndexOfMembersEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "membersEnabled");
      final int _cursorIndexOfRolesEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "rolesEnabled");
      final int _cursorIndexOfTaskEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "taskEnabled");
      final int _cursorIndexOfStatusEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "statusEnabled");
      final int _cursorIndexOfMergeTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "mergeTaskStatus");
      final int _cursorIndexOfDisplayedInfo = CursorUtil.getColumnIndexOrThrow(_cursor, "displayedInfo");
      final int _cursorIndexOfAssociatedSubject = CursorUtil.getColumnIndexOrThrow(_cursor, "associatedSubject");
      final int _cursorIndexOfProjectOngoing = CursorUtil.getColumnIndexOrThrow(_cursor, "projectOngoing");
      final List<ProjectData> _result = new ArrayList<ProjectData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final ProjectData _item;
        _item = new ProjectData();
        _item.projectID = _cursor.getString(_cursorIndexOfProjectID);
        _item.salt = _cursor.getString(_cursorIndexOfSalt);
        _item.projectTitle = _cursor.getString(_cursorIndexOfProjectTitle);
        _item.projectPass = _cursor.getString(_cursorIndexOfProjectPass);
        _item.description = _cursor.getString(_cursorIndexOfDescription);
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfHasIcon);
        _item.hasIcon = _tmp != 0;
        _item.projectStatusIcon = _cursor.getInt(_cursorIndexOfProjectStatusIcon);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_3);
        final String _tmp_4;
        _tmp_4 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_4);
        final int _tmp_5;
        _tmp_5 = _cursor.getInt(_cursorIndexOfProjectProtected);
        _item.projectProtected = _tmp_5 != 0;
        final int _tmp_6;
        _tmp_6 = _cursor.getInt(_cursorIndexOfMemberSignupEnabled);
        _item.memberSignupEnabled = _tmp_6 != 0;
        _item.memberDefaultRole = _cursor.getString(_cursorIndexOfMemberDefaultRole);
        final int _tmp_7;
        _tmp_7 = _cursor.getInt(_cursorIndexOfMembersEnabled);
        _item.membersEnabled = _tmp_7 != 0;
        final int _tmp_8;
        _tmp_8 = _cursor.getInt(_cursorIndexOfRolesEnabled);
        _item.rolesEnabled = _tmp_8 != 0;
        final int _tmp_9;
        _tmp_9 = _cursor.getInt(_cursorIndexOfTaskEnabled);
        _item.taskEnabled = _tmp_9 != 0;
        final int _tmp_10;
        _tmp_10 = _cursor.getInt(_cursorIndexOfStatusEnabled);
        _item.statusEnabled = _tmp_10 != 0;
        final int _tmp_11;
        _tmp_11 = _cursor.getInt(_cursorIndexOfMergeTaskStatus);
        _item.mergeTaskStatus = _tmp_11 != 0;
        _item.displayedInfo = _cursor.getInt(_cursorIndexOfDisplayedInfo);
        _item.associatedSubject = _cursor.getString(_cursorIndexOfAssociatedSubject);
        final int _tmp_12;
        _tmp_12 = _cursor.getInt(_cursorIndexOfProjectOngoing);
        _item.projectOngoing = _tmp_12 != 0;
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
