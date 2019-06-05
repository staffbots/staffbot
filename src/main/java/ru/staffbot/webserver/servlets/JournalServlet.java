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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class JournalServlet extends MainServlet {

    public Journal journal = new Journal();

    public JournalServlet(AccountService accountService) {
        super(PageType.JOURNAL, accountService);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Database.connected()){
            response.sendRedirect("/about");
            return;
        }
        HttpSession session = request.getSession();
        Map<String, Object> pageVariables = new HashMap();

        Map<Integer, Boolean> checkboxes = new HashMap<>();
        for (NoteType pageType : NoteType.values()) {
            String checkboxName = "journal_" + pageType.name().toLowerCase();
            String checkboxValueStr = accountService.getAttribute(session,checkboxName); // Читаем из сессии
            if (checkboxValueStr.equals("")) {
                checkboxValueStr = "true"; // Значение при первой загрузке
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");
            checkboxes.put(pageType.getValue(), checkboxValue);
        }

        String toDateStr = accountService.getAttribute(session,"journal_todate");
        if (toDateStr.equals("")) toDateStr = request.getParameter("journal_todate");

        String fromDateStr = accountService.getAttribute(session,"journal_fromdate");
        if (fromDateStr.equals("")) fromDateStr = request.getParameter("journal_fromdate");

        journal.period.set(fromDateStr, toDateStr);

        pageVariables.put("journal_fromdate", journal.period.fromDate.getValueAsString());
        pageVariables.put("journal_todate", journal.period.toDate.getValueAsString());
        pageVariables.put("journal_datesize", journal.DATE_FORMAT.get().length());
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("journal_page", getJournalPage(checkboxes));
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
        //request.setAttribute("fromdate", Converter.dateToString(fromDate, DateFormat.TIMEDATE));
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        for (NoteType pageType : NoteType.values()) {
            String checkboxName = "journal_" + pageType.name().toLowerCase();
            String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
            checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
            accountService.setAttribute(session, checkboxName, checkboxValueStr);
        }
        accountService.setAttribute(request.getSession(),"journal_todate", request.getParameter("journal_todate"));
        accountService.setAttribute(request.getSession(),"journal_fromdate", request.getParameter("journal_fromdate"));
        doGet(request, response);
    }

    //
    private String getJournalPage(Map<Integer, Boolean> typesForShow) {
        ArrayList<Note> journalList = journal.getJournal(typesForShow);
        String htmlCode = (!journalList.isEmpty()) ? "" :
                "<tr><td></td><td>По указанному фильтру записей нет</td></tr>";
        String title;
        for (Note note : journalList) {
            title = Converter.dateToString(note.getDate(), DateFormat.FULLTIMEDATE);
            htmlCode += "<tr><td  align=\"right\" title=\""+title+"\">"
                    + Converter.dateToString(note.getDate(), DateFormat.CUTSHORTDATETIME)
                    + "</td><td>" + note.getMessage() + "</td></tr>";
        }
        return htmlCode;
    }
}
