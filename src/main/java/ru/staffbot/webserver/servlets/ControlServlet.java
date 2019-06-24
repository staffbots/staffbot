package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.configs.Configs;
import ru.staffbot.utils.botprocess.BotProcess;
import ru.staffbot.utils.botprocess.BotProcessStatus;
import ru.staffbot.utils.botprocess.BotTask;
import ru.staffbot.utils.levers.Lever;
import ru.staffbot.utils.levers.Levers;
import ru.staffbot.utils.values.ValueType;
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
                        Configs config = new Configs(PageGenerator.fromCode(configName));
                        if (request.getParameter("control_save") != null) config.save();
                        if (request.getParameter("control_load") != null) config.load();
                        if (request.getParameter("control_delete") != null) config.delete();
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
            for (String configName : Configs.getList()) {
                context += "<option value=\"" + configName + "\">";
            }
        } catch (Exception exception) {
            context = "";
        }
        return context;
    }


}
