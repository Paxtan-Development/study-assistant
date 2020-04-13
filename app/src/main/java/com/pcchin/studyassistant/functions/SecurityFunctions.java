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

package com.pcchin.studyassistant.functions;

import android.util.Base64;
import android.util.Log;

import com.pcchin.studyassistant.ui.MainActivity;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.crypto.params.ParametersWithRandom;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/** Functions used in hashing, encryption, decryption etc. **/
public class SecurityFunctions {
    /** Constructor made private to simulate static class. **/
    private SecurityFunctions() {}

    /** Process a cipher buffer based on a specific length and
     * @return a specific output. **/
    private static byte[] processCipherBuffer(PaddedBufferedBlockCipher cipher, byte[] original)
            throws InvalidCipherTextException, DataLengthException {
        byte[] output = new byte[cipher.getOutputSize(original.length)];
        int length1 = cipher.processBytes(original,  0, original.length, output, 0);
        int length2 = cipher.doFinal(output, length1);
        byte[] result = new byte[length1+length2];
        System.arraycopy(output, 0, result, 0, result.length);
        return result;
    }

    /** Trims a byte array to a specific length, or adds to it if its not enough. **/
    private static byte[] trimByte(byte[] original, int size) {
        byte[] response = new byte[size];
        if (original.length > size) {
            // Trim the key to 32 bytes in length (256 bits)
            System.arraycopy(original, 0, response, 0, response.length);
        } else if (original.length > 0){
            // Add the same characters if it is not enough
            for (int i = 0; i < size; i++) {
                response[i] = original[i % original.length];
            }
        } else {
            Arrays.fill(response, (byte) 0);
        }
        return response;
    }

    /** Hashing method used in the passwords that prevent notes from being edited.
     * No need to be too secure as the contents of the notes can be easily found when exported. **/
    public static String notesHash(String original) {
        byte[] originalByte = null;
        // 1) SHA
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            originalByte = shaDigest.digest(original.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(MainActivity.LOG_APP_NAME, "Cryptography Error: Algorithm SHA-512 not found in" +
                    " MessageDigest.");
        }

        // 2) Blowfish
        originalByte = blowfish(originalByte, original.getBytes(), true);

        return Base64.encodeToString(originalByte, Base64.DEFAULT);
    }

    /** Hashing method used in the passwords of projects when logging in. **/
    public static String projectHash(String original, String salt) {
        // 1) PBKDF2
        byte[] originalByte = pbkdf2(original.getBytes(), salt.getBytes(), 10800);

        // 2) SHA
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            originalByte = shaDigest.digest(originalByte);
        } catch (NoSuchAlgorithmException e) {
            Log.e(MainActivity.LOG_APP_NAME, "Cryptography Error: Algorithm SHA-512 not found in" +
                    " MessageDigest.");
        }

        // 3) Blowfish
        originalByte = blowfish(originalByte, original.getBytes(), true);

