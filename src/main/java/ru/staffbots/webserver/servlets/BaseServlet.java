package ru.staffbots.webserver.servlets;

import freemarker.template.Configuration;
import freemarker.template.Template;
import ru.staffbots.Pattern;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;
import ru.staffbots.webserver.WebServer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BaseServlet extends HttpServlet {

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
                menu += FillTemplate("html/items/menu_item.html", menuVariables);
            }
        }
        return menu;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response, String content)throws IOException {
        String login = accountService.getUserLogin(request.getSession());

        Map<String, Object> pageVariables = Translator.getSection(PageType.BASE.getName());
        pageVariables.put("page_title",
            Pattern.projectName + ":" +
            Pattern.solutionName + " - " +
            pageType.getCaption());
        pageVariables.put("main_menu", getMenu(accountService.getUserAccessLevel(login)));
        pageVariables.put("page_content", content);
        pageVariables.put("login_value", login);
        pageVariables.put("role_value", accountService.users.getRole(login).getDescription());
        pageVariables.put("update_delay", WebServer.updateDelay.toString());

        String result = FillTemplate("html/base.html", pageVariables);

        response.getOutputStream().write( result.getBytes("UTF-8") );

        response.setContentType("text/html; charset=UTF-8");
        response.setStatus( HttpServletResponse.SC_OK );
    }

    /**
     * Заполняет html-шаблон данными
     */
    protected String FillTemplate(String fileName, Map<String, Object> data) {
        Writer stream = new StringWriter();
        try {
            Configuration conf = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            String codePage = "UTF-8";
            conf.setDefaultEncoding(codePage);
            InputStream inputStream = Resources.getAsStream(fileName);
            Charset charset = StandardCharsets.UTF_8;
            Template template = new Template(fileName, new InputStreamReader(inputStream, charset), conf, codePage);
            template.process(data, stream);
        } catch (Exception e) {
            Journal.add(e.getMessage());
        }
        return stream.toString();
    }

    /**
     * Карта хранит параметризированные функции, по которым расчитываются значения переменных
     * Map<Name, Function<Parametr, Value>>
     */
    protected Map<String, Function<String,String>> getParameters = new HashMap();

    /**
     * Функция обрабатывает запросы вида get=name:value
     */
    protected boolean getResponse(HttpServletRequest request, HttpServletResponse response)throws IOException {
        String getName = request.getParameter("get");
        if (getName == null) return false;
        String value;
        for (String name : getParameters.keySet()){
            if (getName.startsWith(name)) {
                int index = getName.indexOf(":");
                value = (index < 0) ? null : getName.substring(index + 1);
                response.getOutputStream().write(getParameters.get(name).apply(value).getBytes("UTF-8"));
                response.setContentType("text/html; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                break;
            }
        }
        return true;
    }

}
