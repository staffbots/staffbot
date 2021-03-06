package ru.staffbots.webserver.servlets;

import ru.staffbots.Staffbot;
import ru.staffbots.database.Database;
import ru.staffbots.tools.SystemInformation;
import ru.staffbots.tools.colors.ColorSchema;
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
                case JS:
                    ColorSchema colorSchema = WebServer.getInstance().getColorSchema();
                    pageVariables.put("site_color", colorSchema.getSiteColor());
                    pageVariables.put("deep_color", colorSchema.getDeepColor());
                    pageVariables.put("main_color", colorSchema.getMainColor());
                    pageVariables.put("text_color", colorSchema.getTextColor());
                    pageVariables.put("half_color", colorSchema.getHalfColor());
                    pageVariables.put("font_family", WebServer.getInstance().getFontFamily());
                    pageVariables.put("dberror_display", Database.connected() ? "none" : "inline-table");
                    pageVariables.put("piwarning_display", SystemInformation.isRaspbian || Database.disconnected() ? "none" : "inline-table");
                    break;
                case INO:
                    String deviceName = requests.length > 1 ? requests[1] : null;
                    if (resourceName == null) break;
                    NetworkDevice networkDevice = null;
                    for (Device device: Devices.getList())
                        if (device.getName().equals(deviceName))
                            networkDevice = NetworkDevice.convertDevice(device);
                    if (networkDevice == null) break;
                    pageVariables.put("device_name", networkDevice.getName());
                    pageVariables.put("device_address", networkDevice.getAddressSettings().getAddress(true));
                    pageVariables.put("device_gateway", networkDevice.getAddressSettings().getGateway(true));
                    pageVariables.put("device_subnetMask", networkDevice.getAddressSettings().getSubnetMask(true));
                    pageVariables.put("http_port", WebServer.getInstance().getHttpPort());
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

}
