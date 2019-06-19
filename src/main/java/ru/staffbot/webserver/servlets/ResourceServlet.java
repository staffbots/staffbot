package ru.staffbot.webserver.servlets;

import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

///resource?name=/img/logo.png
public class ResourceServlet extends BaseServlet {

    public ResourceServlet(AccountService accountService) {
        super(accountService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            String resourceName = request.getParameter("name");
            if (resourceName == null) return;

//            if (!resourceName..equals("/img/logo.png")) // на страничке авторизации рисуем логотип вне зависимости от уровня доступа
//                if (accountService.isAccessDenied(request.getSession())) {
//                    response.sendRedirect("/entry");
//                    return;
//                }
            InputStream inputStream = ResourceServlet.class.getResourceAsStream(resourceName);

            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            response.getOutputStream().write(bytes);

//            response.getOutputStream().write(inputStream.readAllBytes());
        } catch (Exception exception) {
            // Ignore
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
