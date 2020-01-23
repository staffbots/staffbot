package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.database.cleaner.Cleaner;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Translator;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class SystemServlet extends BaseServlet {

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
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (isAccessDenied(request, response)) return;
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());

        Database.cleaner.refresh();

        for (String variable : dbcleanVariables)
            if (!variable.contains("_measure") && !variable.contains("_cleaning"))
                pageVariables.put(variable, "" + Database.settings.load(variable));

        for (String variable : dbcleanVariables)
            if (variable.contains("_measure")){
                List<String> values = (variable.contains("_auto_")) ?
                        Arrays.asList("minute", "hour", "day") : Arrays.asList("record", "day");
                for (String value : values)
                    pageVariables.put(variable + "_" + value,
                            value.equalsIgnoreCase(Database.settings.load(variable)) ? "selected" : "");
            }
        //dbclean_journal_value
        String variable = "dbclean_auto_cleaning";
        List<String> values = Arrays.asList("on", "off");
        for (String value : values)
            pageVariables.put(variable + "_" + value,
                    (value.equalsIgnoreCase(Database.settings.load(variable)) ? "checked" : ""));

        pageVariables.put("dateformat", Cleaner.DATE_FORMAT.getFormat());
        String content = FillTemplate("html/" + pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (isAccessDenied(request, response)) return;

        if (request.getParameter("dbclean_apply") != null){
            for (String variable : dbcleanVariables)
                Database.settings.save(variable, request.getParameter(variable));
            Database.cleaner.update();
        }
        if (request.getParameter("dbclean_now") != null){
            for (String variable : dbcleanVariables)
                if (!variable.contains("_auto_"))
                    Database.settings.save(variable, request.getParameter(variable));
            Database.cleaner.clean();
        }
        boolean exiting = false;
        String message = " из веб-интерфейса (пользователь: " + accountService.getUserLogin(request.getSession()) + ", адрес: " + request.getRemoteAddr() + ")";
        if (request.getParameter("system_shutdown") != null) exiting = shutdown(false, "Выключение системы " + message);
        if (request.getParameter("system_reboot") != null) exiting = shutdown(true, "Перезагрузка системы " + message);
        if (request.getParameter("system_exit") != null) exiting = true;
        if (exiting) {
            Journal.add(NoteType.WARNING, "Закрытие приложения" + message);
            System.exit(0);
        } else

        doGet(request, response);
    }

    private static boolean shutdown(boolean reboot, String message) throws RuntimeException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String shutdownCommand = (operatingSystem.contains("windows")) ?
            "shutdown -" + (reboot ? "r" : "s") + " -t 0" :
            "shutdown -" + (reboot ? "r" : "h") + " now";
        try {
            Journal.add(NoteType.WARNING, message);
            Runtime.getRuntime().exec(shutdownCommand);
        } catch (IOException exception) {
            Journal.add(NoteType.ERROR, "Ошибка выполнения команды " + shutdownCommand + "\n" + exception.getMessage());
            return false;
        }
        return true;
    }

}