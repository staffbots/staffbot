package ru.staffbots;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo.BoardType;
import ru.staffbots.database.Database;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
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


/**
 * Parent for all staffbots<br><br>
 * an abstract class on the basis of which classes are implemented for specific solutions.<br>
 * Each such solution (the class inheritor <b>{@code Staffbot}</b>) is a serving robot (<b>staff bot</b>)
 * for automating a specific process with certain peripheral devices and control levers.
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

    public static String getProjectVersion() {
        return projectVersion;
    }

    public static void setProjectVersion(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        projectVersion = value.trim();
    }

    public static void setProjectWebsite(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        projectWebsite = value.trim();
    }

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
        return solutionName + "-" + projectVersion;
    }

    public static String getFullName(){
        return projectName + "." + getShortName();
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

    public static void initiateSolution(Device[] devices, Lever[] levers, Task[] tasks) {
        initiateSolution(devices, (Object[]) levers, tasks);
    }

    public static void initiateSolution(Device[] devices, Object[] levers, Task[] tasks) {
        initiateSolution(
                ()->{
                    Levers.addObjects(levers); // Инициализируем список элементов управления
                    Devices.addDevices(devices); // Инициализируем список устройств
                    Tasks.init(tasks);
                }
        );
    }

    public static void initiateSolution(Runnable solutionInitAction){
        // execution order is important
        Staffbot.applyProperties();
        Staffbot.loadCongigure();
        Database.connect();
        solutionInitAction.run();
        WebServer.start();
        MainWindow.open(getFullName());
        Database.dropUnusingTables();
    }

    /**
     * Apply properties from jar-package resources
     */
    private static void applyProperties() {
        ParsableProperties properties = new ParsableProperties();
        try {
            properties.load(Resources.getAsStream("properties")); // load properties from jar-package resources
        } catch (Exception exception) {
            Journal.add(NoteType.ERROR, false, "apply_properties");
            exception.printStackTrace();
            System.exit(0);
        }
        setProjectVersion(properties.getProperty("staffbot.version"));
        setProjectWebsite(properties.getProperty("staffbot.website"));
        Languages.put(properties.getStringsProperty("staffbot.languages"));
        Journal.add(false, "apply_properties");
    }

    /**
     * Load congigure from cfg-file
     */
    private static void loadCongigure(){
        ParsableProperties properties = new ParsableProperties();
        String projectCfgFileName = "cfg/pattern.cfg"; // default cfg-file from jar-package resources
        String solutionCfgFileName = getFullName() + ".cfg"; // specific cfg-file from jar-directory
        Resources.getAsFile(projectCfgFileName, solutionCfgFileName, false); // Extract cfg-file from jar-package
        try {
            FileInputStream inputStream = new FileInputStream(Resources.getJarDirName() + solutionCfgFileName);
            properties.load(inputStream);
        }catch (Exception exception){
            Journal.add(NoteType.ERROR, false, "init_configs");
            exception.printStackTrace();
            return;
        }
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
        Database.setName(properties.getProperty("db.name", (projectName + "_" + solutionName).toLowerCase()));
        Database.setUser(properties.getProperty("db.user"));
        Database.setPassword(properties.getProperty("db.password"));
        Database.setDrop(properties.getBooleanProperty("db.drop"));

        int fanPin = properties.getIntegerProperty("pi.fan_pin", -1);
        double cpuTemperature = properties.getDoubleProperty("pi.cpu_temperature", 50);
        CoolingDevice.init(RaspiPin.getPinByAddress(fanPin), cpuTemperature);

        MainWindow.setFrameUsed(properties.getBooleanProperty("ui.frame_used"));
        webServer.setColorSchema(properties.getProperty("ui.main_color"));
        webServer.setFontFamily(properties.getProperty("ui.font_family"));

        Journal.add(false, "init_configs");

    }

}
