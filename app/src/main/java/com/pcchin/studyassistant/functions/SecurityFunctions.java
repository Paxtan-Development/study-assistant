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

package com.pcchin.studyassistant.functions;

import android.util.Base64;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/** Functions used in hashing, encryption etc. **/
public class SecurityFunctions {
    /** Hashing method used in the passwords that prevent notes from being edited.
     * No need to be too secure as they can be easily found when exported. **/
    public static String notesHash(String original) {
        byte[] originalByte = null;
        // 1) SHA
        try {
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-512");
            originalByte = shaDigest.digest(original.getBytes());
        } catch (NoSuchAlgorithmException e) {
            Log.e("StudyAssistant", "Cryptography Error: Algorithm SHA-512 not found in" +
                    " MessageDigest.");
        }

        // 2) Blowfish
        blowfish(originalByte, original.getBytes(), Cipher.ENCRYPT_MODE);

        return Base64.encodeToString(originalByte, Base64.DEFAULT);
    }

    /** Encryption method used to protect subject contents in .subject files **/
    public static byte[] subjectEncrypt(String password,
                                        ArrayList<ArrayList<String>> content) {
        // TODO: PBKDF2 (Encrypt & Decrypt)
        byte[] responseByte = ConverterFunctions.arrayToJson(content).getBytes();
        byte[] passwordByte = password.getBytes();
        aes(responseByte, passwordByte, Cipher.ENCRYPT_MODE);
        blowfish(responseByte, passwordByte, Cipher.ENCRYPT_MODE);

        return responseByte;
    }

    /** Decryption method used to protect subject contents in .subject files. **/
    public static ArrayList<ArrayList<String>> subjectDecrypt(String password, byte[] content) {
        byte[] passwordByte = password.getBytes();
        aes(content, passwordByte, Cipher.DECRYPT_MODE);
        blowfish(content, passwordByte, Cipher.DECRYPT_MODE);
        return ConverterFunctions.jsonToArray(new String(content));
    }

    /** AES encryption/decryption via Cipher.getInstance().
     * @param mode takes either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * There is no return statement as
     * @param original has been modified in the statement. **/
    public static void aes(byte[] original, byte[] key, int mode) {
        byte[] modifiedKey;
        if (key.length > 32) {
            // Trim the key to 32 bytes in length
            modifiedKey = new byte[32];
        } else {
            // Add the same characters (or 0) if it is not enough
            modifiedKey = new byte[16];
            for (int i = 0; i < 16; i++) {
                if (key.length == 0) {
                    modifiedKey[i] = 0;
                } else {
                    modifiedKey[i] = key[i % key.length];
                }
            }
        }
        try {
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(mode, new SecretKeySpec
                    (modifiedKey, 0, modifiedKey.length, "AES"));
            aesCipher.doFinal(original);
        } catch (NoSuchAlgorithmException e) {
            Log.e("StudyAssistant", "Cryptography Error: Algorithm AES/CBC/PKCS5Padding "
                    + "not found in Cipher.getInstance(). Stack trace is");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Log.e("StudyAssistant", "Cryptography Error: The padding in algorithm "
                    + "AES/CBC/PKCS5Padding not found in Cipher.getInstance(). Stack trace is");
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Log.e("StudyAssistant", "Cryptography Error: The key provided in the algorithm"
                    + " AES/CBC/PKCS5Padding of " + Arrays.toString(key) + " is not valid. Stack trace is");
            e.printStackTrace();
        } catch (BadPaddingException e) {
            Log.e("StudyAssistant", "Cryptography Error: The padding provided in the "
                    + "algorithm AES/CBC/PKCS5Padding is not valid.");
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.e("StudyAssistant", "Cryptography Error: The block size provided in the "
                    + "algorithm AES/CBC/PKCS5Padding is not valid. The input provided is "
                    + Arrays.toString(original) + " and the key is "+ Arrays.toString(key));
            e.printStackTrace();
        }
    }

    /** Blowfish encryption/decryption via Cipher.getInstance().
     * @param mode takes either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     * There is no return statement as
     * @param original has been modified in the statement. **/
    public static void blowfish(byte[] original, byte[] key, int mode) {
        byte[] modifiedKey;
        if (key.length > 56) {
            // Trim the array short if its too long
            modifiedKey = new byte[56];
            System.arraycopy(key, 0, modifiedKey, 0, 56);
        } else if (key.length == 0) {
            modifiedKey = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        } else if (key.length < 8) {
            // Repeat the values in the array if its too short
            modifiedKey = new byte[8];
            for (int i = 0; i < 8; i++) {
                // Used remainder as it is possible to calculate multiple times
                modifiedKey[i] = key[i % key.length];
            }
        } else {
            modifiedKey = key;
        }
        try {
            Cipher blowfishCipher = Cipher.getInstance("BLOWFISH/CBC/PKCS5Padding");
            blowfishCipher.init(mode, new SecretKeySpec(modifiedKey,
                    0, modifiedKey.length, "BLOWFISH"));
            blowfishCipher.doFinal(original);
        } catch (NoSuchAlgorithmException e) {
            Log.e("StudyAssistant", "Cryptography Error: Algorithm BLOWFISH/CBC/PKCS5Padding"
                    + " not found in Cipher.getInstance().");
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            Log.e("StudyAssistant", "Cryptography Error: The padding PKCS5Padding does "
                    + " not exist in algorithm BLOWFISH/CBC/PKCS5Padding in Cipher.getInstance().");
            e.printStackTrace();
        } catch (BadPaddingException e) {
            Log.e("StudyAssistant", "Cryptography Error: The padding provided in the "
                    + "algorithm BLOWFISH/CBC/PKCS5Padding is not valid.");
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            Log.e("StudyAssistant", "Cryptography Error: The block size provided in the "
                    + "algorithm BLOWFISH/CBC/PKCS5Padding is not valid. The input provided is "
                    + Arrays.toString(original) + " and the key is "+ Arrays.toString(modifiedKey));
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            Log.e("StudyAssistant", "Cryptography Error: The key provided in the"
                    + " algorithm BLOWFISH/CBC/PKCS5Padding of " + Arrays.toString(key) + " is not valid.");
            e.printStackTrace();
        }
    }
}
