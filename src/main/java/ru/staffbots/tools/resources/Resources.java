package ru.staffbots.tools.resources;

import java.io.*;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// Контейнер методов для работы с ресурсами из jar-файла
public class Resources {

    public static String getJarDirName(){
        // Полный путь до jar-пакета
        String jarFileName = Resources.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        // Полный путь до каталога jar-пакета
        return (new File(jarFileName)).getParent() + "/";
    }

    public static InputStream getAsStream(String resourceName){
        return Resources.class.getResourceAsStream("/" + resourceName);
    }

    private static File getAsFile(String resourceName, String targetName, boolean isDir){
        try {
            // Полный путь до внешнего файла
            File targetFile = new File(getJarDirName() + targetName);
            // Если таковой файл ещё не существует,
            if (!targetFile.exists()) {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                FileOutputStream outputStream = new FileOutputStream(targetFile.getPath());
                outputStream.write(getAsBytes(resourceName));
            }
            return targetFile;
        } catch (Exception exception) {
            return null;
        }
    }

    // Извлекает ресурс resourceName из jar-пакета в файл targetName в тот же каталог, где находится сам пакет
    // Взвращает полный путь до файла targetName, или null если что-то пошло не так
    public static File getAsFile(String resourceName, String targetName) {
        return getAsFile(resourceName, targetName, false);
    }

    // Извлекает ресурс resourceName из jar-пакета в тот же каталог, где находится сам пакет
    // Взвращает полный путь до извлечённого файла, или null если что-то пошло не так
    public static File getAsFile(String resourceName){
        return getAsFile(resourceName, resourceName);
    }

    public static File[] getAsFiles(String resourceName){
       return getAsFiles(resourceName, resourceName);
    }

    public static File[] getAsFiles(String resourceName, String targetName){
        File directory = getAsFile(resourceName, targetName, true);
        ArrayList<File> result = new ArrayList();
        for(File file: directory.listFiles())
            result.add(file);
        return (File[]) result.toArray();
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