package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.configs.Configs;
import ru.staffbot.utils.levers.Lever;
import ru.staffbot.utils.levers.Levers;
import ru.staffbot.utils.tasks.TaskStatus;
import ru.staffbot.utils.tasks.Tasks;
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

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> pageVariables = new HashMap();

        for (TaskStatus status : TaskStatus.values()) {
            String statusName = "control_" + status.name().toLowerCase();
            if (status == TaskStatus.PAUSE)
                pageVariables.put(statusName, TaskStatus.START == Tasks.getStatus() ? "" : "disabled");
            else
                pageVariables.put(statusName, status == Tasks.getStatus() ? "disabled" : "");
        }
        pageVariables.put("control_display", Database.connected() ? "inline-table" : "none");
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("control_leverlist", getLeverList());
        pageVariables.put("control_configlist", getConfigList());
        String content = PageGenerator.getPage(pageType.getName() + ".html", pageVariables);

        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getParameter("control_apply") == null) {
            // Обработка управляющих кнопок (пуск, пауза, старт)
            for (TaskStatus status : TaskStatus.values())
                if (request.getParameter("control_" + status.name().toLowerCase()) != null)
                    Tasks.setStatus(status);
            // Обработка кнопок для работы с конфигурацией (сохранить, загрузить, удалить)
            String configName = request.getParameter("control_configname");
            if (configName != null)
                try {
                    Configs config = new Configs(configName);
                    if (request.getParameter("control_save") != null) config.save();
                    if (request.getParameter("control_load") != null) config.load();
                    if (request.getParameter("control_delete") != null) config.delete();
                } catch (Exception e) {}

        } else {
            for (Lever lever : Levers.list) {
                if (lever.getValueType() == ValueType.EMPTY) continue;
                String leverName = "control_" + lever.getName().toLowerCase();
                String leverValue = request.getParameter(leverName);
                lever.setValueFromString(leverValue);
            }
        }
        doGet(request, response);
    }

    public String getLeverList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("page_bg_color", page_bg_color);
        for (Lever lever : Levers.list){
            pageVariables.put("name", "control_" + lever.getName().toLowerCase());
            pageVariables.put("note", lever.getNote());
            pageVariables.put("value", lever.getValueAsString());
            pageVariables.put("size", lever.getStringValueSize());
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
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return context;
    }


}
