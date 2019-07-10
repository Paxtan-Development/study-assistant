package com.pcchin.studyassistant.functions;

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

import de.rtner.misc.BinTools;
import de.rtner.security.auth.spi.PBKDF2Engine;
import de.rtner.security.auth.spi.PBKDF2Parameters;

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

        if (originalByte == null) {
            return null;
        } else {
            return BinTools.bin2hex(originalByte);
        }
    }

    /** Encryption method used to protect subject contents in .subject files **/
    public static byte[] subjectEncrypt(String title, String password,
                                        ArrayList<ArrayList<String>> content) {
        Log.d("Log", "S1");
        byte[] responseByte = ConverterFunctions.arrayToJson(content).getBytes();
        Log.d("Log", "S2");
        byte[] passwordByte = pbkdf2(password, title.getBytes());
        Log.d("Log", "S3");
        aes(responseByte, passwordByte, Cipher.ENCRYPT_MODE);
        Log.d("Log", "S4");
        blowfish(responseByte, passwordByte, Cipher.ENCRYPT_MODE);
        Log.d("Log", "S5");

        return responseByte;
    }

    /** Decryption method used to protect subject contents in .subject files. **/
    public static ArrayList<ArrayList<String>> subjectDecrypt(String title,
                                                       String password, byte[] content) {
        byte[] passwordByte = pbkdf2(password, title.getBytes());
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

    /** PBKDF2 encryption/decryption. An external library has been used
     * instead of BouncyCastle to save space. The value of the salt is used in multiples of 3,
     * unless it is too short, in which cased it is recursively divided by 2 and minus 1
     * until a valid response is obtained. **/
    public static byte[] pbkdf2(String original, byte[] salt) {
        byte[] pbkdfSalt = new byte[64];
        for (int i = 0; i < 64; i++) {
            int referenceVal = i * 3;
            while (salt.length > referenceVal) {
                referenceVal /= 2;
                referenceVal -= 1;
            }
            pbkdfSalt[i] = salt[referenceVal];
        }
        PBKDF2Parameters params = new PBKDF2Parameters(
                "HmacSHA256", "UTF-8", pbkdfSalt, 10000);
        return new PBKDF2Engine(params).deriveKey(original);
    }
}
