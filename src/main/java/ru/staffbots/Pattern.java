package ru.staffbots;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.webserver.WebServer;
import ru.staffbots.webserver.servlets.BaseServlet;
import ru.staffbots.webserver.servlets.ResourceServlet;
import ru.staffbots.windows.MainWindow;

import java.io.*;
import java.util.Properties;

/**
 * <b>Прототип обслуживающего робота</b><br>
 **/
public abstract class Pattern {

    // Название проекта,
    // Определяется параметром name в файле ресурсов properties
    // Используется в имени БД и в заголовках веб-интерфейса и главного окна
    public final static String projectName = "Staffbots";

    // Адрес веб-сайта проекта в www
    // Определяется параметром website в файле ресурсов properties
    // Используется при формировании ссылки на описание устройств
    public static String projectWebsite = "http://www.staffbots.ru";

    // Название решения,
    // Совпадает с названием дочернего класса, в нём же и определяется
    // Используется в имени БД, наименовании файлов .jar и .cfg, а так же в заголовках веб-интерфейса и главного окна
    public static String solutionName = "Solution";

    // Название версия проекта,
    // Определяется параметром version в файле ресурсов properties
    // Используется в наименовании файлов .jar и .cfg и в заголовке главного окна
    public static String projectVersion = "0.00";

    // Инициализация параметров
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    public static void propertiesInit(){
        // Читаем свойства проекта
        try {
            Properties property = new Properties();
            property.load(Resources.getAsStream("properties"));
            projectVersion = property.getProperty("staffbots.version", "");
            projectWebsite = property.getProperty("staffbots.website", "");
            Journal.add("Свойства проекта загружены", false);
        } catch (IOException exception) {
            Journal.add("Свойства проекта не загружены", NoteType.ERROR, exception, false);
        }
        // Имя исходного файла конфигурации, лежащего внутри jar-пакета
        String projectCfgFileName = "pattern.cfg"; // внутри jar-пакета
        // Имя внешнего файла конфигурации, лежащего рядом с jar-пакетом
        String solutionCfgFileName = projectName + "." + solutionName + "-" + projectVersion + ".cfg";
        try {
            // Извлекаем из jar-пакета файл конфигурации
            Resources.ExtractFromJar(projectCfgFileName, solutionCfgFileName);
            // Читаем свойства из извлечённого файла
            FileInputStream inputStream = new FileInputStream(Resources.getJarDirName() + solutionCfgFileName);
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

            WebServer.ADMIN = property.getProperty("web.admin", WebServer.ADMIN);
            WebServer.PASSWORD = property.getProperty("web.password", WebServer.PASSWORD);
            WebServer.HTTP_PORT = Integer.parseInt(property.getProperty("web.http_port", WebServer.HTTP_PORT.toString()));
            WebServer.HTTPS_PORT = Integer.parseInt(property.getProperty("web.https_port", WebServer.HTTPS_PORT.toString()));
            WebServer.key_store = property.getProperty("web.key_store", WebServer.key_store);
            WebServer.key_store_password = property.getProperty("web.key_store_password", WebServer.key_store_password);
            WebServer.key_manager_password = property.getProperty("web.key_manager_password", WebServer.key_manager_password);

            BaseServlet.site_bg_color = property.getProperty("web.site_bg_color", BaseServlet.site_bg_color);
            BaseServlet.main_bg_color = property.getProperty("web.main_bg_color", BaseServlet.main_bg_color);
            BaseServlet.page_bg_color = property.getProperty("web.page_bg_color", BaseServlet.page_bg_color);

            Journal.add("Конфигурация загружена", false);

        }catch (Exception exception){
            Journal.add("Конфигурация не загружена", NoteType.ERROR, exception, false);
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

