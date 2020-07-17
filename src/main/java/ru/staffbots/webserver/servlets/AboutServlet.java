package ru.staffbots.webserver.servlets;

import com.pi4j.io.gpio.Pin;
import com.pi4j.system.SystemInfo;
import ru.staffbots.Staffbot;
import ru.staffbots.database.Database;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.devices.CoolingDevice;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.i2c.I2CBusDevice;
import ru.staffbots.tools.devices.drivers.network.NetworkDevice;
import ru.staffbots.tools.devices.drivers.spi.SpiBusDevice;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 */
public class AboutServlet extends BaseServlet {

    public AboutServlet(AccountService accountService) {
        super(PageType.ABOUT, accountService);
    }

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

        if (isAccessDenied(request, response)) return;
        Language language = accountService.getUserLanguage(request);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        pageVariables.put("board_info", getBoardInfo(language));
        pageVariables.put("osname_value", SystemInformation.osName + " " + SystemInformation.osRelease);
        pageVariables.put("osversion_value", SystemInformation.osVersion);
        pageVariables.put("javaversion_value", System.getProperty("java.version"));
        pageVariables.put("dbserver_value", Database.getServer());
        pageVariables.put("dbmsystem_value", Database.getDBMSystem());
        pageVariables.put("dbname_value", Database.getName());
        pageVariables.put("project_value", Staffbot.getProjectName());
        pageVariables.put("solution_value", Staffbot.getShortName());
        pageVariables.put("website_link", Staffbot.getProjectWebsite());
        pageVariables.put("device_list", getDeviceList(language));
        pageVariables.put("dberror_message", Database.connected() ? "" : Database.getException().getMessage());
        super.doGet(request, response, getContent(pageVariables));
    }

    private String getDeviceList(Language language) {
        String context = "";
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        if (Devices.getList().size() > 0)
            context += fillTemplate("html/about/header.html", pageVariables);
        String templateFileName = "html/about/device.html";
        for (Device device : Devices.getList()){
            pageVariables.put("device_url", device.getLink());
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_description", device.getNote(language.getCode()));
            NetworkDevice networkDevice = NetworkDevice.convertDevice(device);
            if (networkDevice != null) {
                pageVariables.put("address_value", networkDevice.getAddressSettings().getAddress());
                context += fillTemplate("html/about/controller.html", pageVariables);
                continue;
            }
            ArrayList<Pin> pins = device.getPins();
            int i = 0;
            if (pins.size() == 0)
                context += fillTemplate(templateFileName, pageVariables);
            else
                for (Pin pin : pins){
                    if (i>0){ // in case pin is not first
                        pageVariables.put("device_model", "");
                        pageVariables.put("device_description", "");
                    }
                    pageVariables.put("device_pin", device.getPinName(pin));
                    pageVariables.put("controller_pin", pin.getName());
                    String bus = "";
                    String hint = "";
                    SpiBusDevice spiBusDevice = SpiBusDevice.convertDevice(device);
                    if (spiBusDevice != null) {
                        bus = String.valueOf(spiBusDevice.getBusChannel());
                        hint = language.getValue(pageType.getName(), "buschannel_hint");
                    }
                    I2CBusDevice i2CBusDevice = I2CBusDevice.convertDevice(device);
                    if (i2CBusDevice != null) {
                        bus = String.valueOf(i2CBusDevice.getBusAddress());
                        hint = language.getValue(pageType.getName(), "busaddress_hint");
                    }
                    pageVariables.put("bus_hint", hint);
                    pageVariables.put("bus_pin", bus);
                    context += fillTemplate(templateFileName, pageVariables);
                    i++;
                }
        }
        return context;
    }

    private String getBoardInfo(Language language){
        if (!SystemInformation.isRaspbian)
            return "";
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        pageVariables.put("board_value", Staffbot.getBoardType());
        pageVariables.put("board_link", Staffbot.getProjectWebsite() + "/" + Staffbot.getBoardType());
        pageVariables.put("temperature_value", SystemInformation.getCPUTemperature(0));
        return fillTemplate("html/about/board.html", pageVariables);
    }

}
