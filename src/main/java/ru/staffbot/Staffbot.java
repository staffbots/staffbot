package ru.staffbot;

import ru.staffbot.database.DBMS;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.webserver.WebServer;
import ru.staffbot.webserver.servlets.MainServlet;
import ru.staffbot.windows.MainWindow;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        try {
            // Расположение файла конфигурации в той же директории, что и запускаемый jar-пакет
            File cfgFile = new File((new File(Staffbot.class.getProtectionDomain().getCodeSource().getLocation().getPath())).getParent() + solutionCfgFileName);
            // Если файл конфигурации ещё не существует,
            if (!cfgFile.exists())
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                Files.copy(Staffbot.class.getResourceAsStream(projectCfgFileName), cfgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

