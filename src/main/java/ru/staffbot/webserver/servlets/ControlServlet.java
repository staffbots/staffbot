package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.tools.botprocess.BotProcess;
import ru.staffbot.tools.botprocess.BotProcessStatus;
import ru.staffbot.tools.levers.Lever;
import ru.staffbot.tools.levers.Levers;
import ru.staffbot.tools.values.ValueType;
import ru.staffbot.webserver.AccountService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ControlServlet extends MainServlet {

    public ControlServlet(AccountService accountService) {
        super(PageType.CONTROL, accountService);
    }

    // Вызывается при запросе страницы с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();
        for (BotProcessStatus status : BotProcessStatus.values()) {
            String statusName = "control_" + status.name().toLowerCase();
            if (status == BotProcessStatus.PAUSE)
                pageVariables.put(statusName, BotProcessStatus.START == BotProcess.getStatus() ? "" : "disabled");
            else
                pageVariables.put(statusName, status == BotProcess.getStatus() ? "disabled" : "");
        }

        pageVariables.put("start_time", Long.toString(BotProcess.getStartTime()));
        pageVariables.put("control_display", Database.connected() ? "inline-table" : "none");
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("control_leverlist", getLeverList());
        pageVariables.put("control_configlist", getConfigList());
        pageVariables.put("tasks_display", BotProcess.list.size() > 0 ? "inline-table" : "none");
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);

        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("control_apply") == null) {
            // Обработка управляющих кнопок (пуск, пауза, старт)
            for (BotProcessStatus status : BotProcessStatus.values())
                if (request.getParameter("control_" + status.name().toLowerCase()) != null)
                    BotProcess.setStatus(status);

            // Обработка кнопок для работы с конфигурацией (сохранить, загрузить, удалить)
            String configName = request.getParameter("control_configname");
            if (configName != null)
                if (!configName.equals(""))
                    try {
                        configName = PageGenerator.fromCode(configName);
                        if (request.getParameter("control_save") != null) Database.configs.save(configName);
                        if (request.getParameter("control_load") != null) Database.configs.load(configName);
                        if (request.getParameter("control_delete") != null) Database.configs.delete(configName);
                    } catch (Exception e) {} finally {
                        BotProcess.reScheduleAll();
                    }
        } else {
            for (Lever lever : Levers.list) {
                if (lever.toValue().getValueType() == ValueType.VOID) continue;
                String leverName = "control_" + lever.toValue().getName().toLowerCase();
                String leverValue = request.getParameter(leverName);
                lever.toValue().setValueFromString(leverValue);
            }
            BotProcess.reScheduleAll();
        }

        doGet(request, response);
    }


    public String getLeverList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);
        for (Lever lever : Levers.list){
            if (!lever.toValue().isChangeable()) continue;
            pageVariables.put("name", "control_" + lever.toValue().getName().toLowerCase());
            String value = lever.toValue().getValueAsString();
            if (lever.toValue().getValueType() == ValueType.BOOLEAN)
                value = (lever.toValue().get() == 0) ? "" : "checked";
            pageVariables.put("value", value);
            pageVariables.put("note", lever.toValue().getNote());
            pageVariables.put("size", lever.toValue().getStringValueSize());
            context += PageGenerator.getPage(lever.getTemplateFile(),pageVariables);
        }
        return context;
    }

    public String getConfigList() {
        String context = "";
        try {
            for (String configName : Database.configs.getList()) {
                context += "<option value=\"" + configName + "\">";
            }
        } catch (Exception exception) {
            context = "";
        }
        return context;
    }


}
