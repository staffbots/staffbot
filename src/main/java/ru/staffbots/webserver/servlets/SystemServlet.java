package ru.staffbots.webserver.servlets;

import ru.staffbots.database.*;
import ru.staffbots.database.tables.DBTable;
import ru.staffbots.database.tables.Variables;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class SystemServlet extends BaseServlet {

    private static final List<String> dbcleanVariables = Arrays.asList(
            "dbclean_journal_value",
            "dbclean_journal_measure",
            "dbclean_tables_value",
            "dbclean_tables_measure",
            "dbclean_auto_cleaning",
            "dbclean_auto_value",
            "dbclean_auto_measure",
            "dbclean_auto_start");

    public SystemServlet(AccountService accountService) {
        super(PageType.SYSTEM, accountService);
        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));
        setParameters.put("clean_button", (HttpServletRequest request) -> buttonCleanClick(request));
        setParameters.put("shutdown_button", (HttpServletRequest request) -> buttonShutdownClick(request));
        setParameters.put("reboot_button", (HttpServletRequest request) -> buttonRebootClick(request));
        setParameters.put("exit_button", (HttpServletRequest request) -> buttonExitClick(request));
        doGet = (HttpServletRequest request, HttpServletResponse response) -> doGet(request, response);
    }

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (isAccessDenied(request, response)) return;
        Language language = accountService.getUserLanguage(request);

        Map<String, Object> pageVariables = language.getSection(pageType.getName());

        Cleaner.reload();

        for (String variable : dbcleanVariables)
            if (!variable.contains("_measure") && !variable.contains("_cleaning"))
                pageVariables.put(variable, "" + Variables.load(variable));

        for (String variable : dbcleanVariables)
            if (variable.contains("_measure")){
                List<String> values = (variable.contains("_auto_")) ?
                        Arrays.asList("minute", "hour", "day") : Arrays.asList("record", "day");
                for (String value : values)
                    pageVariables.put(variable + "_" + value,
                            value.equalsIgnoreCase(Variables.load(variable)) ? "selected" : "");
            }
        //dbclean_journal_value
        String variable = "dbclean_auto_cleaning";
        List<String> values = Arrays.asList("on", "off");
        for (String value : values)
            pageVariables.put(variable + "_" + value,
                    (value.equalsIgnoreCase(Variables.load(variable)) ? "checked" : ""));

        pageVariables.put("date_format", Cleaner.getFormat());
        pageVariables.put("table_list", getTableList(language));

        String content = fillTemplate("html/" + pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
    }

    private boolean buttonApplyClick(HttpServletRequest request){
        for (String variable : dbcleanVariables)
            Variables.save(variable, request.getParameter(variable));
        Cleaner.restart();
        return true;
    }

    private boolean buttonCleanClick(HttpServletRequest request) {
        for (String variable : dbcleanVariables)
            if (!variable.contains("_auto_"))
                Variables.save(variable, request.getParameter(variable));
        Cleaner.clean();
        return true;
    }

    private boolean buttonShutdownClick(HttpServletRequest request){
        String login = accountService.getUserLogin(request.getSession());
        Journal.add(NoteType.WARNING, "shutdown_system", login, request.getRemoteAddr());
        shutdown(false);
        return false;
    }

    private boolean buttonRebootClick(HttpServletRequest request){
        String login = accountService.getUserLogin(request.getSession());
        Journal.add(NoteType.WARNING, "reboot_system", login, request.getRemoteAddr());
        shutdown(true);
        return false;
    }

    private boolean buttonExitClick(HttpServletRequest request){
        String login = accountService.getUserLogin(request.getSession());
        Journal.add(NoteType.WARNING, "exit_program", login, request.getRemoteAddr());
        System.exit(0);
        return false;
    }

    private static boolean shutdown(boolean reboot) throws RuntimeException {
        String operatingSystem = System.getProperty("os.name").toLowerCase();
        String shutdownCommand = (operatingSystem.contains("windows")) ?
            "shutdown -" + (reboot ? "r" : "s") + " -t 0" :
            "shutdown -" + (reboot ? "r" : "h") + " now";
        try {
            Runtime.getRuntime().exec(shutdownCommand);
        } catch (IOException exception) {
            Journal.add(NoteType.ERROR, "shutdown_error", shutdownCommand, exception.getMessage());
            return false;
        }
        return true;
    }

    private String getTableList(Language language) {
        String context = "";
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        Map<String, DBTable> tableList = Database.getTableList();
        for (String tableName: tableList.keySet()){
            pageVariables.put("name_value", tableName);
            DBTable dbTable = tableList.get(tableName);
            pageVariables.put("note_value", dbTable == null ? pageVariables.get("table_unuse") :
                    language.getValue("database", dbTable.getTableName()));
            pageVariables.put("rows_value", dbTable == null ? dbTable.getRows() : dbTable.getRows());
            context += fillTemplate("html/system/table.html",pageVariables);
        }
        return context;
    }


}