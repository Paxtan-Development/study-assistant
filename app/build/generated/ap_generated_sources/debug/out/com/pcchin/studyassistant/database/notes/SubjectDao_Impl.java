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

package com.pcchin.studyassistant.database.notes;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.pcchin.studyassistant.functions.ConverterFunctions;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SubjectDao_Impl implements SubjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<NotesSubject> __insertionAdapterOfNotesSubject;

  private final EntityDeletionOrUpdateAdapter<NotesSubject> __deletionAdapterOfNotesSubject;

  private final EntityDeletionOrUpdateAdapter<NotesSubject> __updateAdapterOfNotesSubject;

  public SubjectDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNotesSubject = new EntityInsertionAdapter<NotesSubject>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `NotesSubject` (`_title`,`contents`,`sortOrder`) VALUES (?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, NotesSubject value) {
        if (value.title == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.title);
        }
        final String _tmp;
        _tmp = ConverterFunctions.doubleArrayToJson(value.contents);
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        stmt.bindLong(3, value.sortOrder);
      }
    };
    this.__deletionAdapterOfNotesSubject = new EntityDeletionOrUpdateAdapter<NotesSubject>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `NotesSubject` WHERE `_title` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, NotesSubject value) {
        if (value.title == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.title);
        }
      }
    };
    this.__updateAdapterOfNotesSubject = new EntityDeletionOrUpdateAdapter<NotesSubject>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `NotesSubject` SET `_title` = ?,`contents` = ?,`sortOrder` = ? WHERE `_title` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, NotesSubject value) {
        if (value.title == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.title);
        }
        final String _tmp;
        _tmp = ConverterFunctions.doubleArrayToJson(value.contents);
        if (_tmp == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, _tmp);
        }
        stmt.bindLong(3, value.sortOrder);
        if (value.title == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.title);
        }
      }
    };
  }

  @Override
  public void insert(final NotesSubject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfNotesSubject.insert(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final NotesSubject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfNotesSubject.handle(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final NotesSubject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfNotesSubject.handle(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public NotesSubject search(final String title) {
    final String _sql = "SELECT * FROM notesSubject WHERE _title = ?";
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
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "_title");
      final int _cursorIndexOfContents = CursorUtil.getColumnIndexOrThrow(_cursor, "contents");
      final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
      final NotesSubject _result;
      if(_cursor.moveToFirst()) {
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final ArrayList<ArrayList<String>> _tmpContents;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfContents);
        _tmpContents = ConverterFunctions.doubleJsonToArray(_tmp);
        final int _tmpSortOrder;
        _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
        _result = new NotesSubject(_tmpTitle,_tmpContents,_tmpSortOrder);
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
  public List<NotesSubject> getAll() {
    final String _sql = "SELECT * FROM notesSubject ORDER BY _title ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "_title");
      final int _cursorIndexOfContents = CursorUtil.getColumnIndexOrThrow(_cursor, "contents");
      final int _cursorIndexOfSortOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "sortOrder");
      final List<NotesSubject> _result = new ArrayList<NotesSubject>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final NotesSubject _item;
        final String _tmpTitle;
        _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
        final ArrayList<ArrayList<String>> _tmpContents;
        final String _tmp;
        _tmp = _cursor.getString(_cursorIndexOfContents);
        _tmpContents = ConverterFunctions.doubleJsonToArray(_tmp);
        final int _tmpSortOrder;
        _tmpSortOrder = _cursor.getInt(_cursorIndexOfSortOrder);
        _item = new NotesSubject(_tmpTitle,_tmpContents,_tmpSortOrder);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
