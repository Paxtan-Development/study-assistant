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
import com.pcchin.studyassistant.database.project.data.TaskData;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class TaskDao_Impl implements TaskDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TaskData> __insertionAdapterOfTaskData;

  private final EntityDeletionOrUpdateAdapter<TaskData> __deletionAdapterOfTaskData;

  private final EntityDeletionOrUpdateAdapter<TaskData> __updateAdapterOfTaskData;

  public TaskDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTaskData = new EntityInsertionAdapter<TaskData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `TaskData` (`_taskID`,`taskDesc`,`parentProject`,`expectedStartDate`,`expectedEndDate`,`actualStartDate`,`actualEndDate`,`assignedMember`,`taskStatus`,`taskStatusCustom`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TaskData value) {
        if (value.taskID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.taskID);
        }
        if (value.taskDesc == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.taskDesc);
        }
        if (value.parentProject == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.parentProject);
        }
        final String _tmp;
        _tmp = ConverterFunctions.dateToString(value.expectedStartDate);
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp);
        }
        final String _tmp_1;
        _tmp_1 = ConverterFunctions.dateToString(value.expectedEndDate);
        if (_tmp_1 == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, _tmp_1);
        }
        final String _tmp_2;
        _tmp_2 = ConverterFunctions.dateToString(value.actualStartDate);
        if (_tmp_2 == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, _tmp_2);
        }
        final String _tmp_3;
        _tmp_3 = ConverterFunctions.dateToString(value.actualEndDate);
        if (_tmp_3 == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, _tmp_3);
        }
        if (value.assignedMember == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.assignedMember);
        }
        stmt.bindLong(9, value.taskStatus);
        if (value.taskStatusCustom == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.taskStatusCustom);
        }
      }
    };
    this.__deletionAdapterOfTaskData = new EntityDeletionOrUpdateAdapter<TaskData>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `TaskData` WHERE `_taskID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TaskData value) {
        if (value.taskID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.taskID);
        }
      }
    };
    this.__updateAdapterOfTaskData = new EntityDeletionOrUpdateAdapter<TaskData>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `TaskData` SET `_taskID` = ?,`taskDesc` = ?,`parentProject` = ?,`expectedStartDate` = ?,`expectedEndDate` = ?,`actualStartDate` = ?,`actualEndDate` = ?,`assignedMember` = ?,`taskStatus` = ?,`taskStatusCustom` = ? WHERE `_taskID` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, TaskData value) {
        if (value.taskID == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.taskID);
        }
        if (value.taskDesc == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.taskDesc);
        }
        if (value.parentProject == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.parentProject);
        }
        final String _tmp;
        _tmp = ConverterFunctions.dateToString(value.expectedStartDate);
        if (_tmp == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, _tmp);
        }
        final String _tmp_1;
        _tmp_1 = ConverterFunctions.dateToString(value.expectedEndDate);
        if (_tmp_1 == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindString(5, _tmp_1);
        }
        final String _tmp_2;
        _tmp_2 = ConverterFunctions.dateToString(value.actualStartDate);
        if (_tmp_2 == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindString(6, _tmp_2);
        }
        final String _tmp_3;
        _tmp_3 = ConverterFunctions.dateToString(value.actualEndDate);
        if (_tmp_3 == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindString(7, _tmp_3);
        }
        if (value.assignedMember == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.assignedMember);
        }
        stmt.bindLong(9, value.taskStatus);
        if (value.taskStatusCustom == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.taskStatusCustom);
        }
        if (value.taskID == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.taskID);
        }
      }
    };
  }

  @Override
  public void insert(final TaskData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTaskData.insert(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final TaskData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfTaskData.handle(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final TaskData status) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfTaskData.handle(status);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public TaskData searchById(final String ID) {
    final String _sql = "SELECT * FROM taskData WHERE _taskID = ?";
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
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final TaskData _result;
      if(_cursor.moveToFirst()) {
        _result = new TaskData();
        _result.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _result.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _result.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _result.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _result.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _result.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _result.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _result.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _result.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _result.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
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
  public List<TaskData> searchByProject(final String projectID) {
    final String _sql = "SELECT * FROM taskData WHERE parentProject = ?";
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
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchByStatus(final int status) {
    final String _sql = "SELECT * FROM taskData WHERE taskStatus = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, status);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchByMember(final String member) {
    final String _sql = "SELECT * FROM taskData WHERE assignedMember = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (member == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, member);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchByDesc(final String desc) {
    final String _sql = "SELECT * FROM taskData WHERE taskDesc = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (desc == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, desc);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchInProjectByStatus(final String projectID, final int status) {
    final String _sql = "SELECT * FROM taskData WHERE parentProject = ? AND taskStatus = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, status);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchInProjectByMember(final String projectID, final String member) {
    final String _sql = "SELECT * FROM taskData WHERE parentProject = ? AND assignedMember = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    if (member == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, member);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<TaskData> searchInProjectByDesc(final String projectID, final String desc) {
    final String _sql = "SELECT * FROM taskData WHERE parentProject = ? AND taskDesc = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (projectID == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, projectID);
    }
    _argIndex = 2;
    if (desc == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, desc);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTaskID = CursorUtil.getColumnIndexOrThrow(_cursor, "_taskID");
      final int _cursorIndexOfTaskDesc = CursorUtil.getColumnIndexOrThrow(_cursor, "taskDesc");
      final int _cursorIndexOfParentProject = CursorUtil.getColumnIndexOrThrow(_cursor, "parentProject");
      final int _cursorIndexOfExpectedStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedStartDate");
      final int _cursorIndexOfExpectedEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "expectedEndDate");
      final int _cursorIndexOfActualStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualStartDate");
      final int _cursorIndexOfActualEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "actualEndDate");
      final int _cursorIndexOfAssignedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "assignedMember");
      final int _cursorIndexOfTaskStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatus");
      final int _cursorIndexOfTaskStatusCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "taskStatusCustom");
      final List<TaskData> _result = new ArrayList<TaskData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final TaskData _item;
        _item = new TaskData();
        _item.taskID = _cursor.getString(_cursorIndexOfTaskID);
        _item.taskDesc = _cursor.getString(_cursorIndexOfTaskDesc);
        _item.parentProject = _cursor.getString(_cursorIndexOfParentProject);
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfExpectedStartDate);
        _item.expectedStartDate = ConverterFunctions.stringToDate(_tmp);
        final String _tmp_1;
        _tmp_1 = _cursor.getString(_cursorIndexOfExpectedEndDate);
        _item.expectedEndDate = ConverterFunctions.stringToDate(_tmp_1);
        final String _tmp_2;
        _tmp_2 = _cursor.getString(_cursorIndexOfActualStartDate);
        _item.actualStartDate = ConverterFunctions.stringToDate(_tmp_2);
        final String _tmp_3;
        _tmp_3 = _cursor.getString(_cursorIndexOfActualEndDate);
        _item.actualEndDate = ConverterFunctions.stringToDate(_tmp_3);
        _item.assignedMember = _cursor.getString(_cursorIndexOfAssignedMember);
        _item.taskStatus = _cursor.getInt(_cursorIndexOfTaskStatus);
        _item.taskStatusCustom = _cursor.getString(_cursorIndexOfTaskStatusCustom);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
