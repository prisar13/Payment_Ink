package com.util;
import java.security.SecureRandom;

public class IdGeneratorUtil {
    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateUTR() {
        StringBuilder sb = new StringBuilder("UTR-");

        for (int i = 0; i < 10; i++) {
            int index = RANDOM.nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(index));
        }

        String utr = sb.toString();
        System.out.println("Generated ID: " + utr);
        return utr;
    }
}
