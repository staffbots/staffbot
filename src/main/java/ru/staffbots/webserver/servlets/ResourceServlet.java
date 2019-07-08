package ru.staffbots.webserver.servlets;

import ru.staffbots.tools.Resources;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.List;

// Обработка запроса ресурса
// вида: <server>:<port>/resource?name=<resourceName>
// например: localhost/resource?name=/html/scripts/main.js
// или https://localhost:8055/resource?name=/img/logo.png
public class ResourceServlet extends BaseServlet {

    // Список ресурсов, для получения которых не требуется авторизация
    public static final List<String> FREE_RESOURCES = asList(
        "/img/logo.png"
    );

    public ResourceServlet(AccountService accountService) {
        super(accountService);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{
        String resourceName = request.getParameter("name");
        if (!FREE_RESOURCES.contains(resourceName))
            if (accountService.isAccessDenied(request)) return;
        try {
            if (resourceName == null) return;
            response.getOutputStream().write(Resources.getAsBytes(resourceName));
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
