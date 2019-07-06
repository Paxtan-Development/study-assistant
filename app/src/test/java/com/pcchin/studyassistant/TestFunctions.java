package com.pcchin.studyassistant;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Random;

/** Functions used in tests **/
class TestFunctions {
    /** Generate a string with random characters.
     * @param count defines the max number of characters in the string. **/
    @NonNull
    static String randomString(int count) {
        return RandomStringUtils.random(new Random().nextInt(count));
    }

    /** Generate an ArrayList with random values.
     * @param count defines the max number of children in the array. **/
    static ArrayList<ArrayList<String>> randomArray(int count) {
        Random rand = new Random();
        ArrayList<ArrayList<String>> returnList = new ArrayList<>();
        for (int i = 0; i < rand.nextInt(count); i++) {
            ArrayList<String> temp = new ArrayList<>();
            for (int j = 0; j < rand.nextInt(count); j++) {
                temp.add(randomString(count));
            }
            returnList.add(temp);
        }
        return returnList;
    }
}
