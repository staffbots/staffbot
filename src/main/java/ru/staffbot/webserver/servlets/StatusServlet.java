package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.Period;
import ru.staffbot.utils.devices.Device;
import ru.staffbot.utils.devices.Devices;
import ru.staffbot.utils.values.Value;
import ru.staffbot.utils.values.ValueType;
import ru.staffbot.webserver.AccountService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sysadmin on 25.05.2017.
 */
public class StatusServlet extends MainServlet {

    private ArrayList<String> checkboxes;

    public StatusServlet(AccountService accountService) {
        super(PageType.STATUS, accountService);
        checkboxes = new ArrayList<>(Arrays.asList("status_fromdate_on", "status_todate_on"));
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();
        HttpSession session = request.getSession();

        String getName = request.getParameter("get");
        if (getName != null) {
           // System.out.println("Запрос " + getName);
            for (Device device : Devices.list)
                for (Value value : device.getValues())
                    if (getName.equals(value.getName())) {
                        response.getWriter().println(value.getValueAsString());
                        response.setContentType("text/html; charset=utf-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        return;
                    }
            return;
        }
        //System.out.println("Запрос страницы");

        for (String checkboxName : checkboxes) {
            String checkboxValueStr = accountService.getAttribute(session, checkboxName); // Читаем из сессии
            if (checkboxValueStr.equals("")) {
                checkboxValueStr = "true"; // Значение при первой загрузке
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");
        }

        Period period = new Period(Journal.DATE_FORMAT);

        String toDateStr = accountService.getAttribute(session,"status_todate");
        if (toDateStr.equals("")) toDateStr = request.getParameter("status_todate");

        String fromDateStr = accountService.getAttribute(session,"status_fromdate");
        if (fromDateStr.equals("")) fromDateStr = request.getParameter("status_fromdate");

        period.set(fromDateStr, toDateStr);


        pageVariables.put("dateformat", Journal.DATE_FORMAT.getFormat());
        pageVariables.put("status_fromdate", period.getFromDateAsString());
        pageVariables.put("status_todate", period.getToDateAsString());
        pageVariables.put("status_devicelist", getDeviceList(session));
        pageVariables.put("status_display", Database.connected() ? "inline-table" : "none");
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("site_bg_color", site_bg_color);
        String content = PageGenerator.getPage(pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        HttpSession session = request.getSession();
        if (request.getParameter("status_apply") != null) {
            for (Device device : Devices.list)
                for (Value value : device.getValues()){
                    String checkName = value.getName() + "_checkbox";
                    String checkValue = (request.getParameter(checkName) == null) ? "false" : "true";
                    accountService.setAttribute(session, checkName, checkValue);
                }
            accountService.setAttribute(session,"status_todate", request.getParameter("journal_todate"));
            accountService.setAttribute(session,"status_fromdate", request.getParameter("journal_fromdate"));
            for (String checkboxName : checkboxes){
                String checkboxValueStr = request.getParameter(checkboxName); // Читаем со страницы
                checkboxValueStr = (checkboxValueStr == null) ? "false" : "true";
                accountService.setAttribute(session, checkboxName, checkboxValueStr);
            }
        }
        doGet(request, response);
    }

    public String getDeviceList(HttpSession session) {
        //udate_value(value_name)
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);

        String radiobox = accountService.getAttribute(session,"status_radiobox"); // Читаем из сессии
        for (Device device : Devices.list){
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_note", device.getNote());
            pageVariables.put("status_display", "none");
            pageVariables.put("radio_value", "");
            pageVariables.put("check_name", device.getName() + "_check");
            pageVariables.put("check_value", "");
            pageVariables.put("value_name", "");
            //pageVariables.put("value", "");
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
                pageVariables.put("radio_display", (value.dbStorage &&
                        (value.getValueType() == ValueType.BOOLEAN ) ? "inline" : "none"));
                pageVariables.put("check_display", (value.dbStorage &&
                        (value.getValueType() == ValueType.DOUBLE ||
                         value.getValueType() == ValueType.LONG  ||
                         value.getValueType() == ValueType.BOOLEAN ) ? "inline" : "none"));

                String checkName = value.getName() + "_checkbox";
                String checkValue = accountService.getAttribute(session,checkName); // Читаем из сессии
                if (checkValue.equals("")) {
                    checkValue = "false"; // Значение при первой загрузке
                    accountService.setAttribute(session, checkName, checkValue);
                }
                pageVariables.put("radio_value", checkName.equalsIgnoreCase(radiobox) ? "checked" : "");
                pageVariables.put("check_value", Boolean.parseBoolean(checkValue) ? "checked" : "");
                pageVariables.put("check_name", checkName);
                pageVariables.put("value_name", (value.dbStorage ? value.getName() : ""));
                pageVariables.put("value_note", value.getNote());
               // pageVariables.put("value", value.getValueAsString());
                context += PageGenerator.getPage("items/device_value.html",pageVariables);
                i++;
            }

        }
        return context;
    }

}
