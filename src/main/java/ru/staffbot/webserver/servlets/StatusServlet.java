package ru.staffbot.webserver.servlets;

import ru.staffbot.database.DBValue;
import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.Period;
import ru.staffbot.utils.Converter;
import ru.staffbot.utils.DateFormat;
import ru.staffbot.utils.botprocess.BotProcess;
import ru.staffbot.utils.botprocess.BotTask;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.levers.Lever;
import ru.staffbot.utils.levers.Levers;
import ru.staffbot.utils.values.BooleanValue;
import ru.staffbot.utils.values.Value;
import ru.staffbot.utils.values.ValueType;
import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Sysadmin on 25.05.2017.
 */
public class StatusServlet extends MainServlet {

    private ArrayList<String> checkboxes =  new ArrayList<>();

    public StatusServlet(AccountService accountService) {
        super(PageType.STATUS, accountService);
        checkboxes.add("status_fromdate_on");
        checkboxes.add("status_todate_on");
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isPlotPossible())
                    checkboxes.add(value.getName() + "_checkbox");
        for (Lever lever : Levers.list)
            if (lever.toValue().isPlotPossible())
                checkboxes.add(lever.toValue().getName() + "_checkbox");
    }

    public void doGetValue(Value value, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println(value.getValueAsString());
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();
        HttpSession session = request.getSession();

        String getName = request.getParameter("get");
        if (getName != null) {
            if (getName.equals("tasklist")) {
                response.getWriter().println(getTaskList());
                response.setContentType("text/html; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            if (getName.equals("processstatus")) {
                response.getWriter().println(
                        PageGenerator.toCode(BotProcess.getStatus().getDescription()));
                response.setContentType("text/html; charset=utf-8");
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }
            for (Device device : Devices.list)
                for (Value value : device.getValues())
                    if (getName.equals(value.getName())) {
                        doGetValue(value, response);
                        return;
                    }
            for (Lever lever : Levers.list)
                if (getName.equals(lever.toValue().getName())) {
                    doGetValue(lever.toValue(), response);
                    return;
                }
            return;
        }

        Period period = new Period(Journal.DATE_FORMAT);

        String toDateStr = accountService.getAttribute(session,"status_todate");
        if (toDateStr.equals("")) toDateStr = request.getParameter("status_todate");

        String fromDateStr = accountService.getAttribute(session,"status_fromdate");
        if (fromDateStr.equals("")) fromDateStr = request.getParameter("status_fromdate");

        period.set(fromDateStr, toDateStr);


        for (String checkboxName : checkboxes) {
            String checkboxValueStr = accountService.getAttribute(session, checkboxName); // Читаем из сессии
            if (checkboxValueStr.equals("")) {
                checkboxValueStr = "true"; // Значение при первой загрузке
                for (Lever lever : Levers.list)
                    if (lever.toValue().isPlotPossible())
                        if (checkboxName.equals(lever.toValue().getName() + "_checkbox")){
                            checkboxValueStr = "false"; // Значение при первой загрузке для lever
                            break;
                        }
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");

            if (checkboxName.equals("status_fromdate_on") && checkboxValue && (period.fromDate == null))
                period.initFromDate();

            if (checkboxName.equals("status_todate_on") && checkboxValue && (period.toDate == null))
                period.initToDate();
        }

        pageVariables.put("start_time", Long.toString(BotProcess.getStartTime()));
        pageVariables.put("dateformat", Journal.DATE_FORMAT.getFormat());
        pageVariables.put("status_fromdate", period.getFromDateAsString());
        pageVariables.put("status_todate", period.getToDateAsString());
        pageVariables.put("status_devicelist", getDeviceList(session));
        pageVariables.put("status_leverlist", getLeverList(session));
        pageVariables.put("status_display", Database.connected() ? "inline-table" : "none");
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("datasets", getDataSets(period));
        String content = PageGenerator.getPage(pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        HttpSession session = request.getSession();
        if (request.getParameter("status_apply") != null) {
            accountService.setAttribute(session,"status_todate", request.getParameter("status_todate"));
            accountService.setAttribute(session,"status_fromdate", request.getParameter("status_fromdate"));

            for (String checkboxName : checkboxes){
                String checkboxValueStr = (request.getParameter(checkboxName) == null) ? "false" : "true";
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
        }
        doGet(request, response);
    }

    public String getDeviceList(HttpSession session) {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);

        for (Device device : Devices.list){
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_note", device.getNote());
            pageVariables.put("check_name", device.getName() + "_check");
            pageVariables.put("check_value", "");
            pageVariables.put("value_name", "");
            pageVariables.put("value_note", "");

            ArrayList<Value> values = device.getValues();
            int i = 0;
            if (device.getValues().size() == 0)
                context += PageGenerator.getPage("items/device_value.html",pageVariables);
            else
            for (Value value : values){
                if (i>0){
                    pageVariables.put("device_model", "");
                    pageVariables.put("device_note", "");

                }
                pageVariables.put("check_display", (value.isPlotPossible() ? "inline" : "none"));

                String checkName = value.getName() + "_checkbox";
                String checkValue = accountService.getAttribute(session,checkName); // Читаем из сессии
                if (checkValue.equals("")) {
                    checkValue = "false"; // Значение при первой загрузке
                    accountService.setAttribute(session, checkName, checkValue);
                }
                pageVariables.put("check_value", Boolean.parseBoolean(checkValue) ? "checked" : "");
                pageVariables.put("check_name", checkName);
                pageVariables.put("value_name", (value.isStorable() ? value.getName() : ""));
                pageVariables.put("value_note", value.getNote().equals(device.getNote()) ? "" : value.getNote());
                context += PageGenerator.getPage("items/device_value.html",pageVariables);
                i++;
            }

        }
        return context;
    }

    public String getLeverList(HttpSession session) {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);

        for (Lever lever : Levers.list){
            Value value = lever.toValue();

            pageVariables.put("check_display", (value.isPlotPossible() ? "inline" : "none"));
            String checkName = value.getName() + "_checkbox";
            String checkValue = accountService.getAttribute(session,checkName); // Читаем из сессии
            if (checkValue.equals("")) {
                checkValue = "false"; // Значение при первой загрузке
                accountService.setAttribute(session, checkName, checkValue);
            }
            pageVariables.put("check_value", Boolean.parseBoolean(checkValue) ? "checked" : "");
            pageVariables.put("check_name", checkName);
            pageVariables.put("value_name", (value.isStorable() ? value.getName() : ""));
            if (value.getValueType() == ValueType.VOID)
                pageVariables.put("value_note", "<b>" + value.getNote() + "</b>");
            else
                pageVariables.put("value_note", value.getNote());
            context += PageGenerator.getPage("items/lever_value.html",pageVariables);
        }
        return context;
    }

    private String getDataSets(Period period){
        String context = "";
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isPlotPossible())
                    context +=(context.equals("") ? "" : ",") + "\n" + getDataSet(value, period);
        for (Lever lever : Levers.list)
            if (lever.toValue().isPlotPossible())
                context +=(context.equals("") ? "" : ",") + "\n" + getDataSet((Value)lever, period);
        return context;
    }

    private String getDataSet(Value value, Period period) {
        String[] booleans = {"false", "true"};
        int index = (value.getValueType() == ValueType.BOOLEAN) ? 1 : 0;

        String context = "'" + value.getName() + "':{";
        context += "label:'" + value.getNote() + "',\n";
        context += "lines:{show:" + booleans[index] + "},\n";
        context += "splines: {show: " + booleans[1 - index] + "},\n";
        context += "points: {show: " + booleans[1 - index] + "},\n";
        context += "data:[";
        ArrayList<DBValue> dbValues = value.getDataSet(period);
        boolean first = true;
        for (DBValue dbValue : dbValues){
            context += (first ? "" : ",") + "['"
                    + (dbValue.moment.getTime() / 1000) + "',"
                    + dbValue.value + "]";
             first = false;
        }
        context += "],\n";
        return context + "}";
    }

    public String getTaskList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        for (int index = 0; index < BotProcess.list.size(); index++){
            BotTask task = BotProcess.list.get(index);
            String status = task.getStatusString();
            if (status == null) continue;
            pageVariables.put("note", task.note);
            pageVariables.put("status", status);
            context += PageGenerator.getPage("items/control_task.html",pageVariables);
        }
        return context;
    }

}
