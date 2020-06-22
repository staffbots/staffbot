package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo.BoardType;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.ParsableProperties;
import ru.staffbots.tools.devices.CoolingDevice;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.webserver.WebServer;
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

    private static BoardType boardType = BoardType.UNKNOWN;

    public static BoardType getBoardType() {
        return boardType;
    }

    public static void setBoardType(BoardType value) {
        boardType = value;
    }

    /**
     * <b>Project name</b><br>
     * Value is current class name - {@code Staffbot}
     **/
    private static final String projectName = MethodHandles.lookup().lookupClass().getSimpleName();

    public static String getProjectName() {
        return projectName;
    }

    /**
     * Название версия проекта,
     * Определяется параметром version в файле ресурсов properties
     * Используется в наименовании файлов .jar и .cfg и в заголовке главного окна
     */
    private static String projectVersion = "0.00";


    /**
     * <b>Solution name</b><br>
     * Value is name of inherited class, set by default is <em>Solution</em><br>
     * Initialized in {@code solutionInit()}-method
     **/
    private static String solutionName = "Solution";

    public static String getSolutionName() {
        return solutionName;
    }

    public static void setSolutionName(String value) {
        solutionName = value;
    }

    public static String getShortName(){
        return projectName + "." + solutionName;
    }

    public static String getFullName(){
        return getShortName() + "-" + projectVersion;
    }

    /**
     * Адрес веб-сайта проекта в www
     * Определяется параметром website в файле ресурсов properties
     * Используется при формировании ссылки на описание устройств
     */
    private static String projectWebsite = "http://www.staffbots.ru";

    public static String getProjectWebsite() {
        return projectWebsite;
    }

    public static void solutionInit(Device[] devices, Lever[] levers, Task[] tasks) {
        solutionInit(devices, (Object[]) levers, tasks);
    }

    public static void solutionInit(Device[] devices, Object[] levers, Task[] tasks) {
        solutionInit(
                ()->{
                    Levers.addObjects(levers); // Инициализируем список элементов управления
                    Devices.addDevices(devices); // Инициализируем список устройств
                    Tasks.init(tasks);
                }
        );
    }

    public static void solutionInit(Runnable solutionInitAction){
        Staffbot.solutionName = solutionName;
        propertiesInit(); // Загружаем свойства из cfg-файла
        Database.connect(); // Подключаемся к базе данных
        solutionInitAction.run(); // Инициализируем решение (Levers, Devices and Tasks)
        Database.dropUnusingTables();
        WebServer.getInstance().init(); // Запускаем веб-сервер
        MainWindow.init(getFullName()); // Открываем главное окно приложения
    }

    /**
     * Инициализация параметров
     * Вызывается при запуске приложения в определённом порядке с прочими инициализациями
     */
    private static void propertiesInit(){
        // Читаем свойства проекта
        try {
            Properties properties = new Properties();
            properties.load(Resources.getAsStream("properties"));
            projectVersion = properties.getProperty("staffbot.version", projectVersion);
            projectWebsite = properties.getProperty("staffbot.website", projectWebsite);
            String[] languageCodes = properties.getProperty("staffbot.languages", "").split(",");
            for (String code: languageCodes)
                Languages.put(code);
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
            Resources.getAsFile(projectCfgFileName, solutionCfgFileName, false);
            // Читаем свойства из извлечённого файла
            FileInputStream inputStream = new FileInputStream(Resources.getJarDirName() + solutionCfgFileName);

            ParsableProperties properties = new ParsableProperties();
            properties.load(inputStream);
            // Применяем конфигурацию
            WebServer webServer = WebServer.getInstance();
            webServer.setAdminLogin(properties.getProperty("web.admin_login"));
            webServer.setAdminPassword(properties.getProperty("web.admin_password"));
            webServer.setHttpPort(properties.getIntegerProperty("web.http_port"));
            webServer.setHttpsPort(properties.getIntegerProperty("web.https_port"));
            webServer.setHttpUsed(properties.getBooleanProperty("web.http_used"));
            webServer.setKeyStore(properties.getProperty("web.key_store"));
            webServer.setStorePassword(properties.getProperty("web.store_password"));
            webServer.setManagerPassword(properties.getProperty("web.manager_password"));
            webServer.setUpdateDelay(properties.getIntegerProperty("web.update_delay"));

            Database.setServer(properties.getProperty("db.server"));
            Database.setPort(properties.getIntegerProperty("db.port"));
            Database.setName((projectName + "_" + solutionName).toLowerCase());
            Database.setName(properties.getProperty("db.name"));
            Database.setUser(properties.getProperty("db.user"));
            Database.setPassword(properties.getProperty("db.password"));
            Database.setDrop(properties.getBooleanProperty("db.drop"));

            int fanPin = properties.getIntegerProperty("pi.fan_pin", -1);
            double cpuTemperature = properties.getDoubleProperty("pi.cpu_temperature", 50);
            CoolingDevice.init(RaspiPin.getPinByAddress(fanPin), cpuTemperature);

            MainWindow.setFrameUsed(properties.getBooleanProperty("ui.frame_used"));
            webServer.setColorSchema(properties.getProperty("ui.main_color"));
            webServer.setFontFamily(properties.getProperty("ui.font-family"));

            Journal.add(false, "init_configs");

        }catch (Exception exception){
            Journal.add(NoteType.ERROR, false, "init_configs");
            exception.printStackTrace();
        }
    }

}
