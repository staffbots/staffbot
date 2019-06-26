package ru.staffbot;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.tools.Converter;
import ru.staffbot.tools.devices.Devices;
import ru.staffbot.webserver.WebServer;
import ru.staffbot.webserver.servlets.MainServlet;
import ru.staffbot.webserver.servlets.ResourceServlet;
import ru.staffbot.windows.MainWindow;

import java.io.*;
import java.util.Properties;


/**
 * <b>Обслуживающий робот</b><br>
 * Родительский класс для всех приложений (решений) Staffbot
 **/

public abstract class Staffbot{

    // Название решения,
    // Совпадает с названием дочернего класса, в нём же и определяется
    // Используется в имени БД, наименовании файлов .jar и .cfg, а так же в заголовках веб-интерфейса и главного окна
    public static String solutionName;

    // Название версия проекта,
    // Определяется параметром <projectName>.version в файле ресурсов properties
    // Используется в наименовании файлов .jar и .cfg и в заголовке главного окна
    public static String projectVersion;

    // Название проекта,
    // Совпадает с названием этого класса (т.е. Staffbot), в нём же и определяется (см. ниже)
    // Используется в имени БД и в заголовках веб-интерфейса и главного окна
    public static String projectName = new Object(){}.getClass().getEnclosingClass().getSimpleName(); //"Staffbot"

    // Инициализация параметров
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    public static void propertiesInit(){
        // Считываем версию проекта
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
            if (!cfgFile.exists()) {
                // то копируем его из ресурсов, заархивированных внутри jar-файла
                FileOutputStream outputStream = new FileOutputStream(cfgFile.getPath());
                outputStream.write(
                        Converter.inputStreamToBytes(
                                ResourceServlet.class.getResourceAsStream(
                                        projectCfgFileName)));
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
            Journal.add("Конфигурация не загружена", NoteType.ERROR, exception);
        }
    }

    // Инициализация БД
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    public static void databaseInit(){
        Database.init();
    }

    // Инициализация веб-сервера
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    public static void webserverInit(){
        WebServer.init();
    }

    // Инициализация главного окна приложения
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    public static void windowInit(){
        MainWindow.init(projectName + ":" + solutionName + "-" + projectVersion);
    }

}

