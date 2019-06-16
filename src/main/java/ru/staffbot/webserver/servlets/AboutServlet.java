package ru.staffbot.webserver.servlets;

import com.pi4j.io.gpio.Pin;
import ru.staffbot.Grower;
import ru.staffbot.database.Database;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.Value;
import ru.staffbot.utils.values.ValueType;
import ru.staffbot.webserver.AccountService;
import ru.staffbot.webserver.WebServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AboutServlet extends MainServlet {

    public AboutServlet(AccountService accountService) {
        super(PageType.ABOUT, accountService);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();


        pageVariables.put("about_osname",System.getProperty("os.name"));
        pageVariables.put("about_osversion",System.getProperty("os.version"));
        pageVariables.put("about_osarch",System.getProperty("os.arch"));
        pageVariables.put("about_javaversion",System.getProperty("java.version"));
        pageVariables.put("about_dbserver",Database.SERVER);
        pageVariables.put("about_dbmsystem",Database.DBMSystem);
        pageVariables.put("about_dbname",Database.NAME);
        pageVariables.put("about_project", Grower.projectName);
        pageVariables.put("about_solution", Grower.solutionName + "-" + Grower.projectVersion);

        pageVariables.put("about_message", Database.getException().getMessage());
        String trace = "";
        for (StackTraceElement traceElement: Database.getException().getStackTrace())
            trace += traceElement.toString() + "<br>";
        pageVariables.put("about_trace", trace);
        pageVariables.put("about_admin", WebServer.ADMIN);
        pageVariables.put("device_display", Devices.USED ? "none" : "inline-table");
        pageVariables.put("database_display", Database.connected() ? "none" : "inline-table");
        pageVariables.put("about_devicelist", getDeviceList());
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        String content = PageGenerator.getPage(pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public String getDeviceList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);
        for (Device device : Devices.list){
            // String deviceName = device.getName();
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_note", device.getNote());
            pageVariables.put("pin_note", "");
            pageVariables.put("pin_name", "");

            ArrayList<Pin> pins = device.getPins();
            int i = 0;
            if (pins.size() == 0)
                context += PageGenerator.getPage("items/device_pin.html",pageVariables);
            else
                for (Pin pin : pins){
                    if (i>0){
                        pageVariables.put("device_model", "");
                        pageVariables.put("device_note", "");

                    }
                    pageVariables.put("pin_note", Devices.pins.get(pin).pinNote);
                    pageVariables.put("pin_name", pin.getName());
                    context += PageGenerator.getPage("items/device_pin.html",pageVariables);
                    i++;
                }

        }
        return context;
    }


}
