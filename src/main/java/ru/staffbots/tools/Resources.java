package ru.staffbots.tools;

import ru.staffbots.Pattern;

import java.io.*;

public class Resources {

    public static String getJarDirName(){
        // Полный путь до jar-пакета
        String jarFileName = Resources.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // Полный путь до каталога jar-пакета
        return (new File(jarFileName)).getParent();
    }

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

    // Извлекает ресурс resourceName из jar-пакета в тот же каталог, где находится сам пакет
    // Взвращает полный путь до файла targetName, или null если что-то пошло не так
    public static String ExtractFromJar(String resourceName){
        return ExtractFromJar(resourceName, resourceName);
    }

    // Извлекает ресурс resourceName из jar-пакета в файл targetName в тот же каталог, где находится сам пакет
    // Взвращает полный путь до файла targetName, или null если что-то пошло не так
    public static String ExtractFromJar(String resourceName, String targetName){
        try {
            // Полный путь до внешнего файла
            File targetFile = new File(getJarDirName() + targetName);
            // Если таковой файл ещё не существует,
            if (!targetFile.exists()) {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                FileOutputStream outputStream = new FileOutputStream(targetFile.getPath());
                outputStream.write(getAsBytes(resourceName));
            }
            return targetFile.getPath();
        } catch (Exception exception) {
            return null;
        }
    }
}
