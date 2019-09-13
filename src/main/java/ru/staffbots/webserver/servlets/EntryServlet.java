package ru.staffbots.webserver.servlets;

import ru.staffbots.Pattern;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("website", Pattern.projectWebsite);
        pageVariables.put("main_pagename", Pattern.projectName + ":" + Pattern.solutionName + " - " + pageType.getDescription());
        pageVariables.put("entry_login", login);

        String result = PageGenerator.getPage("/html/entry.html", pageVariables);
        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Вызывается при отправке страницы
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        String login = request.getParameter("entry_login");
        String password = request.getParameter("entry_password");
        if (accountService.verifyUser(login, password) > -1) {
            accountService.addSession(request.getSession(), login);
            response.sendRedirect("/control");
        } else doGet(request, response);
    }

}
