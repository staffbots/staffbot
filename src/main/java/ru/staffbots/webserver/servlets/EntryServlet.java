package ru.staffbots.webserver.servlets;

import ru.staffbots.Staffbot;
import ru.staffbots.tools.Translator;
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
public class EntryServlet extends BaseServlet {

    public EntryServlet(AccountService accountService) {
        super(PageType.ENTRY, accountService);
    }

    // Вызывается при запросе странице с сервера (Обновление страницы)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String login = accountService.getUserLogin(request.getSession());

        if (login == null) login = "";
            accountService.forgetSession(request.getSession());

        Map<String, Object> pageVariables = Translator.getSection(PageType.ENTRY.getName());
        pageVariables.put("page_title", Staffbot.getShortName() + " - " + pageType.getCaption());
        pageVariables.put("website_link", Staffbot.projectWebsite);
        pageVariables.put("login_input", login);

        String result = fillTemplate("html/entry.html", pageVariables);
        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Вызывается при отправке страницы
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String login = request.getParameter("login_input");
        String password = request.getParameter("password_input");
        if (accountService.verifyUser(login, password) > -1) {
            accountService.addSession(request.getSession(), login);
            response.sendRedirect("/control");
        } else doGet(request, response);
    }

}
