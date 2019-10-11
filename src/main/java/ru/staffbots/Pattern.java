package ru.staffbots;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.webserver.WebServer;
import ru.staffbots.webserver.servlets.BaseServlet;
import ru.staffbots.windows.MainWindow;

import java.io.*;
import java.util.Properties;

/**
 * <b>Прототип обслуживающего робота</b>
 * - абстрактный класс, на базе которого реализуются классы для конкретных решений.
 * Каждое такое решение (класс-наследник <b>{@code Pattern}</b>) представляет из себя <b>обслуживающего робота</b>
 * по автоматизации определённого процесса с определёнными переферийными устройствами и рычагами управления.
 * Упомянутая определённость (рычагов управления, переферийных устройств и методов их взаимодействия)
 * полностью описывается в одном единственном вышеупомянутом классе <b>обслуживающего робота</b>.
 * Таких наследников может быть сколько угодно и каждый из них будет самостоятельной системой автоматизации.
 * На практике же как правило решается только одна задача и для её решения достаточно одного <b>обслуживающего робота</b>.
 *
 * <br><br><b>Простейший пример с миганием светодиода на пине {@code GPIO_01}</b>
 *
 * <br></><code>
 * <br>public class <b>{@link Staffbot}</b> extends Pattern {
 * <blockquote>
 *         // Точка входа приложения
 * <br>    public static void main(String[] args) {
 * <blockquote>// ВНИМАНИЕ! Порядок инициализаций менять не рекомендуется
 * <br>        // Определяем наименование решения по названию текущего класса
 * <br>        solutionName = new Object(){}.getClass().getEnclosingClass().getSimpleName();
 * <br>        propertiesInit(); // Загружаем свойства из cfg-файла
 * <br>        databaseInit();   // Подключаемся к базе данных
 * <br>        leversInit();     // Инициализируем список элементов управления
 * <br>        devicesInit();    // Инициализируем список устройств
 * <br>        tasksInit();      // Инициализируем список заданий
 * <br>        webserverInit();  // Запускаем веб-сервер
 * <br>        windowInit();     // Открываем главное окно приложения
 * </blockquote>
 *         }
 * <br>
 * <br>    /////////////////////////////////////////////////////////////
 * <br>    // Описание рычагов управления
 * <br>    static LongLever frequencyLever = new LongLever("frequency",
 * <br>          "Частота мигания светодиода, Гц", ValueMode.TEMPORARY, 2, 0.5);
 * <br>
 * <br>    // Инициализация рычагов управления
 * <br>    static void leversInit() {
 * <blockquote>Levers.init(frequencyLever);
 * <br>        Journal.add("Рычаги управления успешно проинициализированы");
 * </blockquote>
 *     }
 * <br>
 * <br>    /////////////////////////////////////////////////////////////
 * <br>    // Описание переферийных устройств
 * <br>    static LedDevice ledDevice = new LedDevice("led",
 * <br>        "Светодиод", RaspiPin.GPIO_01, false);
 * <br>
 * <br>    // Инициализация переферийных устройств
 * <br>    static void devicesInit() {
 * <blockquote>
 *              Devices.init(ledDevice);
 * </blockquote>
 *     }
 * <br>
 * <br>    /////////////////////////////////////////////////////////////
 * <br>    // Описание заданий автоматизации
 * <br>    static Task ledFlashingTask = new Task( "Мигание светодиода",
 * <blockquote>() -> {// Расчёт задержки перед следующим запуском задания в миллисекундах
 * <blockquote>   return Math.round(1000/frequencyLever.getValue());
 * </blockquote>    },
 * <br>        () -> {// Команды выполнения задания
 * <blockquote>    ledDevice.set(true); // Включаем светодиод
 * <br>            try { Thread.sleep(Math.round(1000/frequencyLever.getValue())); } // Ждём
 * <br>            catch (Exception exception) { Journal.add("Мигание светодиода прервано", NoteType.WRINING); }
 * <br>            ledDevice.set(false); // Выключаем светодиод
 * </blockquote>    }
 * </blockquote>    );
 * <br>
 * <br>    // Инициализация заданий автоматизации
 * <br>    static void tasksInit() {
 * <blockquote>
 *              Tasks.init(ledFlashingTask);
 * </blockquote> }
 * </blockquote> }
 * </code>
 */
public abstract class Pattern {

    /** Название проекта
    * <br>Определяется параметром <b>name</b> в исходном файле ресурсов <b>properties</b> перед компиляцией проекта
    * <br>Используется в имени БД, в заголовках веб-интерфейса и главного окна
    */
    public final static String projectName = "Staffbots";

    /** Адрес веб-сайта проекта в www
    * Определяется параметром website в файле ресурсов properties
    * Используется при формировании ссылки на описание устройств
    */
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
            WebServer.HTTP_USED = Boolean.parseBoolean(property.getProperty("web.http_used", WebServer.HTTP_USED.toString()));
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
