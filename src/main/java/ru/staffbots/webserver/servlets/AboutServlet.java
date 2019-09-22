package ru.staffbots.webserver.servlets;

import com.pi4j.io.gpio.Pin;
import ru.staffbots.Pattern;
import ru.staffbots.database.Database;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;
import ru.staffbots.webserver.WebServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AboutServlet extends BaseServlet {

    public AboutServlet(AccountService accountService) {
        super(PageType.ABOUT, accountService);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        if (isAccessDenied(request, response)) return;
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("website", Pattern.projectWebsite);
        pageVariables.put("about_osname",System.getProperty("os.name"));
        pageVariables.put("about_osversion",System.getProperty("os.version"));
        pageVariables.put("about_osarch",System.getProperty("os.arch"));
        pageVariables.put("about_javaversion",System.getProperty("java.version"));
        pageVariables.put("about_dbserver",Database.SERVER);
        pageVariables.put("about_dbmsystem",Database.DBMSystem);
        pageVariables.put("about_dbname",Database.NAME);
        pageVariables.put("about_project", Pattern.projectName);
        pageVariables.put("about_solution", Pattern.solutionName + "-" + Pattern.projectVersion);
        pageVariables.put("about_message", Database.connected() ? "" : Database.getException().getMessage());
        String trace = "";
        if (Database.disconnected())
            for (StackTraceElement traceElement: Database.getException().getStackTrace())
                trace += traceElement.toString() + "<br>";
        pageVariables.put("about_trace", trace);
        pageVariables.put("about_admin", WebServer.ADMIN);
        pageVariables.put("about_devicelist", getDeviceList());
        String content = FillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }

    private String getDeviceList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);
        String templateFileName = "html/items/device_pin.html";
        for (Device device : Devices.list){
            pageVariables.put("device_url", device.getURL());
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_note", device.getNote());
            pageVariables.put("pin_note", "");
            pageVariables.put("pin_name", "");

            ArrayList<Pin> pins = device.getPins();
            int i = 0;
            if (pins.size() == 0)
                context += FillTemplate(templateFileName, pageVariables);
            else
                for (Pin pin : pins){
                    if (i>0){
                        pageVariables.put("device_model", "");
                        pageVariables.put("device_note", "");

                    }
                    pageVariables.put("pin_note", Devices.pins.get(pin).pinNote);
                    pageVariables.put("pin_name", pin.getName());
                    context += FillTemplate(templateFileName, pageVariables);
                    i++;
                }

        }
        return context;
    }


}
