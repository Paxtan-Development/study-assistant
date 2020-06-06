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

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.pcchin.studyassistant.activity.ActivityConstants;
import com.pcchin.studyassistant.database.notes.NotesContent;
import com.pcchin.studyassistant.database.notes.SubjectDatabase;

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
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/** Functions used in hashing, encryption, decryption etc. **/
public final class SecurityFunctions {
    private SecurityFunctions() {
        throw new IllegalStateException("Utility class");
    }

    /** Process a cipher buffer based on a specific length and
     * @return a specific output. **/
    @NonNull
    private static byte[] processCipherBuffer(@NonNull PaddedBufferedBlockCipher cipher, @NonNull byte[] original)
            throws InvalidCipherTextException, DataLengthException {
        byte[] output = new byte[cipher.getOutputSize(original.length)];
        int length1 = cipher.processBytes(original,  0, original.length, output, 0);
        int length2 = cipher.doFinal(output, length1);
        byte[] result = new byte[length1+length2];
        System.arraycopy(output, 0, result, 0, result.length);
        return result;
    }

    /** Trims a byte array to a specific length, or adds to it if its not enough. **/
    @NonNull
    private static byte[] trimByte(@NonNull byte[] original, int size) {
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

    /** Functions used to hash a password through PBKDF2. This function replaces notesHash,
     * projectHash, roleHash and memberHash. **/
    public static String passwordHash(@NonNull String original, @NonNull String salt) {
        return Base64.encodeToString(pbkdf2(original.getBytes(), salt.getBytes(), 10800),
                Base64.DEFAULT);
    }

    /** Encryption method used to protect subject contents in .subject files. **/
    public static byte[] subjectEncrypt(@NonNull String title, @NonNull String password,
                                        List<NotesContent> content) {
        byte[] responseByte = ConverterFunctions.notesListToString(content).getBytes();
        byte[] passwordByte = pbkdf2(password.getBytes(), title.getBytes(), 12000);
        responseByte = aes(responseByte, passwordByte, title.getBytes(), true);
        responseByte = blowfish(responseByte, passwordByte, true);
        return responseByte;
    }

    /** Decryption method used to protect subject contents in .subject files. **/
    public static ArrayList<NotesContent> subjectDecrypt(SubjectDatabase database, int subjectId,
                                                              @NonNull String title, @NonNull String password,
                                                              byte[] content) {
        byte[] passwordByte = pbkdf2(password.getBytes(), title.getBytes(), 12000);
        content = blowfish(content, passwordByte, false);
        content = aes(content, passwordByte, title.getBytes(), false);
        return ConverterFunctions.stringToNotesList(database, subjectId, new String(content));
    }

    /** Encrypts the message sent to the server through its public RSA key (PKCS1-OAEP). **/
    public static String RSAServerEncrypt(Context context, String original) {
        // Returns a UTF-8 String buffer
        try (InputStream inputStream = context.getAssets().open("public.pem")) {
            // Gets the key
            StringBuilder contentBuilder = new StringBuilder();
            try (Scanner scanner = new Scanner(inputStream)) {
                while (scanner.hasNext()) contentBuilder.append(scanner.next());
            }
            // PEM needs to be decoded to X509 for it to be accepted by the RSA Engine
            byte[] decodedKey = Base64.decode(contentBuilder.toString(), Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            ByteArrayOutputStream outputStream = getRSAStream(keySpec, original);
            // UTF-8 String does not work but hex string does
            return ConverterFunctions.bytesToHex(outputStream.toByteArray());
        } catch (IOException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "File Error: Unable to get public server RSA Key, stack trace is");
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException
                | BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Cryptography Error: Unable to encrypt message using server RSA key, stack trace is");
            e.printStackTrace();
            return null;
        }
    }

    /** Get the ByteArrayOutputStream for the RSA stream. **/
    @NonNull
    private static ByteArrayOutputStream getRSAStream(X509EncodedKeySpec keySpec, @NonNull String original)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeyException {
        Security.addProvider(new BouncyCastleProvider());
        Cipher rsa = Cipher.getInstance("RSA/NONE/OAEPPadding");
        KeyFactory.getInstance("RSA");
        rsa.init(Cipher.ENCRYPT_MODE, KeyFactory.getInstance("RSA").generatePublic(keySpec));

        // Split into 240 bytes per encoding
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
        int index = 0;
        while (index < originalBytes.length) {
            byte[] currentBytes = Arrays.copyOfRange(originalBytes, index, Math.min(index + 128, originalBytes.length));
            byte[] outputBytes = rsa.doFinal(currentBytes);
            outputStream.write(outputBytes);
            index += 128;
        }
        return outputStream;
    }

    /** AES encryption/decryption via PaddedBufferedBlockCipher in BouncyCastle.
     * IV added as additional security measure.
     * This function should not be used by itself outside of this class except for unit testing. **/
    public static byte[] aes(byte[] original, byte[] key, byte[] iv, boolean isEncrypt) {
        key = trimByte(key, 32);
        iv = trimByte(iv, 16);
        PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
        aes.init(isEncrypt, new ParametersWithIV(new KeyParameter(key), iv, 0, 16));
        try {
            original = processCipherBuffer(aes, original);
        } catch (InvalidCipherTextException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Cipher text in AES encryption invalid. Stack trace is ");
            e.printStackTrace();
        } catch (DataLengthException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Data length in AES encryption invalid. Stack trace is ");
            e.printStackTrace();
        }
        return original;
    }

    /** Blowfish encryption/decryption via PaddedBufferedBlockCipher in BouncyCastle.
     * This function should not be used by itself outside of this class except for unit testing. **/
    public static byte[] blowfish(byte[] original, byte[] key, boolean isEncrypt) {
        key = trimByte(key, 56);
        PaddedBufferedBlockCipher blowfish = new PaddedBufferedBlockCipher(new CBCBlockCipher(
                new BlowfishEngine()));
        blowfish.init(isEncrypt, new ParametersWithRandom(new KeyParameter(key)));
        try {
            original = processCipherBuffer(blowfish, original);
        } catch (InvalidCipherTextException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Cipher text in Blowfish encryption invalid. Stack trace is ");
            e.printStackTrace();
        } catch (DataLengthException e) {
            Log.w(ActivityConstants.LOG_APP_NAME, "Data length in Blowfish encryption invalid. Stack trace is ");
            e.printStackTrace();
        }
        return original;
    }

    /** PBKDF2 hashing method with SHA 256.
     * @param iterations should be >=100000 to ensure that the hash is secure.
     * @return A byte[] of the hashed String.
     * If an cryptography error occurs during encryption, original.getBytes() would be returned.
     * This function should not be used by itself outside of this class except for unit testing.**/
    public static byte[] pbkdf2(byte[] original, byte[] salt, int iterations) {
        PKCS5S2ParametersGenerator pbkdf2 = new PKCS5S2ParametersGenerator(new SHA256Digest());
        pbkdf2.init(original, salt, iterations);
        return ((KeyParameter) pbkdf2.generateDerivedParameters(original.length)).getKey();
    }
}
