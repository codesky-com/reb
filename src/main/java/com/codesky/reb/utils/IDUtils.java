package com.codesky.reb.utils;//


import java.util.UUID;

public class IDUtils {
    private static char[] CHARS = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
    private static Long id = 0L;

    public IDUtils() {
    }

    public static String getShortID() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = getID();

        for(int i = 0; i < 8; ++i) {
            String str = uuid.substring(i * 4, i * 4 + 4);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(CHARS[x % 62]);
        }

        return shortBuffer.toString();
    }

    public static String toString(long i, int radix) {
        if (radix < 2 || radix > 36) {
            radix = 10;
        }

        if (radix == 10) {
            return Long.toString(i);
        } else {
            char[] buf = new char[65];
            int charPos = 64;
            boolean negative = i < 0L;
            if (!negative) {
                i = -i;
            }

            while(i <= (long)(-radix)) {
                buf[charPos--] = CHARS[(int)(-(i % (long)radix))];
                i /= (long)radix;
            }

            buf[charPos] = CHARS[(int)(-i)];
            if (negative) {
                --charPos;
                buf[charPos] = '-';
            }

            return new String(buf, charPos, 65 - charPos);
        }
    }

    public static synchronized String getStartShortID() {
        id = id + 1L;
        return toString(id, CHARS.length);
    }

    public static String getID() {
        UUID ret = UUID.randomUUID();
        return ret.toString().replaceAll("-", "");
    }
}
