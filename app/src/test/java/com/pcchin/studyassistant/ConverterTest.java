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

package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.ConverterFunctions;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/** Test the converters and whether they are working. **/
public class ConverterTest {
    private static int TEST_COUNT;

    /** Default constructor. **/
    public ConverterTest() {
        if (BuildConfig.IS_LOCAL) {
            TEST_COUNT = 5000;
        } else {
            TEST_COUNT = 1000;
        }
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
