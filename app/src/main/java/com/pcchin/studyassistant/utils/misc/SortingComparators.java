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

package com.pcchin.studyassistant.utils.misc;

import com.pcchin.studyassistant.database.notes.NotesContent;

import org.apache.commons.lang3.ObjectUtils;

import java.util.Comparator;

/** Comparators used to sort special classes/cases **/
public class SortingComparators {
    private SortingComparators() {
        throw new IllegalStateException("Utility class");
    }

    /** Comparator that sorts NotesContent by their title.
     * If the titles are the same, they are then sorted by their edited date **/
    public static final Comparator<NotesContent> noteTitleComparator =
            (a, b) -> ObjectUtils.compare(a.noteTitle, b.noteTitle);

    /** Comparator that sorts NotesContent by their last edited date. **/
    public static final Comparator<NotesContent> noteDateComparator =
            (a, b) -> ObjectUtils.compare(a.lastEdited, b.lastEdited);
}
