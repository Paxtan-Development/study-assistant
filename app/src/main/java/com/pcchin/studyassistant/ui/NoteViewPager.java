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

package com.pcchin.studyassistant.ui;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.pcchin.studyassistant.activity.MainActivityFunctions;
import com.pcchin.studyassistant.database.notes.NotesContent;

import java.util.List;

/** A custom ViewPager that is stores the positions of the note IDs corresponding to their position. **/
public class NoteViewPager extends ViewPager {
    public List<NotesContent> notesList;

    public NoteViewPager(@NonNull Context context) {
        super(context);
    }

    public NoteViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(@Nullable PagerAdapter adapter) {
        super.setAdapter(adapter);
        if (adapter instanceof MainActivityFunctions.NotePagerAdapter) {
            notesList = ((MainActivityFunctions.NotePagerAdapter) adapter).notesList;
        }
    }

    /** Set the pager to show the note requested based on the given note ID. **/
    public void setPagerOrder(int noteId) {
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).noteId == noteId) {
                setCurrentItem(i);
                break;
            }
        }
    }
}
