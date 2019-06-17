package ru.staffbot;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.webserver.WebServer;
import ru.staffbot.webserver.servlets.MainServlet;
import ru.staffbot.windows.MainWindow;

import java.io.*;
import java.util.Properties;


/**
 ** <b>Обслуживающий робот</b><br>
 **/

public abstract class Staffbot{

    public static String solutionName;

    public static String projectVersion;

    public static String projectName = new Object(){}.getClass().getEnclosingClass().getSimpleName(); //"Staffbot"

    public static void propertiesInit(){
        // Считываем версию
        try {
            Properties property = new Properties();
            property.load(Staffbot.class.getResourceAsStream("/properties"));
            projectVersion = property.getProperty(projectName.toLowerCase() + ".version", "");
            Journal.add("Свойства проекта загружены");
        } catch (IOException exception) {
            Journal.add("Ошибка чтения файла свойств проекта (properties):\n"+ exception.getMessage());
        }
        // Имя исходного файла конфигурации, лежащего внутри jar-пакета
        String projectCfgFileName = "/" + projectName.toLowerCase() + ".cfg"; // внутри jar-пакета
        // Имя внешнего файла конфигурации, лежащего рядом с jar-пакетом
        String solutionCfgFileName = "/" + solutionName + "-" + projectVersion + ".cfg";
        try {
            // Полный путь до jar-пакета
            String jarFileName = Staffbot.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            // Полный путь до каталога jar-пакета
            String jarDirName = (new File(jarFileName)).getParent();
            // Полный путь до внешнего файла конфигурации
            File cfgFile = new File(jarDirName + solutionCfgFileName);
            // Если таковой файл конфигурации ещё не существует,
            if (!cfgFile.exists())
            try {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                InputStream inputStream = Staffbot.class.getResourceAsStream(projectCfgFileName);
                FileOutputStream outputStream = new FileOutputStream(cfgFile.getPath());

                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                outputStream.write(bytes);

//                outputStream.write(inputStream.readAllBytes());


            } catch (Exception exception){
                System.out.println("Error of coping: " + exception.getMessage());
                System.out.println("StackTrace: " + exception.getStackTrace());
            }

            // Читаем свойства из (внешнего) файла конфигурации
            FileInputStream inputStream = new FileInputStream(cfgFile);
            Properties property = new Properties();
            property.load(inputStream);
            inputStream.close();
            // Применяем конфигурацию
            Database.SERVER = property.getProperty("db.server", Database.SERVER);
            Database.PORT = Integer.parseInt(property.getProperty("db.port", Database.PORT.toString()));
            Database.NAME = property.getProperty("db.name", (projectName + "_" + solutionName).toLowerCase());
            Database.USER = property.getProperty("db.user", Database.USER);
            Database.PASSWORD = property.getProperty("db.password", Database.PASSWORD);
            Database.DROP = Boolean.parseBoolean(property.getProperty("db.drop", Database.DROP.toString()));
            MainWindow.USED = Boolean.parseBoolean(property.getProperty("gui.used", MainWindow.USED.toString()));
            Devices.USED = Devices.isRaspbian() && Boolean.parseBoolean(property.getProperty("pi.used", Devices.USED.toString()));
            WebServer.PORT = Integer.parseInt(property.getProperty("web.port", WebServer.PORT.toString()));
            WebServer.ADMIN = property.getProperty("web.admin", WebServer.ADMIN);
            WebServer.PASSWORD = property.getProperty("web.password", WebServer.PASSWORD);
            MainServlet.site_bg_color = property.getProperty("web.site_bg_color", MainServlet.site_bg_color);
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
        MainWindow.init(projectName + ":" + solutionName + "-" + projectVersion);
    }

}

