package ru.staffbot.webserver.servlets;

import ru.staffbot.webserver.servlets.PageGenerator;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Главный поток
 */
//entry
public class EntryServlet extends MainServlet {

    public EntryServlet(AccountService accountService) {
        super(PageType.ENTRY, accountService);
    }

    // Вызывается при запросе странице с сервера (Обновление страницы)
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String login = accountService.getUserLogin(request.getSession());
        if (login == null) login = "";
            accountService.forgetSession(request.getSession());

        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("entry_login", login);
        response.getWriter().println(PageGenerator.getPage("entry.html", pageVariables));
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Вызывается при отправке страницы
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String login = PageGenerator.fromCode(request.getParameter("entry_login"));
        String password = PageGenerator.fromCode(request.getParameter("entry_password"));
        if (accountService.verifyUser(login, password) > -1) {
            accountService.addSession(request.getSession(), login);
            response.sendRedirect("/control");
        } else doGet(request, response);
    }

}
