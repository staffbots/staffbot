package ru.staffbots.webserver.servlets;

import ru.staffbots.tools.Resources;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Обработка запроса ресурса
// вида: <server>:<port>/resource?name=<resourceName>
// например: localhost:8055/resource?name=/html/scripts/jquery.js
public class ResourceServlet extends BaseServlet {

    public ResourceServlet(AccountService accountService) {
        super(accountService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String resourceName = request.getParameter("name");
            if (resourceName == null) return;
            response.getOutputStream().write(Resources.getAsBytes(resourceName));
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
