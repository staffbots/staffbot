package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.database.settings.Settings;
import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class SystemServlet extends MainServlet {

    public SystemServlet(AccountService accountService) {
            super(PageType.SYSTEM, accountService);
        }

    private List<String> dbcleanVariables = Arrays.asList(
            "dbclean_journal_value",
            "dbclean_journal_measure",
            "dbclean_tables_value",
            "dbclean_tables_measure",
            "dbclean_auto_cleaning",
            "dbclean_auto_value",
            "dbclean_auto_measure",
            "dbclean_auto_start");

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();

        Database.cleaner.refresh();

        for (String variable : dbcleanVariables)
            if (!variable.contains("_measure") && !variable.contains("_cleaning"))
                pageVariables.put(variable, (new Settings(variable)).load());

        for (String variable : dbcleanVariables)
            if (variable.contains("_measure")){
                List<String> values = (variable.contains("_auto_")) ?
                        Arrays.asList("minute", "hour", "day") : Arrays.asList("record", "day");
                String dbValue = (new Settings(variable)).load();
                for (String value : values)
                    pageVariables.put(variable + "_" + value,
                            dbValue.equalsIgnoreCase(value) ? "selected" : "");
            }

        String variable = "dbclean_auto_cleaning";
        List<String> values = Arrays.asList("on", "off");
        for (String value : values)
            pageVariables.put(variable + "_" + value,
                    (new Settings(variable)).load().equalsIgnoreCase(value) ? "checked" : "");


        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("system_display", Database.connected() ? "inline-table" : "none");
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("dbclean_apply") != null){
            for (String variable : dbcleanVariables)
                (new Settings(variable)).save(request.getParameter(variable));
            Database.cleaner.update();
        }
        if (request.getParameter("dbclean_now") != null){
            for (String variable : dbcleanVariables)
                if (!variable.contains("_auto_"))
                    (new Settings(variable)).save(request.getParameter(variable));
            Database.cleaner.clean();
        }
        boolean exiting = false;
        if (request.getParameter("system_shutdown") != null) exiting = shutdown(false);
        if (request.getParameter("system_reboot") != null) exiting = shutdown(true);
        if (request.getParameter("system_exit") != null) exiting = true;
        if (exiting) {
            //response.getWriter().println(PageGenerator.toCode("Прощай юный мой друг!"));
            System.exit(0);
        } else

        doGet(request, response);
    }

    public static boolean shutdown(boolean reboot) throws RuntimeException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String shutdownCommand = (operatingSystem.contains("windows")) ?
            "shutdown -" + (reboot ? "r" : "s") + " -t 0" :
            "shutdown -" + (reboot ? "r" : "h") + " now";
        try {
            Runtime.getRuntime().exec(shutdownCommand);
        } catch (IOException exception) {
            Journal.add("Ошибка выполнения команды " + shutdownCommand + "\n" + exception.getMessage(), NoteType.ERROR);
            return false;
        }
        return true;
    }

    }