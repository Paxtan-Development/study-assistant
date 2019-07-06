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
}
