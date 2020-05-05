package ru.staffbots.webserver.servlets;

import com.pi4j.io.gpio.Pin;
import ru.staffbots.Staffbot;
import ru.staffbots.database.Database;
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
import java.io.IOException;
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        Language language = accountService.getUserLanguage(request);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        pageVariables.put("board_value", Staffbot.boardType);
        pageVariables.put("board_link", Staffbot.projectWebsite + "/" + Staffbot.boardType);
        pageVariables.put("osname_value",System.getProperty("os.name"));
        pageVariables.put("osversion_value",System.getProperty("os.version"));
        pageVariables.put("osarch_value",System.getProperty("os.arch"));
        pageVariables.put("javaversion_value",System.getProperty("java.version"));
        pageVariables.put("dbserver_value",Database.SERVER);
        pageVariables.put("dbmsystem_value",Database.DBMSystem);
        pageVariables.put("dbname_value",Database.NAME);
        pageVariables.put("project_value", Staffbot.projectName);
        pageVariables.put("solution_value", Staffbot.solutionName + "-" + Staffbot.projectVersion);
        pageVariables.put("website_link", Staffbot.projectWebsite);
        pageVariables.put("device_list", getDeviceList(language));
        pageVariables.put("dberror_message", Database.connected() ? "" : Database.getException().getMessage());
        super.doGet(request, response, getContent(pageVariables));
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (setRequest(request))
            doGet(request, response);
    }

    private String getDeviceList(Language language) {
        String context = "";
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        if (Devices.list.size() > 0)
            context += fillTemplate("html/about/header.html", pageVariables);
        String templateFileName = "html/about/device.html";
        for (Device device : Devices.list){
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

}
