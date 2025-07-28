package com.urlshortener;

public class Base62Encoder {
    private static final String BASE62_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = 62;

    public static String encode(long id) {
        if (id == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        while (id > 0) {
            sb.append(BASE62_ALPHABET.charAt((int) (id % BASE)));
            id /= BASE;
        }
        
        return sb.reverse().toString();
    }

    public static long decode(String encoded) {
        long result = 0;
        for (int i = 0; i < encoded.length(); i++) {
            char c = encoded.charAt(i);
            int value = BASE62_ALPHABET.indexOf(c);
            if (value == -1) {
                throw new IllegalArgumentException("Invalid character in Base62 string: " + c);
            }
            result = result * BASE + value;
        }
        return result;
    }

    public static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * BASE);
            sb.append(BASE62_ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
