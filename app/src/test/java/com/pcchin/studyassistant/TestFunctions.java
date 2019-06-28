package com.pcchin.studyassistant;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.Random;

/** Functions used in tests **/
class TestFunctions {@NonNull
    static String randomString(int count) {
        return RandomStringUtils.random(new Random().nextInt(count));
    }
}
