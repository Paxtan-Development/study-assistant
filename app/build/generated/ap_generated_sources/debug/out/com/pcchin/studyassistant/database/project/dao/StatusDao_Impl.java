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
import com.pcchin.studyassistant.database.project.data.StatusData;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class StatusDao_Impl implements StatusDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StatusData> __insertionAdapterOfStatusData;

  private final EntityDeletionOrUpdateAdapter<StatusData> __deletionAdapterOfStatusData;

  private final EntityDeletionOrUpdateAdapter<StatusData> __updateAdapterOfStatusData;

  public StatusDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStatusData = new EntityInsertionAdapter<StatusData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `StatusData` (`_statusID`,`publishedDate`,`statusTitle`,`statusType`,`statusContent`,`parentProject`,`memberID`,`statusColor`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, StatusData value) {
        if (value.statusID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.statusID);
        }
        final String _tmp;
        _tmp = ConverterFunctions.dateToString(value.publishedDate);
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        if (value.statusTitle == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.statusTitle);
        }
        if (value.statusType == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.statusType);
        }
        if (value.statusContent == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.statusContent);
        }
        if (value.parentProject == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.parentProject);
        }
        if (value.memberID == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.memberID);
        }
        stmt.bindLong(8, value.statusColor);
      }
    };
    this.__deletionAdapterOfStatusData = new EntityDeletionOrUpdateAdapter<StatusData>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `StatusData` WHERE `_statusID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, StatusData value) {
        if (value.statusID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.statusID);
        }
      }
    };
    this.__updateAdapterOfStatusData = new EntityDeletionOrUpdateAdapter<StatusData>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `StatusData` SET `_statusID` = ?,`publishedDate` = ?,`statusTitle` = ?,`statusType` = ?,`statusContent` = ?,`parentProject` = ?,`memberID` = ?,`statusColor` = ? WHERE `_statusID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, StatusData value) {
        if (value.statusID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.statusID);
        }
        final String _tmp;
        _tmp = ConverterFunctions.dateToString(value.publishedDate);
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        if (value.statusTitle == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.statusTitle);
        }
        if (value.statusType == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.statusType);
        }
        if (value.statusContent == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, value.statusContent);
        }
        if (value.parentProject == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, value.parentProject);
        }
        if (value.memberID == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, value.memberID);
        }
        stmt.bindLong(8, value.statusColor);
        if (value.statusID == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.statusID);
        }
      }
    };
  }

  @Override
  public void insert(final StatusData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfStatusData.insert(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final StatusData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfStatusData.handle(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final StatusData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfStatusData.handle(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public StatusData searchByID(final String ID) {
    final String _sql = "SELECT * FROM statusData WHERE _statusID = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (ID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, ID);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final StatusData _result;
      if(_cursor.moveToFirst()) {
        _result = new StatusData();
        _result.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _result.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _result.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _result.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _result.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _result.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _result.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _result.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
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
  public List<StatusData> searchByTitle(final String title) {
    final String _sql = "SELECT * FROM statusData WHERE statusTitle = ?";
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
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchByProject(final String projectID) {
    final String _sql = "SELECT * FROM statusData WHERE parentProject = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchByType(final String type) {
    final String _sql = "SELECT * FROM statusData WHERE statusType = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchByMember(final String memberID) {
    final String _sql = "SELECT * FROM statusData WHERE memberID = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (memberID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberID);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchInProjectByTitle(final String projectID, final String title) {
    final String _sql = "SELECT * FROM statusData WHERE parentProject = ? AND statusTitle = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    if (title == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, title);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchInProjectByType(final String projectID, final String type) {
    final String _sql = "SELECT * FROM statusData WHERE parentProject = ? AND statusType = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<StatusData> searchInProjectByMember(final String projectID, final String memberID) {
    final String _sql = "SELECT * FROM statusData WHERE parentProject = ? AND memberID = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    if (memberID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, memberID);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfStatusID = CursorUtil.getColumnIndexOrThrow(_cursor, "_statusID");
      final int _cursorIndexOfPublishedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "publishedDate");
      final int _cursorIndexOfStatusTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "statusTitle");
      final int _cursorIndexOfStatusType = CursorUtil.getColumnIndexOrThrow(_cursor, "statusType");
      final int _cursorIndexOfStatusContent = CursorUtil.getColumnIndexOrThrow(_cursor, "statusContent");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfMemberID = CursorUtil.getColumnIndexOrThrow(_cursor, "memberID");
      final int _cursorIndexOfStatusColor = CursorUtil.getColumnIndexOrThrow(_cursor, "statusColor");
      final List<StatusData> _result = new ArrayList<StatusData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final StatusData _item;
        _item = new StatusData();
        _item.statusID = _cursor.getString(_cursorIndexOfStatusID);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfPublishedDate);
        _item.publishedDate = ConverterFunctions.stringToDate(_tmp);
        _item.statusTitle = _cursor.getString(_cursorIndexOfStatusTitle);
        _item.statusType = _cursor.getString(_cursorIndexOfStatusType);
        _item.statusContent = _cursor.getString(_cursorIndexOfStatusContent);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        _item.memberID = _cursor.getString(_cursorIndexOfMemberID);
        _item.statusColor = _cursor.getInt(_cursorIndexOfStatusColor);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
