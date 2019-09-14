package ru.staffbots.webserver.servlets;

import ru.staffbots.tools.resources.ResourceType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static java.util.Arrays.asList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Обработка запроса ресурса
// вида: <server>:<port>/resource?<resourceName>
// например: localhost/resource?js/jquery/flot/jquery.flot.js
// или https://localhost:8055/resource?img/logo.png
public class ResourceServlet extends BaseServlet {

    // Список ресурсов, для получения которых не требуется авторизация
    public static final List<String> FREE_RESOURCES = asList(
            "css/main.css", "img/logo.png", "img/icon.ico"
    );

    public ResourceServlet(AccountService accountService) {
        super(null, accountService);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{

        String resourceName = request.getQueryString();

        System.out.println(resourceName);

        if (resourceName == null)
            return;

        if (!FREE_RESOURCES.contains(resourceName))
            if (accountService.getUserAccessLevel(request) < 0) return;

        try {
            switch (ResourceType.getByName(resourceName)){
                case CSS:
                    Map<String, Object> pageVariables = new HashMap();
                    pageVariables.put("site_bg_color", site_bg_color);
                    pageVariables.put("page_bg_color", page_bg_color);
                    pageVariables.put("main_bg_color", main_bg_color);
                    String result = FillTemplate(resourceName, pageVariables);
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
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        doGet(request, response);
    }
}
