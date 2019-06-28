package com.pcchin.studyassistant;

import com.pcchin.studyassistant.functions.GeneralFunctions;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/** Test functions that do not require context or activities to work. **/
public class FunctionTest {
    @Rule
    public static final int TEST_COUNT = 10000;
    @Rule
    public Random rand = new Random();

    /** Test JSON to ArrayList and vice versa. **/
    @Test
    public void testJson() {
        ArrayList<ArrayList<String>> initialArray = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(TEST_COUNT); i++) {
            ArrayList<String> temp = new ArrayList<>();
            for (int j = 0; j < rand.nextInt(TEST_COUNT); i++) {
                temp.add(TestFunctions.randomString(TEST_COUNT));
            }
            initialArray.add(temp);
        }
        ArrayList<ArrayList<String>> compareArray = GeneralFunctions
                .jsonToArray(GeneralFunctions.arrayToJson(initialArray));
        Assert.assertNotNull(compareArray);
        Assert.assertEquals(initialArray.size(), compareArray.size());
        Assert.assertEquals(initialArray, compareArray);
    }
}
