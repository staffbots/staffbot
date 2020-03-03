package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.devices.drivers.network.NetworkDevice;
import ru.staffbots.tools.resources.ResourceType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.WebServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import static java.util.Arrays.asList;

import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

// Обработка запроса ресурса
// вида: <server>:<port>/resource?<resourceName>
// например: localhost/resource?js/jquery/flot/jquery.flot.js
// или https://localhost/resource?img/logo.png
public class ResourceServlet extends BaseServlet {

    // Список защищенных ресурсов, для получения которых требуется авторизация
    public static final List<String> privateResources = asList(
            "keystore"
    );


    public ResourceServlet(AccountService accountService) {
        super(null, accountService);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        //String resourceName = request.getQueryString();
        String[] requests = request.getQueryString().split("&");

        String resourceName = requests.length > 0 ? requests[0] : null;

        if (resourceName == null) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        if (privateResources.contains(resourceName))
            if (accountService.getUserAccessLevel(request) < 0) return;

        try {
            Map<String, Object> pageVariables = new HashMap(0);
            switch (ResourceType.getByName(resourceName)){
                case CSS:
                    pageVariables.put("site_color", siteColor);
                    pageVariables.put("page_color", pageColor);
                    pageVariables.put("main_color", mainColor);
                    pageVariables.put("dberror_display", Database.connected() ? "none" : "inline-table");
                    pageVariables.put("piwarning_display", Devices.USED || Database.disconnected() ? "none" : "inline-table");
                    break;
                case INO:
                    String deviceName = requests.length > 1 ? requests[1] : null;
                    if (resourceName == null) break;
                    NetworkDevice networkDevice = null;
                    for (Device device: Devices.list)
                        if (device.getName().equals(deviceName))
                            networkDevice = NetworkDevice.convertDevice(device);
                    if (networkDevice == null) break;
                    pageVariables.put("device_name", networkDevice.getName());
                    pageVariables.put("device_address", networkDevice.getAddress().replaceAll("\\.", ","));
                    pageVariables.put("http_port", WebServer.httpPort);
                    break;
                default:
            }
            if (pageVariables.isEmpty()) {
                response.getOutputStream().write(Resources.getAsBytes(resourceName));
            } else {
                String result = fillTemplate(resourceName, pageVariables);
                response.getOutputStream().write(result.getBytes("UTF-8") );
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }

    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}
