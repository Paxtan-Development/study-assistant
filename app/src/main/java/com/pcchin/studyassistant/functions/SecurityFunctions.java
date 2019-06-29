package com.pcchin.studyassistant.functions;

import androidx.annotation.NonNull;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.engines.BlowfishEngine;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.MessageDigest;
import java.security.Security;

/** Functions used in hashing, encryption etc. **/
public class SecurityFunctions {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    /** Hashing method used in the passwords that prevent notes from being edited.
     * No need to be too secure as they can be easily found when exported. **/
    public static String notesHash(String original) {
        byte[] originalByte;
        // 1) SHA
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest shaDigest = new SHA3.Digest512();
        originalByte = shaDigest.digest(original.getBytes());

        // 2) Blowfish
        BlowfishEngine blowfishEngine = new BlowfishEngine();
        blowfishEngine.init(true,  new KeyParameter(original.getBytes()));
        byte[] responseByte = new byte[originalByte.length];
        blowfishEngine.processBlock(originalByte, original.length(), responseByte, 0);

        return bytesToHex(responseByte);
    }

    /** Convert bytes to hex. **/
    @NonNull
    private static String bytesToHex(@NonNull byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
