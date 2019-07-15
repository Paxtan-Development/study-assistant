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

package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.ConverterFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/** Test the converters and whether they are working. **/
public class ConverterTest {
    private static final int TEST_COUNT = 5000;

    /** Test JSON to ArrayList and vice versa. **/
    @Test
    public void testJson() {
        Random rand = new Random();
        ArrayList<ArrayList<String>> initialArray = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            ArrayList<String> temp = new ArrayList<>();
            for (int j = 0; j < rand.nextInt(TEST_COUNT); i++) {
                temp.add(TestFunctions.randomString(TEST_COUNT));
            }
            initialArray.add(temp);
        }
        ArrayList<ArrayList<String>> compareArray = ConverterFunctions
                .jsonToArray(ConverterFunctions.arrayToJson(initialArray));
        Assert.assertNotNull(compareArray);
        Assert.assertEquals(initialArray.size(), compareArray.size());
        Assert.assertEquals(initialArray, compareArray);
    }

    /** Test int to byte[] and vice versa. **/
    @Test
    public void testIntByte() {
        Random rand = new Random();
        int currentInt, convertedInt;
        int targetInt = rand.nextInt(TEST_COUNT * 10);
        while (targetInt < 10000) {
            targetInt = rand.nextInt(TEST_COUNT * 10);
        }
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            currentInt = rand.nextInt();
            convertedInt = ConverterFunctions.bytesToInt(ConverterFunctions.intToBytes(currentInt));
            Assert.assertEquals(currentInt, convertedInt);
        }
    }
}
