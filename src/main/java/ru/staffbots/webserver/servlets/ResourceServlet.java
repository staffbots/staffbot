package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.resources.ResourceType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.AccountService;

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
        String resourceName = request.getQueryString();
        if (resourceName == null)
            return;

        if (privateResources.contains(resourceName))
            if (accountService.getUserAccessLevel(request) < 0) return;

        try {
            switch (ResourceType.getByName(resourceName)){
                case CSS:
                    Map<String, Object> pageVariables = new HashMap();
                    pageVariables.put("site_color", siteColor);
                    pageVariables.put("page_color", pageColor);
                    pageVariables.put("main_color", mainColor);
                    pageVariables.put("dberror_display", Database.connected() ? "none" : "inline-table");
                    pageVariables.put("piwarning_display", Devices.USED || Database.disconnected() ? "none" : "inline-table");
                    String result = fillTemplate(resourceName, pageVariables);
                    response.getOutputStream().write(result.getBytes("UTF-8") );
                    break;
                default:
                    response.getOutputStream().write(Resources.getAsBytes(resourceName));
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
