package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.users.UserRole;
import ru.staffbot.webserver.AccountService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class MainServlet extends BaseServlet {

    public static String main_bg_color = "e7d8df";

    public static String page_bg_color = "ccaabb";

    protected PageType pageType = PageType.ENTRY;

    public MainServlet(PageType pageType, AccountService accountService){
        super(accountService);
        this.pageType = pageType;
    }

    protected String getMenu(int accessLevel) {
        String menu = "";
        Map<String, Object> menuVariables = new HashMap();
        for (PageType pageType: PageType.values()){
            if ((accessLevel <= pageType.getAccessLevel())&&(!pageType.getDatabaseDepend()||Database.connected())) {
                menuVariables.put("main_menuName", pageType.getCaption());
                menuVariables.put("main_menuTitle", pageType.getDescription());
                menuVariables.put("main_menuRef", (pageType == this.pageType) ? "" : "href=\"" + pageType.getName() + "\"");
                menu += PageGenerator.getPage("menu.html", menuVariables);
            }
        }
        return menu;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response, String content) throws ServletException, IOException {
        if (accountService.isAccessDenied(request.getSession())){
            response.sendRedirect("/entry");
            return;
        }
        String login = accountService.getUserLogin(request.getSession());
        int accessLevel = accountService.getUserAccessLevel(login);
        if(pageType.getAccessLevel() < accessLevel){
            switch (UserRole.valueOf(accessLevel)) {
                case USER:
                    response.sendRedirect("/control");
                    break;
                case GUEST:
                    response.sendRedirect("/status");
                    break;
                default:
                    response.sendRedirect("/entry");
                    break;
            }
            return;
        }

        Map<String, Object> pageVariables = new HashMap();

        pageVariables.put("main_pagename", pageType.getDescription());
        pageVariables.put("main_menu", getMenu(accessLevel));
        pageVariables.put("main_content", content);
        pageVariables.put("main_login", login);
        pageVariables.put("main_role", accountService.getUserRole(login).getDescription());
        pageVariables.put("main_bg_color", main_bg_color);

        String result = PageGenerator.getPage("main.html", pageVariables);

        response.getWriter().println(result);

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }


}
