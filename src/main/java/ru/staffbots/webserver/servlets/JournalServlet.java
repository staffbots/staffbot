package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.Note;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class JournalServlet extends BaseServlet {

    private ArrayList<String> checkboxes = new ArrayList<>();;

    public JournalServlet(AccountService accountService) {
        super(PageType.JOURNAL, accountService);
        //checkboxes = new ArrayList<>(Arrays.asList("journal_fromdate_checkbox", "journal_todate_checkbox"));
        checkboxes.add("journal_fromdate_checkbox");
        checkboxes.add("journal_todate_checkbox");
        for (NoteType pageType : NoteType.values())
            checkboxes.add("journal_" + pageType.name().toLowerCase() + "_checkbox");
        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());

        Database.journal.setCount(accountService.getAttribute(request,"journal_count"));
        String toDateStr = accountService.getAttribute(request,"journal_todate");
        String fromDateStr = accountService.getAttribute(request,"journal_fromdate");
        Database.journal.period.set(fromDateStr, toDateStr);

        Map<Integer, Boolean> typesForShow = new HashMap<>();

        for (String checkboxName : checkboxes){
            String checkboxValueStr = accountService.getAttribute(request, checkboxName, "true");
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");

            for (NoteType pageType : NoteType.values())
                if (checkboxName.equalsIgnoreCase("journal_" + pageType.name() + "_checkbox"))
                    typesForShow.put(pageType.getValue(), checkboxValue);

            if (checkboxName.equals("journal_fromdate_checkbox") && checkboxValue && (Database.journal.period.getFromDate() == null))
                Database.journal.period.initFromDate();

            if (checkboxName.equals("journal_todate_checkbox") && checkboxValue && (Database.journal.period.getToDate() == null))
                Database.journal.period.initToDate();
        }

        String searchString = accountService.getAttribute(request,"journal_search");
        pageVariables.put("journal_search", searchString);
        pageVariables.put("dateformat", Journal.dateFormat.getFormat());
        pageVariables.put("journal_fromdate", Database.journal.period.getFromDateAsString());
        pageVariables.put("journal_todate", Database.journal.period.getToDateAsString());
        pageVariables.put("journal_datesize", Database.journal.dateFormat.get().length());
        pageVariables.put("journal_count", Database.journal.getCount());
        pageVariables.put("journal_page", getJournalPage(typesForShow, searchString));
        String content = fillTemplate("html/" + pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        setRequest(request);
        for (String checkboxName : checkboxes){
            String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
            checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
            accountService.setAttribute(request, checkboxName, checkboxValueStr);
        }
        doGet(request, response);
    }

    private boolean buttonApplyClick(HttpServletRequest request){
        accountService.setAttribute(request,"journal_search");
        accountService.setAttribute(request,"journal_count");
        accountService.setAttribute(request,"journal_fromdate");
        accountService.setAttribute(request,"journal_todate");
        return true;
    }

    private String getJournalPage(Map<Integer, Boolean> typesForShow, String searchString) {
        ArrayList<Note> journalList = Database.journal.getJournal(typesForShow, searchString);
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        String htmlPath = "html/journal/";
        String htmlCode = fillTemplate(htmlPath + "empty.html",pageVariables);
        if(!journalList.isEmpty()) {
            htmlCode = fillTemplate(htmlPath + "title.html", pageVariables);
            for (Note note : journalList) {
                boolean line = (note.getName() == null);
                pageVariables.put("note_fulldate", DateValue.toString(note.getDate(), DateFormat.FULLTIMEDATE));
                pageVariables.put("note_type", line ? "init" : note.getType().getName());
                pageVariables.put("note_date", DateValue.toString(note.getDate(), DateFormat.CUTSHORTDATETIME));
                pageVariables.put("note_value", line ? "<hr>" : note.getMessage());
                pageVariables.put("type_description", line ? pageVariables.get("start_title") : note.getType().getDescription());
                htmlCode += fillTemplate(htmlPath + "note.html", pageVariables);
            }
            pageVariables.put("total_size", journalList.size());
            htmlCode += fillTemplate(htmlPath + "total.html",pageVariables);
        }
        return htmlCode;
    }

}
