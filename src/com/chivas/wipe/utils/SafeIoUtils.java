package com.chivas.wipe.utils;

import java.io.Closeable;
import java.io.IOException;

public class SafeIoUtils {

    private SafeIoUtils() {
        //
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
