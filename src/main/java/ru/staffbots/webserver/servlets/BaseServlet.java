package ru.staffbots.webserver.servlets;

import ru.staffbots.Pattern;
import ru.staffbots.database.Database;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseServlet extends HttpServlet {

    public static String site_bg_color = "fdfdfd";

    public static String main_bg_color = "dddddd";

    public static String page_bg_color = "bbbbbb";

    protected AccountService accountService;

    protected PageType pageType;


    public BaseServlet(PageType pageType, AccountService accountService){
        this.accountService = accountService;
        this.pageType = pageType;
    }

    public boolean isAccessDenied(HttpServletRequest request)throws IOException {
        return isAccessDenied(request, null);
    }

    public boolean isAccessDenied(HttpServletRequest request, HttpServletResponse response)throws IOException {
        int userAccessLevel = accountService.getUserAccessLevel(request);
        int pageAccessLevel = pageType.getAccessLevel();
        boolean accessDenied = (userAccessLevel < pageAccessLevel);

        String redirectLink = null;
        if (accessDenied) {
            redirectLink =
                    (userAccessLevel < 0) ? "/entry" :
                    (userAccessLevel < 1) ? "/about" :
                                            "/control";
        } else {
            accessDenied = !pageEnabled();
            if (accessDenied)
                redirectLink = "/about";
        };

        if ((redirectLink != null)&&(response != null))
            response.sendRedirect(redirectLink);

        return accessDenied;
    }

    public static boolean pageEnabled(PageType pageType) {
        return (!pageType.getDatabaseDepend() || Database.connected());
    }

    public boolean pageEnabled() {
        return pageEnabled(pageType);
    }

    protected String getMenu(int userAccessLevel) {
        String menu = "";
        Map<String, Object> menuVariables = new HashMap();
        for (PageType pageType: PageType.values()){
            if (pageEnabled(pageType)) {
                int pageAccessLevel = pageType.getAccessLevel();
                if (pageAccessLevel < 0) continue;
                if (pageAccessLevel > userAccessLevel) continue;
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
        String login = accountService.getUserLogin(request.getSession());
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("main_pagename", Pattern.projectName + ":" + Pattern.solutionName + " - " + pageType.getDescription());
        pageVariables.put("main_menu", getMenu(accountService.getUserAccessLevel(login)));
        pageVariables.put("main_content", content);
        pageVariables.put("main_login", login);
        pageVariables.put("main_role", accountService.users.getRole(login).getDescription());
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("main_bg_color", main_bg_color);

        String result = PageGenerator.getPage("main.html", pageVariables);


        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=UTF-8");
        response.setStatus( HttpServletResponse.SC_OK );
    }
}
