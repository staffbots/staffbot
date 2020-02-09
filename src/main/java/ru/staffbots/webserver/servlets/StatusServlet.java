package ru.staffbots.webserver.servlets;

import ru.staffbots.database.DBValue;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.dates.Period;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.tasks.Task;
import ru.staffbots.tools.devices.Device;
import ru.staffbots.tools.devices.Devices;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.DoubleValue;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueType;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 *
 */
public class StatusServlet extends BaseServlet {

    private ArrayList<String> checkboxes =  new ArrayList<>();

    public StatusServlet(AccountService accountService) {
        super(PageType.STATUS, accountService);
        checkboxes.add("plot_fromdate_checkbox");
        checkboxes.add("plot_todate_checkbox");
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isPlotPossible())
                    checkboxes.add(value.getName() + "_checkbox");
        for (Lever lever : Levers.list)
            if (lever.toValue().isPlotPossible())
                checkboxes.add(lever.toValue().getName() + "_checkbox");

        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));

        getParameters.put("tasklist", (HttpServletRequest request) -> getTaskList());
        getParameters.put("processstatus", (HttpServletRequest request) -> Tasks.getStatus().getDescription());
        for (Lever lever : Levers.list)
            if (!lever.isGroup())
                getParameters.put(lever.toValue().getName(), (HttpServletRequest request) -> lever.toValue().toViewString());
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                getParameters.put(value.getName(), (HttpServletRequest request) -> value.toViewString());
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        if (getResponse(request, response)) return;

        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());

        Period period = new Period(Journal.dateFormat);
        String toDateStr = accountService.getAttribute(request,"plot_todate");
        String fromDateStr = accountService.getAttribute(request,"plot_fromdate");
        period.set(fromDateStr, toDateStr);

        for (String checkboxName : checkboxes) {
            String checkboxValueStr = accountService.getAttribute(request, checkboxName); // Читаем из сессии
            if (checkboxValueStr.equals("")) {
                checkboxValueStr = "true"; // Значение при первой загрузке
                for (Lever lever : Levers.list)
                    if (lever.toValue().isPlotPossible())
                        if (checkboxName.equals(lever.toValue().getName() + "_checkbox")){
                            checkboxValueStr = "false"; // Значение при первой загрузке для lever
                            break;
                        }
                accountService.setAttribute(request, checkboxName, checkboxValueStr);
            }
            Boolean checkboxValue = Boolean.parseBoolean(checkboxValueStr);
            pageVariables.put(checkboxName, checkboxValue ? "checked" : "");

            if (checkboxName.equals("plot_fromdate_checkbox") && checkboxValue && (period.getFromDate() == null))
                period.initFromDate();

            if (checkboxName.equals("plot_todate_checkbox") && checkboxValue && (period.getToDate() == null))
                period.initToDate();
        }

        pageVariables.put("tasks_display", Tasks.list.size() > 0 ? "table-row" : "none");
        pageVariables.put("plot_display", checkboxes.size() > 2 ? "inline-table" : "none");
        pageVariables.put("start_time", Long.toString(Tasks.getStartTime()));
        pageVariables.put("date_format", Journal.dateFormat.getFormat());
        pageVariables.put("plot_fromdate", period.getFromDateAsString());
        pageVariables.put("plot_todate", period.getToDateAsString());
        pageVariables.put("device_list", getDeviceList(request));
        pageVariables.put("lever_list", getLeverList(request));
        pageVariables.put("page_color", pageColor);
        pageVariables.put("site_color", siteColor);
        pageVariables.put("datasets_value", getDataSets(period));

        String content = fillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        if (isAccessDenied(request, response)) return;
        setRequest(request);
        doGet(request, response);
    }

    private boolean buttonApplyClick(HttpServletRequest request){
        for (String checkboxName : checkboxes){
            String checkboxValueStr = (request.getParameter(checkboxName) == null) ? "false" : "true";
            accountService.setAttribute(request, checkboxName, checkboxValueStr);
        }
        return true;
    }

    private String getDeviceList(HttpServletRequest request) {
        String context = "";
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        pageVariables.put("page_color", pageColor);
        String htmlPath = "html/status/device/";
        for (Device device : Devices.list){
            pageVariables.put("device_url", device.getURL());
            pageVariables.put("device_model", device.getModel());
            pageVariables.put("device_note", device.getNote());
            pageVariables.put("check_name", device.getName() + "_check");
            pageVariables.put("check_value", "");
            pageVariables.put("value_name", "");
            pageVariables.put("value_note", "");

            ArrayList<Value> values = device.getValues();
            int i = 0;
            if (device.getValues().size() == 0)
                context += fillTemplate(htmlPath + "value.html",pageVariables);
            else
            for (Value value : values){
                if (i>0){
                    pageVariables.put("device_model", "");
                    pageVariables.put("device_note", "");

                }
                pageVariables.put("check_display", (value.isPlotPossible() ? "inline" : "none"));

                String checkName = value.getName() + "_checkbox";
                String checkValue = accountService.getAttribute(request,checkName, "false"); // Читаем из сессии
//                if (checkValue.equals("")) {
//                    checkValue = "false"; // Значение при первой загрузке
//                    accountService.setAttribute(request.getSession(), checkName, checkValue);
//                }
                pageVariables.put("check_value", Boolean.parseBoolean(checkValue) ? "checked" : "");
                pageVariables.put("check_name", checkName);
                pageVariables.put("value_name", value.getName());
                pageVariables.put("value_note", value.getNote().equals(device.getNote()) ? "" : value.getNote());
                context += fillTemplate(htmlPath + "value.html",pageVariables);
                i++;
            }

        }
        if (!context.isEmpty())
            context = fillTemplate(htmlPath + "title.html",pageVariables) + context;
        return context;
    }

    private String getLeverList(HttpServletRequest request) {
        String context = "";
        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        pageVariables.put("page_color", pageColor);
        String htmlPath = "html/status/lever/";
        for (Lever lever : Levers.list){
            if (lever.isButton()) continue;
            Value value = lever.toValue();
            String checkName = value.getName() + "_checkbox";

            pageVariables.put("check_display", (value.isPlotPossible() ? "inline" : "none"));
            String checkValue =  !checkboxes.contains(checkName) ? "" :
                accountService.getAttribute(request,checkName,"false"); // Читаем из сессии
//            if (checkValue.equals("")) {
//                checkValue = "false"; // Значение при первой загрузке
//                accountService.setAttribute(session, checkName, checkValue);
//            }
            pageVariables.put("check_value", Boolean.parseBoolean(checkValue) ? "checked" : "");
            pageVariables.put("check_name", checkName);

            //pageVariables.put("value_name", (value.isStorable() ? value.getName() : ""));
            pageVariables.put("value_name", value.getName());
            if (lever.isGroup())
                pageVariables.put("value_note", "<b>" + value.getNote() + "</b>");
            else
                pageVariables.put("value_note", value.getNote());
            context += fillTemplate(htmlPath + "value.html",pageVariables);
        }
        if (!context.isEmpty())
            context = fillTemplate(htmlPath + "title.html",pageVariables) + context;

        return context;
    }

    private String getDataSets(Period period){
        String context = "";
        int numberOfValue = 0;
        for (Lever lever : Levers.list)
            if (lever.toValue().isPlotPossible())
                context +=(context.equals("") ? "" : ",") + "\n" + getDataSet((Value)lever, period, numberOfValue++);
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isPlotPossible())
                    context +=(context.equals("") ? "" : ",") + "\n" + getDataSet(value, period, numberOfValue++);
        return context;
    }

    private String getDataSet(Value value, Period period, int numberOfValue) {
        String[] booleans = {"false", "true"};
        int index = (value.getValueType() == ValueType.BOOLEAN) ? 1 : 0;
        int precision = (value.getValueType() == ValueType.DOUBLE) ? ((DoubleValue) value).precision : 0;

        String context = "'" + value.getName() + "':{";
        context += "label:'" + value.getNote() + "',\n";
        context += "lines:{show:" + booleans[index] + "},\n";
        context += "splines: {show: " + booleans[1 - index] + "},\n";
        context += "points: {show: " + booleans[1 - index] + "},\n";
        context += "color: 'hsl(" + hue[numberOfValue] + ",80%,50%)',\n";
        context += "precision: " + precision + ",\n";
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

    private String getTaskList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        for (int index = 0; index < Tasks.list.size(); index++){
            Task task = Tasks.list.get(index);
            String status = task.getStatusString();
            if (status == null) continue;
            pageVariables.put("task_note", task.note);
            pageVariables.put("task_status", status);
            context += fillTemplate("html/status/task.html",pageVariables);
        }
        return context;
    }

    // Количество значенийй, по которым возможно построить график
    private int plotValueCount = getPlotValueCount();

    // Расчёт количества значенийй, по которым возможно построить график
    private int getPlotValueCount(){
        int count = 0;
        for (Device device : Devices.list)
            for (Value value : device.getValues())
                if (value.isPlotPossible())
                    count++;
        for (Lever lever : Levers.list)
            if (lever.toValue().isPlotPossible())
                count++;
        return count;
    }

    // Насыщенности
    private double[] hue = getRandomHue(plotValueCount);

    // Возращает масиив размером length с перемешанными в нём случайными значениями (насыщенности) от 0 до 360
    private double[] getRandomHue(int length) {
        ArrayList<Integer> random = new ArrayList<>();
        ArrayList<Integer> resource = new ArrayList<>();
        for (int i = 0; i < length; i++)
            resource.add(i);
        for (int i = 0; i < length; i++){
            int k = (int) Math.floor(Math.random() * (length - i));
            random.add(resource.get(k));
            resource.remove(k);
        }
        double[] result = new double[length];
        for (int i = 0; i < length; i++)
            result[i] = 360 * random.get(i) / plotValueCount;
        return result;
    }


}
