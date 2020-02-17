package ru.staffbots.webserver.servlets;

import com.pi4j.io.gpio.Pin;
import ru.staffbots.Staffbot;
import ru.staffbots.database.Database;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (isAccessDenied(request, response)) return;
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        pageVariables.put("website_link", Staffbot.projectWebsite);
        pageVariables.put("about_osname",System.getProperty("os.name"));
        pageVariables.put("about_osversion",System.getProperty("os.version"));
        pageVariables.put("about_osarch",System.getProperty("os.arch"));
        pageVariables.put("about_javaversion",System.getProperty("java.version"));
        pageVariables.put("about_dbserver",Database.SERVER);
        pageVariables.put("about_dbmsystem",Database.DBMSystem);
        pageVariables.put("about_dbname",Database.NAME);
        pageVariables.put("about_project", Staffbot.projectName);
        pageVariables.put("about_solution", Staffbot.solutionName + "-" + Staffbot.projectVersion);
        pageVariables.put("about_devicelist", getDeviceList());

        pageVariables.put("dberror_message", Database.connected() ? "" : Database.getException().getMessage());
        String trace = "";
        if (Database.disconnected())
            for (StackTraceElement traceElement: Database.getException().getStackTrace())
                trace += traceElement.toString() + "<br>";
        pageVariables.put("dberror_trace", trace);

        String content = fillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    private String getDeviceList() {
        String context = "";
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        String templateFileName = "html/about/device.html";
        for (Device device : Devices.list){
            pageVariables.put("device_url", device.getURL());
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_description", device.getNote());
            pageVariables.put("device_pin", "");
            pageVariables.put("controller_pin", "");

            ArrayList<Pin> pins = device.getPins();
            int i = 0;
            if (pins.size() == 0)
                context += fillTemplate(templateFileName, pageVariables);
            else
                for (Pin pin : pins){
                    if (i>0){
                        pageVariables.put("device_model", "");
                        pageVariables.put("device_description", "");

                    }
                    pageVariables.put("device_pin", device.getPinName(pin));
                    pageVariables.put("controller_pin", pin.getName());
                    pageVariables.put("busaddress_pin", device.busAddress < 0 ? "" : device.busAddress);
                    context += fillTemplate(templateFileName, pageVariables);
                    i++;
                }

        }
        return context;
    }


}
