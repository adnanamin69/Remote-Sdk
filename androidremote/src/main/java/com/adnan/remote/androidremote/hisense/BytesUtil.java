package com.adnan.remote.androidremote.hisense;

import java.util.Random;

public class BytesUtil {
    static final byte[] BYTES = new byte[128];
    private static final char[] CHARS_TABLES = "0123456789ABCDEF".toCharArray();

    public static int size(long j) {
        int i = 1;
        while (j > 127) {
            i++;
            j >>>= 7;
        }
        return i;
    }

    static {
        for (int i = 0; i < 10; i++) {
            byte[] bArr = BYTES;
            bArr[i + 48] = (byte) i;
            byte b = (byte) (i + 10);
            bArr[i + 65] = b;
            bArr[i + 97] = b;
        }
    }

    public static String toHexString(byte[] bArr) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        return toHexString(bArr, 0, bArr.length);
    }

    public static byte[] joinBytes(byte[]... bArr) {
        int i = 0;
        for (byte[] length : bArr) {
            i += length.length;
        }
        byte[] bArr2 = new byte[i];
        int i2 = 0;
        for (int i3 = 0; i3 < bArr.length; i3++) {
            byte[] bArr3 = bArr[i3];
            System.arraycopy(bArr3, 0, bArr2, i2, bArr3.length);
            i2 += bArr[i3].length;
        }
        return bArr2;
    }

    public static byte[] hexStringToByteArray(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char[] charArray = str.replace("\n", "").replace(" ", "").toUpperCase().toCharArray();
        int length = charArray.length / 2;
        byte[] bArr = new byte[length];
        int i = 0;
        for (int i2 = 0; i2 < length; i2++) {
            byte[] bArr2 = BYTES;
            int i3 = i + 1;
            byte b = bArr2[charArray[i] & 127];
            i += 2;
            bArr[i2] = (byte) ((b << 4) + bArr2[charArray[i3] & 127]);
        }
        return bArr;
    }


    public static String toHexString(byte[] bArr, int i, int i2) {
        if (bArr == null || bArr.length == 0) {
            return null;
        }
        char[] cArr = new char[(i2 * 2)];
        int i3 = 0;
        for (int i4 = i; i4 < i + i2; i4++) {
            byte b = bArr[i4];
            int i5 = i3 + 1;
            char[] cArr2 = CHARS_TABLES;
            cArr[i3] = cArr2[(b & 240) >>> 4];
            i3 += 2;
            cArr[i5] = cArr2[b & 15];
        }
        return new String(cArr);
    }

    public static int decode(byte[] bArr) {
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        do {
            long j = (long) (bArr[i] & 255);
            i2 = (int) (((long) i2) | ((127 & j) << i3));
            i3 += 7;
            i++;
            if ((j & 128) == 0 || i >= bArr.length) {
                return i2;
            }
            long j2 = (long) (bArr[i] & 255);
            i2 = (int) (((long) i2) | ((127 & j2) << i3));
            i3 += 7;
            i++;
            break;
        } while (i >= bArr.length);
        return i2;
    }

    public static byte[] intToBytes(int i) {
        return new byte[]{(byte) ((i >> 24) & 255), (byte) ((i >> 16) & 255), (byte) ((i >> 8) & 255), (byte) (i & 255)};
    }

    public static String randBytes(int i) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        byte[] bArr = new byte[0];
        for (int i2 = 0; i2 < i; i2++) {
            bArr = joinBytes(bArr, intToBytes(random.nextInt(8) + 1));
        }
        for (byte append : bArr) {
            sb.append(append);
        }
        return sb.toString();
    }

    public static byte[] removeLeadingNullBytes(byte[] bArr) {
        int i = 0;
        while (true) {
            boolean z = true;
            boolean z2 = i < bArr.length;
            if (bArr[i] != 0) {
                z = false;
            }
            if (!z2 || !z) {
                break;
            }
            i++;
        }
        byte[] bArr2 = new byte[(bArr.length - i)];
        for (int i2 = i; i2 < bArr.length; i2++) {
            bArr2[i2 - i] = bArr[i2];
        }
        return bArr2;
    }
}
