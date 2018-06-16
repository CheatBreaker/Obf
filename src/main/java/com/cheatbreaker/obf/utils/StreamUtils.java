package com.cheatbreaker.obf.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {

    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }
}
