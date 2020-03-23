package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.ParsableProperties;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.devices.CoolingDevice;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.webserver.WebServer;
import ru.staffbots.webserver.servlets.BaseServlet;
import ru.staffbots.windows.MainWindow;

import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.util.Properties;

/**
 * Parent for all staffbots<br><br>
 * an abstract class on the basis of which classes are implemented for specific solutions.<br>
 * Each such solution (the class inheritor <b>{@code Staffbot}</b>) is a serving robot (<b>staff bot</b>)
 * for automating a specific process with certain peripheral devices and control levers.<br>
 * <br><b>The simple example of a blinking LED</b>
 *
 * <br><code>
 * <br>public class <b>{@link Sample}</b> extends Staffbot {
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
public abstract class Staffbot {


    public static SystemInfo.BoardType boardType = SystemInfo.BoardType.UNKNOWN;

    /** <b>Project name</b><br>
     * Value is current class name - {@code Staffbot}
     **/
    public static final String projectName = MethodHandles.lookup().lookupClass().getSimpleName();

    /** <b>Solution name</b><br>
     * Value is name of inherited class, set by default is <em>Solution</em><br>
     * Initialized in {@code solutionInit()}-method
     **/
    public static String solutionName = "Solution";

    // Название версия проекта,
    // Определяется параметром version в файле ресурсов properties
    // Используется в наименовании файлов .jar и .cfg и в заголовке главного окна
    public static String projectVersion = "0.00";

    public static String getShortName(){
        return projectName + "." + solutionName;
    }

    public static String getFullName(){
        return getShortName() + "-" + projectVersion;
    }

    /** Адрес веб-сайта проекта в www
     * Определяется параметром website в файле ресурсов properties
     * Используется при формировании ссылки на описание устройств
     */
    public static String projectWebsite = "http://www.staffbots.ru";

    public static void solutionInit(Device[] devices, Lever[] levers, Task[] tasks) {
        solutionInit(devices, (Object[]) levers, tasks);
    }

    public static void solutionInit(Device[] devices, Object[] levers, Task[] tasks) {
        solutionInit(
                ()->{
                    Levers.init(levers); // Инициализируем список элементов управления
                    Devices.init(devices); // Инициализируем список устройств
                    Tasks.init(tasks);
                }
        );
    }

    public static void solutionInit(Runnable solutionInitAction){
        Staffbot.solutionName = solutionName;
        Translator.init(); // Инициализируем мультиязычность
        propertiesInit(); // Загружаем свойства из cfg-файла
        Database.init(); // Подключаемся к базе данных
        solutionInitAction.run(); // Инициализируем решение (Levers, Devices and Tasks)
        WebServer.init(); // Запускаем веб-сервер
        MainWindow.init(getFullName()); // Открываем главное окно приложения
        Database.dropUnusingTables();
    }

    // Инициализация параметров
    // Вызывается при запуске приложения в определённом порядке с прочими инициализациями
    private static void propertiesInit(){
        // Читаем свойства проекта
        try {
            Properties property = new Properties();
            property.load(Resources.getAsStream("properties"));
            projectVersion = property.getProperty("staffbot.version", "");
            projectWebsite = property.getProperty("staffbot.website", "");
            Journal.add(false, "init_properties");
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, false, "init_properties");
            exception.printStackTrace();
        }
        // Имя исходного файла конфигурации, лежащего внутри jar-пакета
        String projectCfgFileName = "pattern.cfg"; // внутри jar-пакета
        // Имя внешнего файла конфигурации, лежащего рядом с jar-пакетом
        String solutionCfgFileName = getFullName() + ".cfg";
        try {
            // Извлекаем из jar-пакета файл конфигурации
            Resources.getAsFile(projectCfgFileName, solutionCfgFileName);
            // Читаем свойства из извлечённого файла
            FileInputStream inputStream = new FileInputStream(Resources.getJarDirName() + solutionCfgFileName);

            ParsableProperties property = new ParsableProperties();
            property.load(inputStream);
            inputStream.close();

            // Применяем конфигурацию

            Database.SERVER = property.getProperty("db.server", Database.SERVER);
            Database.PORT = property.getIntegerProperty("db.port", Database.PORT);
            Database.NAME = property.getProperty("db.name", (projectName + "_" + solutionName).toLowerCase());
            Database.USER = property.getProperty("db.user", Database.USER);
            Database.PASSWORD = property.getProperty("db.password", Database.PASSWORD).trim();
            Database.DROP = property.getBooleanProperty("db.drop", Database.DROP);

            int fanPin = property.getIntegerProperty("pi.fan_pin", -1);
            double cpuTemperature = property.getDoubleProperty("pi.cpu_temperature", 50);
            Devices.coolingDevice = new CoolingDevice(RaspiPin.getPinByAddress(fanPin), cpuTemperature);

            MainWindow.frameUsed = property.getBooleanProperty("ui.frame_used", MainWindow.frameUsed);

            WebServer.defaultAdmin = property.getProperty("web.default_admin", WebServer.defaultAdmin);
            WebServer.adminPassword = property.getProperty("web.admin_password", WebServer.adminPassword);
            WebServer.httpPort = property.getIntegerProperty("web.http_port", WebServer.httpPort);
            WebServer.httpUsed = property.getBooleanProperty("web.http_used", WebServer.httpUsed);
            WebServer.httpsPort = property.getIntegerProperty("web.https_port", WebServer.httpsPort);
            WebServer.keyStore = property.getProperty("web.key_store", WebServer.keyStore);
            WebServer.storePassword = property.getProperty("web.store_password", WebServer.storePassword);
            WebServer.managerPassword = property.getProperty("web.manager_password", WebServer.managerPassword);
            WebServer.updateDelay = property.getIntegerProperty("web.update_delay", WebServer.updateDelay);

            BaseServlet.siteColor = property.getProperty("web.site_color", BaseServlet.siteColor);
            BaseServlet.mainColor = property.getProperty("web.main_color", BaseServlet.mainColor);
            BaseServlet.pageColor = property.getProperty("web.page_color", BaseServlet.pageColor);

            Journal.add(false, "init_configs");

        }catch (Exception exception){
            Journal.add(NoteType.ERROR, false, "init_configs");
            exception.printStackTrace();
        }
    }

}
