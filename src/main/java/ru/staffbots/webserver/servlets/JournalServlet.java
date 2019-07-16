package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.Note;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class JournalServlet extends BaseServlet {

    private ArrayList<String> checkboxes;

    public JournalServlet(AccountService accountService) {
        super(PageType.JOURNAL, accountService);
        checkboxes = new ArrayList<>(Arrays.asList("journal_fromdate_on", "journal_todate_on"));
        for (NoteType pageType : NoteType.values())
            checkboxes.add("journal_" + pageType.name().toLowerCase());
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (isAccessDenied(request, response)) return;

        HttpSession session = request.getSession();
        Map<String, Object> pageVariables = new HashMap();

        String toDateStr = accountService.getAttribute(session,"journal_todate");
        if (toDateStr.equals("")) toDateStr = request.getParameter("journal_todate");

        String fromDateStr = accountService.getAttribute(session,"journal_fromdate");
        if (fromDateStr.equals("")) fromDateStr = request.getParameter("journal_fromdate");

        Database.journal.period.set(fromDateStr, toDateStr);

        Database.journal.setCount(accountService.getAttribute(session,"journal_count"));

        Map<Integer, Boolean> typesForShow = new HashMap<>();

        for (String checkboxName : checkboxes){
            String checkboxValueStr = accountService.getAttribute(session,checkboxName); // Читаем из сессии
            if (checkboxValueStr.equals("")) {
                checkboxValueStr = "true"; // Значение при первой загрузке
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");
            for (NoteType pageType : NoteType.values())
                if (checkboxName.equalsIgnoreCase("journal_" + pageType.name()))
                    typesForShow.put(pageType.getValue(), checkboxValue);

            if (checkboxName.equals("journal_fromdate_on") && checkboxValue && (Database.journal.period.fromDate == null))
                Database.journal.period.initFromDate();

            if (checkboxName.equals("journal_todate_on") && checkboxValue && (Database.journal.period.toDate == null))
                Database.journal.period.initToDate();
        }

        String searchString = accountService.getAttribute(session,"journal_search");
        pageVariables.put("journal_search", searchString);
        pageVariables.put("dateformat", Journal.DATE_FORMAT.getFormat());
        pageVariables.put("journal_fromdate", Database.journal.period.getFromDateAsString());
        pageVariables.put("journal_todate", Database.journal.period.getToDateAsString());
        pageVariables.put("journal_datesize", Database.journal.DATE_FORMAT.get().length());
        pageVariables.put("journal_count", Database.journal.getCount());
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("journal_page", getJournalPage(typesForShow, searchString));
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
        //request.setAttribute("fromdate", Converter.dateToString(fromDate, DateFormat.TIMEDATE));
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (isAccessDenied(request, response)) return;

        HttpSession session = request.getSession();
        for (String checkboxName : checkboxes){
            String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
            checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
            accountService.setAttribute(session, checkboxName, checkboxValueStr);
        }
        accountService.setAttribute(request.getSession(), "journal_search",
                request.getParameter("journal_search"));
        accountService.setAttribute(request.getSession(),"journal_count",
                request.getParameter("journal_count"));
        accountService.setAttribute(request.getSession(),"journal_todate",
                request.getParameter("journal_todate"));
        accountService.setAttribute(request.getSession(),"journal_fromdate",
                request.getParameter("journal_fromdate"));
        doGet(request, response);
    }

    //
    private String getJournalPage(Map<Integer, Boolean> typesForShow, String searchString) {
        ArrayList<Note> journalList = Database.journal.getJournal(typesForShow, searchString);
        Map<String, Object> pageVariables = new HashMap();
        String htmlCode = "<tr><td><em>По указанному фильтру записей нет</em></td></tr>";
        if(!journalList.isEmpty()) {
            pageVariables.put("note_title", "");
            pageVariables.put("note_date", "<b>Дата</b>");
            pageVariables.put("note_value", "<b>Сообщение</b>");
            htmlCode = PageGenerator.getPage("items/journal_note.html",pageVariables);
            for (Note note : journalList) {
                pageVariables.put("note_title", DateValue.toString(note.getDate(), DateFormat.FULLTIMEDATE));
                pageVariables.put("note_date", DateValue.toString(note.getDate(), DateFormat.CUTSHORTDATETIME));
                pageVariables.put("note_value", note.getMessage());
                htmlCode += PageGenerator.getPage("items/journal_note.html",pageVariables);
            }
            pageVariables.put("note_title", "");
            pageVariables.put("note_date", "<em>Выбрано записей:</em>");
            pageVariables.put("note_value", "<em>" + journalList.size() + "</em>");
            htmlCode += PageGenerator.getPage("items/journal_note.html",pageVariables);
        }
        return htmlCode;
    }
}
