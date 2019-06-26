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

    public static String site_bg_color = "fdfdfd";

    public static String main_bg_color = "dddddd";

    public static String page_bg_color = "bbbbbb";

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
                menuVariables.put("bg_color", (pageType == this.pageType) ? page_bg_color : main_bg_color);
                String caption = pageType.getCaption();
                if (pageType == this.pageType) caption = "<b>" + caption + "</b>";
                menuVariables.put("main_menuName", caption);
                menuVariables.put("main_menuTitle", pageType.getDescription());
                menuVariables.put("main_menuRef", (pageType == this.pageType) ? "" : "href=\"" + pageType.getName() + "\"");

                menu += PageGenerator.getPage("items/menu_item.html", menuVariables);
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
                case MANAGER:
                    response.sendRedirect("/control");
                    break;
                case INSPECTOR:
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
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("main_bg_color", main_bg_color);

        String result = PageGenerator.getPage("main.html", pageVariables);

        response.getWriter().println(result);

        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }


}
