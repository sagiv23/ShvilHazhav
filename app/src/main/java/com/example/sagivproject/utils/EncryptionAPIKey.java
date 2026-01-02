package com.example.sagivproject.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionAPIKey {
    //קידוד
    public static String encode(String apiKey) {
        return Base64.getEncoder().encodeToString(apiKey.getBytes(StandardCharsets.UTF_8));
    }

    //פיענוח
    public static String decode(String encodedKey) {
        byte[] decodedBytes = Base64.getDecoder().decode(encodedKey);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
