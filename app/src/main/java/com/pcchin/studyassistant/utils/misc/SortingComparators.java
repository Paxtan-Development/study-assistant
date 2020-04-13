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

import com.pcchin.studyassistant.functions.ConverterFunctions;

import org.apache.commons.lang3.ObjectUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

/** Comparators used to sort special classes/cases **/
public class SortingComparators {
    /** Comparator that sorts an ArrayList by the first string of the array.  **/
    public static final Comparator<ArrayList<String>> firstValComparator = (a, b) -> {
                if (a != null && a.size() > 0
                        && b != null && b.size() > 0) {
                    return ObjectUtils.compare(a.get(0).toLowerCase()
                            , b.get(0).toLowerCase());
                } else if ((a == null && b != null) ||
                        ((a != null && a.size() == 0) && (b != null && b.size() > 0))) {
                    // Returns -1 if a's value is null or does not have any contents
                    return -1;
                } else if ((a != null && b == null) || (a != null && a.size() > 0)) {
                    // Returns 1 if b's value is null or does not have any contents, line simplified
                    return 1;
                } else {
                    // Returns 0 if both arrays are null or both does not have any contents
                    return 0;
                }
        };

    /** Comparator that sorts and ArrayList by the third String, which would be the date
     *  of the array. **/
    public static final Comparator<ArrayList<String>> secondValDateComparator = (a, b) -> {
                if (a != null && a.size() >= 2
                        && b != null && b.size() >= 2) {
                    Date date_a, date_b;
                    // Check if date a is null
                    try {
                        date_a = ConverterFunctions.standardDateTimeFormat.parse(a.get(1));
                    } catch (ParseException e) {
                        try {
                            // Date a is null while date b is not
                            ConverterFunctions.standardDateTimeFormat.parse(b.get(1));
                            return -1;
                        } catch (ParseException e1) {
                            // Date a and date b are both null
                            return 0;
                        }
                    }

                    // Check if date b is null
                    try {
                        date_b = ConverterFunctions.standardDateTimeFormat.parse(b.get(1));
                    } catch (ParseException e) {
                        // Date b is null while date a is not
                        return 1;
                    }

                    return ObjectUtils.compare(date_a, date_b);
                } else if ((a == null && b != null) ||
                        ((a != null && a.size() < 2) && (b != null && b.size() >= 2))) {
                    // Returns -1 if a's value is null or does not have any contents
                    return -1;
                } else if ((a != null && b == null) || (a != null && a.size() >= 2)) {
                    // Returns 1 if b's value is null or does not have any contents, line simplified
                    return 1;
                } else {
                    // Returns 0 if both arrays are null or both does not have any contents
                    return 0;
                }
            };
}
