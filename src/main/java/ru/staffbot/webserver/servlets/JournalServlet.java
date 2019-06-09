package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.Note;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

public class JournalServlet extends MainServlet {

    public Journal journal = new Journal();

    private ArrayList<String> checkboxes;

    public JournalServlet(AccountService accountService) {
        super(PageType.JOURNAL, accountService);
        checkboxes = new ArrayList<>(Arrays.asList("journal_fromdate_on", "journal_todate_on"));
        for (NoteType pageType : NoteType.values())
            checkboxes.add("journal_" + pageType.name().toLowerCase());
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Database.connected()){
            response.sendRedirect("/about");
            return;
        }
        HttpSession session = request.getSession();
        Map<String, Object> pageVariables = new HashMap();

        String toDateStr = accountService.getAttribute(session,"journal_todate");
        if (toDateStr.equals("")) toDateStr = request.getParameter("journal_todate");

        String fromDateStr = accountService.getAttribute(session,"journal_fromdate");
        if (fromDateStr.equals("")) fromDateStr = request.getParameter("journal_fromdate");

        journal.period.set(fromDateStr, toDateStr);

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

            if (checkboxName.equals("journal_fromdate_on") && checkboxValue && (journal.period.fromDate == null))
                journal.period.initFromDate();

            if (checkboxName.equals("journal_todate_on") && checkboxValue && (journal.period.toDate == null))
                journal.period.initToDate();
        }

        String searchString = accountService.getAttribute(session,"journal_search");
        pageVariables.put("journal_search", searchString);
        pageVariables.put("dateformat", Journal.DATE_FORMAT.getFormat());
        pageVariables.put("journal_fromdate", journal.period.getFromDateAsString());
        pageVariables.put("journal_todate", journal.period.getToDateAsString());
        pageVariables.put("journal_datesize", journal.DATE_FORMAT.get().length());
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("journal_page", getJournalPage(typesForShow, searchString));
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
        //request.setAttribute("fromdate", Converter.dateToString(fromDate, DateFormat.TIMEDATE));
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        for (String checkboxName : checkboxes){
            String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
            checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
            accountService.setAttribute(session, checkboxName, checkboxValueStr);
        }
        accountService.setAttribute(request.getSession(), "journal_search",
                PageGenerator.fromCode(request.getParameter("journal_search")));
        accountService.setAttribute(request.getSession(),"journal_todate", request.getParameter("journal_todate"));
        accountService.setAttribute(request.getSession(),"journal_fromdate", request.getParameter("journal_fromdate"));
        doGet(request, response);
    }

    //
    private String getJournalPage(Map<Integer, Boolean> typesForShow, String searchString) {
        ArrayList<Note> journalList = journal.getJournal(typesForShow, searchString);
        Map<String, Object> pageVariables = new HashMap();
        String htmlCode = "<tr><td><em>По указанному фильтру записей нет</em></td></tr>";
        if(!journalList.isEmpty()) {
            pageVariables.put("note_title", "");
            pageVariables.put("note_date", "<b>Дата</b>");
            pageVariables.put("note_value", "<b>Сообщение</b>");
            htmlCode = PageGenerator.getPage("journal/note.html",pageVariables);
            for (Note note : journalList) {
                pageVariables.put("note_title", Converter.dateToString(note.getDate(), DateFormat.FULLTIMEDATE));
                pageVariables.put("note_date", Converter.dateToString(note.getDate(), DateFormat.CUTSHORTDATETIME));
                pageVariables.put("note_value", note.getMessage());
                htmlCode += PageGenerator.getPage("journal/note.html",pageVariables);
            }
            pageVariables.put("note_title", "");
            pageVariables.put("note_date", "<em>Выбрано записей:</em>");
            pageVariables.put("note_value", "<em>" + journalList.size() + "</em>");
            htmlCode += PageGenerator.getPage("journal/note.html",pageVariables);
        }
        return htmlCode;
    }
}
