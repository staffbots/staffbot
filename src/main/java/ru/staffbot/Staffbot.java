package ru.staffbot;

import ru.staffbot.database.DBMS;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.webserver.WebServer;
import ru.staffbot.webserver.servlets.MainServlet;
import ru.staffbot.webserver.servlets.ResourceServlet;
import ru.staffbot.windows.MainWindow;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Properties;


/**
 ** <b>Обслуживающий робот</b><br>
 **/

public abstract class Staffbot{

    public static String solutionName;

    public static String solutionVersion;

    public static String projectName = "Staffbot";

    public static void propertiesInit(){
        try {
            Properties property = new Properties();
            property.load(Staffbot.class.getResourceAsStream("/properties"));
            solutionVersion = property.getProperty(solutionName.toLowerCase() + ".version", "");
            Journal.add("Свойства проекта загружены");
        } catch (IOException exception) {
            Journal.add("Ошибка чтения файла свойств проекта (properties):\n"+ exception.getMessage());
        }

        // Имя файла конфигурации
        String projectCfgFileName = "/" + projectName + ".cfg"; // внутри jar-пакета
        String solutionCfgFileName = "/" + solutionName + "-" + solutionVersion + ".cfg";
        System.out.println(projectCfgFileName);
        System.out.println(solutionCfgFileName);
        try {
            // Расположение файла конфигурации в той же директории, что и запускаемый jar-пакет
            String jarFileName = Staffbot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            System.out.println(jarFileName);
            String jarDirName = (new File(jarFileName)).getParent();
            System.out.println(jarDirName);
            File cfgFile = new File((new File(jarFileName)).getParent() + solutionCfgFileName);
            System.out.println(cfgFile.toPath());
            // Если файл конфигурации ещё не существует,
            if (!cfgFile.exists())
            try {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                System.out.println("Copy resource " + projectCfgFileName);
                InputStream inputStream = Staffbot.class.getResourceAsStream(projectCfgFileName);
                //InputStream inputStream = Staffbot.class.getResourceAsStream(resourceName);
                System.out.println("to " + cfgFile.getPath());
                FileOutputStream outputStream = new FileOutputStream(cfgFile.getPath());
                System.out.println("before write");
                outputStream.write(inputStream.readAllBytes());
                System.out.println("OK - coped");

                //File targetFile = new File(cfgFile.getPath());
                //OpenOption[] options = new OpenOption[] { StandardOpenOption.WRITE, StandardOpenOption.CREATE };
                //java.nio.file.Files.copy(inputStream,targetFile.toPath(),StandardCopyOption.REPLACE_EXISTING);
                //FileUtils.copyInputStreamToFile(initialStream, targetFile);
                } catch (Exception exception){

                System.out.println("Error of coping: " + exception.getMessage());
                System.out.println("StackTrace: " + exception.getStackTrace());

            }

            // Читаем файл конфигурации
            FileInputStream inputStream = new FileInputStream(cfgFile);
            Properties property = new Properties();
            property.load(inputStream);
            inputStream.close();
            // Применяем конфигурацию
            Database.DBMSystem = DBMS.getByName(property.getProperty("db.dbms", null), Database.DBMSystem);
            Database.SERVER = property.getProperty("db.server", Database.SERVER);
            Database.PORT = Integer.parseInt(property.getProperty("db.port", Database.PORT.toString()));
            Database.NAME = property.getProperty("db.name", (projectName + "_" + solutionName).toLowerCase());
            Database.USER = property.getProperty("db.user", Database.USER);
            Database.PASSWORD = property.getProperty("db.password", Database.PASSWORD);
            Database.DROP = Boolean.parseBoolean(property.getProperty("db.drop", Database.DROP.toString()));
            Devices.USED = Boolean.parseBoolean(property.getProperty("pi.used", Devices.USED.toString()));
            WebServer.PORT = Integer.parseInt(property.getProperty("web.port", WebServer.PORT.toString()));
            WebServer.ADMIN = property.getProperty("web.admin", WebServer.ADMIN);
            WebServer.PASSWORD = property.getProperty("web.password", WebServer.PASSWORD);
            MainServlet.main_bg_color = property.getProperty("web.main_bg_color", MainServlet.main_bg_color);
            MainServlet.page_bg_color = property.getProperty("web.page_bg_color", MainServlet.page_bg_color);

            Journal.add("Конфигурация загружена");

        }catch (Exception exception){
            Journal.add("Ошибка чтения файла конфигурации:\n"+ exception.getMessage());
        }
    }

    public static void databaseInit(){
        Database.init();
    }

    public static void webserverInit(){
        WebServer.init();
    }

    public static void windowInit(){
        MainWindow.init(projectName + ":" + solutionName + "-" + solutionVersion);
    }

}

