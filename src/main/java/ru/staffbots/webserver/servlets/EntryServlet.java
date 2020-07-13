package ru.staffbots.webserver.servlets;

import ru.staffbots.Staffbot;
import ru.staffbots.database.tables.users.Users;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class EntryServlet extends BaseServlet {

    public EntryServlet(AccountService accountService) {
        super(PageType.ENTRY, accountService);
    }

    // Вызывается при запросе странице с сервера (Обновление страницы)
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String login = accountService.getUserLogin(request);
        if (login == null) login = "";
            accountService.forgetSession(request.getSession());

        Language language = accountService.getUserLanguage(request);

        Map<String, Object> pageVariables = language.getSection(PageType.ENTRY.getName());
        pageVariables.put("page_title", Staffbot.getShortName() + " - " + pageType.getCaption(language.getCode()));
        pageVariables.put("website_link", Staffbot.getProjectWebsite());
        pageVariables.put("login_input", login);

        String result = fillTemplate("html/entry.html", pageVariables);
        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Вызывается при отправке страницы
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        String login = request.getParameter("login_input");
        String password = request.getParameter("password_input");
        if (accountService.verifyUser(login, password) > -1) {
            accountService.addSession(request.getSession(), login);
            accountService.setUserLanguage(request, Users.getLanguage(login));
            response.sendRedirect("/control");
        } else doGet(request, response);
    }

}
