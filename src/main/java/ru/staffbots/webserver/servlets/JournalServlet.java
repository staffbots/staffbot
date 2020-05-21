package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.Note;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.dates.DateFormat;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.values.DateValue;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class JournalServlet extends BaseServlet {

    private ArrayList<String> checkboxes = new ArrayList();;

    public JournalServlet(AccountService accountService) {
        super(PageType.JOURNAL, accountService);
        checkboxes.add("journal_fromdate_checkbox");
        checkboxes.add("journal_todate_checkbox");
        for (NoteType pageType : NoteType.values())
            checkboxes.add("journal_" + pageType.name().toLowerCase() + "_checkbox");
        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));
        doGet = (HttpServletRequest request, HttpServletResponse response) -> doGet(request, response);
    }

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (isAccessDenied(request, response)) return;
        Language language = accountService.getUserLanguage(request);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());

        Journal journal = Journal.getInstance();
        journal.setCount(accountService.getAttribute(request,"journal_count"));
        String toDateStr = accountService.getAttribute(request,"journal_todate");
        String fromDateStr = accountService.getAttribute(request,"journal_fromdate");
        journal.period.set(fromDateStr, toDateStr);

        Map<Integer, Boolean> typesForShow = new HashMap<>();

        for (String checkboxName : checkboxes){
            String checkboxValueStr = accountService.getAttribute(request, checkboxName, "true");
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");

            for (NoteType pageType : NoteType.values())
                if (checkboxName.equalsIgnoreCase("journal_" + pageType.name() + "_checkbox"))
                    typesForShow.put(pageType.getValue(), checkboxValue);

            if (checkboxName.equals("journal_fromdate_checkbox") && checkboxValue && (journal.period.getFromDate() == null))
                journal.period.initFromDate();

            if (checkboxName.equals("journal_todate_checkbox") && checkboxValue && (journal.period.getToDate() == null))
                journal.period.initToDate();
        }

        String searchString = accountService.getAttribute(request,"journal_search");
        pageVariables.put("journal_search", searchString);
        pageVariables.put("dateformat", Journal.dateFormat.getFormat());
        pageVariables.put("journal_fromdate", journal.period.getFromDateAsString());
        pageVariables.put("journal_todate", journal.period.getToDateAsString());
        pageVariables.put("journal_datesize", journal.dateFormat.get().length());
        pageVariables.put("journal_count", journal.getCount());
        pageVariables.put("journal_page", getJournalPage(typesForShow, searchString, language));
        String content = fillTemplate("html/" + pageType.getName() + ".html", pageVariables);
        super.doGet(request, response, content);
    }

    private boolean buttonApplyClick(HttpServletRequest request){
        accountService.setAttribute(request,"journal_search");
        accountService.setAttribute(request,"journal_count");
        accountService.setAttribute(request,"journal_fromdate");
        accountService.setAttribute(request,"journal_todate");
        for (String checkboxName : checkboxes){
            String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
            checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
            accountService.setAttribute(request, checkboxName, checkboxValueStr);
        }
        return true;
    }

    private String getJournalPage(Map<Integer, Boolean> typesForShow, String searchString, Language language) {
        ArrayList<Note> journalList = Journal.getInstance().getJournal(typesForShow, searchString, language);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        String htmlPath = "html/journal/";
        String htmlCode = fillTemplate(htmlPath + "empty.html",pageVariables);
        if(!journalList.isEmpty()) {
            htmlCode = fillTemplate(htmlPath + "title.html", pageVariables);
            for (Note note : journalList) {
                boolean line = (note.getName() == null);
                pageVariables.put("note_fulldate", DateValue.toString(note.getDate(), DateFormat.FULLTIMEDATE));
                pageVariables.put("note_type", line ? "init" : note.getType().getName());
                pageVariables.put("note_date", DateValue.toString(note.getDate(), DateFormat.CUTSHORTDATETIME));
                pageVariables.put("note_value", line ? "<hr>" : note.getMessage(language));
                pageVariables.put("type_description", line ? pageVariables.get("start_title") : note.getType().getDescription(language.getCode()));
                htmlCode += fillTemplate(htmlPath + "note.html", pageVariables);
            }
            pageVariables.put("total_size", journalList.size());
            htmlCode += fillTemplate(htmlPath + "total.html",pageVariables);
        }
        return htmlCode;
    }

}