        return new String(originalByte);
    }

    /** Hashing method used in the passwords of roles when logging in. **/
    public static String roleHash(String original, String salt){
        byte[] originalByte = null, hashedPassword = null;
        // 1) SHA
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            originalByte = shaDigest.digest(original.getBytes());
            hashedPassword = shaDigest.digest(salt.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(MainActivity.LOG_APP_NAME, "Cryptography Error: Algorithm SHA-512 not found in" +
                    " MessageDigest.");
        }

        // 2) PBKDF
        originalByte = pbkdf2(originalByte, salt.getBytes(), 10000);

        // 3) Blowfish
        originalByte = blowfish(originalByte, hashedPassword, true);

        return Base64.encodeToString(originalByte, Base64.DEFAULT);
    }

    /** Hashing method used in the passwords of members when logging in. **/
    public static String memberHash(String original, String salt, String iv) {
        byte[] originalByte, ivBytes = null;
        // 1) PBKDF
        originalByte = pbkdf2(original.getBytes(), salt.getBytes(), 10000);

        // 2) SHA
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            ivBytes = shaDigest.digest(iv.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e(MainActivity.LOG_APP_NAME, "Cryptography Error: Algorithm SHA-512 not found in" +
                    " MessageDigest.");
        }

        // 3) AES
        originalByte = aes(originalByte, salt.getBytes(), ivBytes, true);

        return Base64.encodeToString(originalByte, Base64.DEFAULT);
    }

    /** Encryption method used to protect subject contents in .subject files **/
    public static byte[] subjectEncrypt(String title, String password,
                                        ArrayList<ArrayList<String>> content) {
        byte[] responseByte = ConverterFunctions.doubleArrayToJson(content).getBytes();
        byte[] passwordByte = pbkdf2(password.getBytes(), title.getBytes(), 12000);
        responseByte = aes(responseByte, passwordByte, title.getBytes(), true);
        responseByte = blowfish(responseByte, passwordByte, true);

        return responseByte;
    }

    /** Decryption method used to protect subject contents in .subject files. **/
    public static ArrayList<ArrayList<String>> subjectDecrypt(String title, String password,
                                                              byte[] content) {
        byte[] passwordByte = pbkdf2(password.getBytes(), title.getBytes(), 12000);
        content = blowfish(content, passwordByte, false);
        content = aes(content, passwordByte, title.getBytes(), false);
        return ConverterFunctions.doubleJsonToArray(new String(content));
    }

    /** AES encryption/decryption via PaddedBufferedBlockCipher in BouncyCastle.
     * IV added as additional security measure. **/
    public static byte[] aes(byte[] original, byte[] key, byte[] iv, boolean isEncrypt) {
        key = trimByte(key, 32);
        iv = trimByte(iv, 16);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(isEncrypt, new ParametersWithIV(new KeyParameter(key), iv, 0, 16));
        try {
            original = processCipherBuffer(aes, original);
        } catch (InvalidCipherTextException e) {
            Log.w(MainActivity.LOG_APP_NAME, "Cipher text in AES encryption invalid. Stack trace is ");
            e.printStackTrace();
        } catch (DataLengthException e) {
            Log.w(MainActivity.LOG_APP_NAME, "Data length in AES encryption invalid. Stack trace is ");
            e.printStackTrace();
        }
        return original;
    }

    /** Blowfish encryption/decryption via PaddedBufferedBlockCipher in BouncyCastle. **/
    public static byte[] blowfish(byte[] original, byte[] key, boolean isEncrypt) {
        key = trimByte(key, 56);
        PaddedBufferedBlockCipher blowfish = new PaddedBufferedBlockCipher(new CBCBlockCipher(
                new BlowfishEngine()));
        blowfish.init(isEncrypt, new ParametersWithRandom(new KeyParameter(key)));
        try {
            original = processCipherBuffer(blowfish, original);
        } catch (InvalidCipherTextException e) {
            Log.w(MainActivity.LOG_APP_NAME, "Cipher text in Blowfish encryption invalid. Stack trace is ");
            e.printStackTrace();
        } catch (DataLengthException e) {
            Log.w(MainActivity.LOG_APP_NAME, "Data length in Blowfish encryption invalid. Stack trace is ");
            e.printStackTrace();
        }
        return original;
    }

    /** PBKDF2 hashing method with SHA 256.
     * @param iterations should be >=100000 to ensure that the hash is secure.
     * @return A byte[] of the hashed String. If an cryptography error occurs during encryption,
     * original.getBytes() would be returned. **/
    public static byte[] pbkdf2(byte[] original, byte[] salt, int iterations) {
        PKCS5S2ParametersGenerator pbkdf2 = new PKCS5S2ParametersGenerator(new SHA256Digest());
        pbkdf2.init(original, salt, iterations);
        return ((KeyParameter) pbkdf2.generateDerivedParameters(original.length)).getKey();
    }
}
