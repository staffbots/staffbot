package ru.staffbots.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Resources {

    public static InputStream getAsStream(String resourceName){
        return Resources.class.getResourceAsStream(resourceName);
    }

    public static byte[] getAsBytes(String resourceName){
        try {
            InputStream inputStream = getAsStream(resourceName);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException exception) {
            return new byte[0];
        }
    }
}
