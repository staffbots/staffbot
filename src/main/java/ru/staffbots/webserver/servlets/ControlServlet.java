package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.tools.botprocess.BotProcess;
import ru.staffbots.tools.botprocess.BotProcessStatus;
import ru.staffbots.tools.levers.ButtonLever;
import ru.staffbots.tools.levers.Lever;
import ru.staffbots.tools.levers.Levers;
import ru.staffbots.tools.values.BooleanValue;
import ru.staffbots.tools.values.Value;
import ru.staffbots.tools.values.ValueType;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ControlServlet extends BaseServlet {

    public ControlServlet(AccountService accountService) {
        super(PageType.CONTROL, accountService);
    }

    // Вызывается при запросе страницы с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (isAccessDenied(request, response)) return;
        Map<String, Object> pageVariables = new HashMap();
        for (BotProcessStatus status : BotProcessStatus.values()) {
            String statusName = "control_" + status.name().toLowerCase();
            if (status == BotProcessStatus.PAUSE)
                pageVariables.put(statusName, BotProcessStatus.START == BotProcess.getStatus() ? "" : "disabled");
            else
                pageVariables.put(statusName, status == BotProcess.getStatus() ? "disabled" : "");
        }

        pageVariables.put("start_time", Long.toString(BotProcess.getStartTime()));
        pageVariables.put("page_bg_color", page_bg_color);
        pageVariables.put("control_leverlist", getLeverList());
        pageVariables.put("control_configlist", getConfigList());
        pageVariables.put("tasks_display", BotProcess.list.size() > 0 ? "inline-table" : "none");
        String content = FillTemplate("html/" + pageType.getName() + ".html", pageVariables);

        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String setName = request.getParameter("set");
        // Проверяем, не запрос ли это на обработку нажатия кнопки
        // вызовом control_button_onclick() из main.js
        if (setName != null) {
            if (isAccessDenied(request)) return;
            // Обработка нажатия кнопки ButtonLever
            for (Lever lever: Levers.list){
                Value value = lever.toValue();
                if (("control_" + value.getName()).equalsIgnoreCase(setName))
                    if (value.getValueType() == ValueType.VOID)
                    try {
                        ((ButtonLever)lever).onClick();
                        break;
                    } catch (Exception exception) {
                        // Игнорируем
                    }
            }
            return;
        }

        if (isAccessDenied(request, response)) return;

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
                        //configName = PageGenerator.fromCode(configName);
                        if (request.getParameter("control_save") != null) Database.configs.save(configName);
                        if (request.getParameter("control_load") != null) Database.configs.load(configName);
                        if (request.getParameter("control_delete") != null) Database.configs.delete(configName);
                    } catch (Exception e) {} finally {
                        BotProcess.reScheduleAll();
                    }
        } else {
            for (Lever lever : Levers.list) {
                Value value = lever.toValue();
                if (value.getValueType() == ValueType.VOID) continue;
                String leverName = "control_" + value.getName().toLowerCase();
                String leverValue = request.getParameter(leverName);
                if (value.getValueType() == ValueType.BOOLEAN)
                    if (leverValue == null) leverValue = BooleanValue.falseValueString;
                if (leverValue != null)
                    value.setFromString(leverValue);
            }
            BotProcess.reScheduleAll();
        }

        doGet(request, response);
    }

    public String getLeverList() {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        int maxSize = Levers.getMaxStringValueSize();
        for (Lever lever : Levers.list){
            if (!lever.toValue().isChangeable()) continue;
            pageVariables.put("page_bg_color", page_bg_color);
            pageVariables.put("name", "control_" + lever.toValue().getName().toLowerCase());
            String value = lever.toValue().toHtmlString();
            pageVariables.put("value", value);
            pageVariables.put("note", lever.toValue().getNote());
            pageVariables.put("size", maxSize);
            context += FillTemplate("html/" + lever.getTemplateFile(),pageVariables);
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
