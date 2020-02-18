package ru.staffbots.webserver.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import ru.staffbots.Staffbot;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.users.Users;
import ru.staffbots.tools.TemplateFillable;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;
import ru.staffbots.webserver.WebServer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class BaseServlet extends HttpServlet implements TemplateFillable {

    public static String siteColor = "fdfdfd";

    public static String mainColor = "dddddd";

    public static String pageColor = "bbbbbb";

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
        return  isAccessDenied(request, response, true);
    }

    public boolean isAccessDenied(HttpServletRequest request, HttpServletResponse response, boolean redirecting)throws IOException {
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

        if (redirecting && (redirectLink != null) && (response != null))
            response.sendRedirect(redirectLink);

        return accessDenied;
    }

    public static boolean pageEnabled(PageType pageType) {
        return (!pageType.getDatabaseDepend() || Database.connected());
    }

    public boolean pageEnabled() {
        return pageEnabled(pageType);
    }

    private String getMenu(int userAccessLevel) {
        String menu = "";
        Map<String, Object> menuVariables = new HashMap();
        for (PageType pageType: PageType.values()){
            if (pageEnabled(pageType)) {
                int pageAccessLevel = pageType.getAccessLevel();
                if (pageAccessLevel < 0) continue;
                if (pageAccessLevel > userAccessLevel) continue;
                String caption = pageType.getCaption();
                boolean isCurrentPage = (pageType == this.pageType);
                if (isCurrentPage) caption = "<b>" + caption + "</b>";
                menuVariables.put("menu_caption", caption);
                menuVariables.put("menu_hint", pageType.getDescription());
                menuVariables.put("menu_link", isCurrentPage ? "" : "href=\"" + pageType.getName() + "\"");
                menu += fillTemplate("html/base/menu.html", menuVariables);
            }
        }
        return menu;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response, String content)throws IOException {
        String login = accountService.getUserLogin(request.getSession());

        Map<String, Object> pageVariables = Translator.getSection(PageType.BASE.getName());
        pageVariables.put("page_title",
            Staffbot.projectName + ":" +
            Staffbot.solutionName + " - " +
            pageType.getCaption());
        pageVariables.put("main_menu", getMenu(accountService.getUserAccessLevel(login)));
        pageVariables.put("page_content", content);
        pageVariables.put("login_value", login);
        pageVariables.put("role_value", Users.getRole(login).getDescription());
        pageVariables.put("update_delay", WebServer.updateDelay.toString());

        String result = fillTemplate("html/base.html", pageVariables);

        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=UTF-8");
        response.setStatus( HttpServletResponse.SC_OK );
    }


    /**
     * Карта хранит параметризированные функции, по которым расчитываются значения переменных
     * Map<Name, Function<Parametr, Value>>
     */
    protected Map<String, Function<HttpServletRequest,String>> getParameters = new HashMap();

    /**
     * Функция обрабатывает запросы вида get=name:value
     */
    protected boolean getResponse(HttpServletRequest request, HttpServletResponse response)throws IOException {
        String name = request.getParameter("get");
        if (!getParameters.containsKey(name)) return false;
        if (isAccessDenied(request, response, false)) return true;
        response.getOutputStream().write(getParameters.get(name).apply(request).getBytes("UTF-8"));
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        return true;
    }

    /**
     * Карта хранит параметризированные функции, по которым расчитываются значения переменных
     * Map<Name, Function<Parametr, Value>>
     */
    protected Map<String, Function<HttpServletRequest, Boolean>> setParameters = new HashMap();

    protected boolean setRequest(HttpServletRequest request)throws IOException {
        for (String name : setParameters.keySet())
            if (request.getParameter(name) != null)
                return setParameters.get(name).apply(request);
        return false;
    }

}
