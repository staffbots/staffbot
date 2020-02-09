package ru.staffbots.webserver.servlets;

import ru.staffbots.database.Database;
import ru.staffbots.database.users.User;
import ru.staffbots.tools.Translator;
import ru.staffbots.tools.tasks.Tasks;
import ru.staffbots.tools.tasks.TasksStatus;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ControlServlet extends BaseServlet {

    public ControlServlet(AccountService accountService) {
        super(PageType.CONTROL, accountService);
        //getParameters.put("lever_list", (String nullValue) -> getLeverList());
        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));
        setParameters.put("save_button", (HttpServletRequest request) -> buttonSaveClick(request));
        setParameters.put("load_button", (HttpServletRequest request) -> buttonLoadClick(request));
        setParameters.put("delete_button", (HttpServletRequest request) -> buttonDeleteClick(request));
        setParameters.put("start_button", (HttpServletRequest request) -> buttonStartClick(request));
        setParameters.put("pause_button", (HttpServletRequest request) -> buttonPauseClick(request));
        setParameters.put("stop_button", (HttpServletRequest request) -> buttonStopClick(request));
        for (ButtonLever buttonLever: Levers.getButtonList())
            setParameters.put(buttonLever.getName().toLowerCase() + "_lever",
                    (HttpServletRequest request) -> buttonLeverClick(request));
    }

    // Вызывается при запросе страницы с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        if (getResponse(request, response)) return;

        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());

        for (TasksStatus status : TasksStatus.values()) {
            String variable = status.getName() + "_visible";
            if (status == TasksStatus.PAUSE)
                pageVariables.put(variable, TasksStatus.START == Tasks.getStatus() ? "" : "disabled");
            else
                pageVariables.put(variable, status == Tasks.getStatus() ? "disabled" : "");
        }

        pageVariables.put("start_time", Long.toString(Tasks.getStartTime()));
        pageVariables.put("lever_list", getLeverList());
        pageVariables.put("config_name", accountService.getAttribute(request, "config_name"));
        pageVariables.put("config_list", getConfigList());
        pageVariables.put("tasks_display", Tasks.list.size() > 0 ? "inline-table" : "none");

        String content = fillTemplate("html/" + pageType.getName() + ".html", pageVariables);

        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        setLeverList(request);
        accountService.setAttribute(request,"config_name", "");
        Tasks.reScheduleAll();
        setRequest(request);
        doGet(request, response);
    }

    private boolean buttonApplyClick(HttpServletRequest request){
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (сохранить)
    private boolean buttonSaveClick(HttpServletRequest request) {
        String configName = accountService.setAttribute(request,"config_name");
        Database.configs.save(configName);
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (загрузить)
    private boolean buttonLoadClick(HttpServletRequest request) {
        String configName = accountService.setAttribute(request,"config_name");
        Database.configs.load(configName);
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (удалить)
    private boolean buttonDeleteClick(HttpServletRequest request) {
        String configName = request.getParameter("config_name");
        Database.configs.delete(configName);
        return true;
    }

    // Обработка управляющих кнопок (пуск)
    private boolean buttonStartClick(HttpServletRequest request){
        Tasks.setStatus(TasksStatus.START);
        return true;
    }

    // Обработка управляющих кнопок (пауза)
    private boolean buttonPauseClick(HttpServletRequest request){
        Tasks.setStatus(TasksStatus.PAUSE);
        return true;
    }

    // Обработка управляющих кнопок (стоп)
    private boolean buttonStopClick(HttpServletRequest request){
        Tasks.setStatus(TasksStatus.STOP);
        return true;
    }

    // Проверяем, не запрос ли это на обработку нажатия кнопки
    // вызовом control_button_onclick() из base.js
    private boolean buttonLeverClick(HttpServletRequest request) {
        for (ButtonLever buttonLever: Levers.getButtonList())
            if (request.getParameter(buttonLever.getName().toLowerCase() + "_lever") != null){
                buttonLever.onClick();
                return true;
            }
        return false;
    }

    private Boolean setLeverList(HttpServletRequest request) {
        for (Lever lever : Levers.list) {
            Value value = lever.toValue();
            if (value.getValueType() == ValueType.VOID) continue;
            String leverName = value.getName().toLowerCase() + "_lever";
            String leverValue = request.getParameter(leverName);
            if (value.getValueType() == ValueType.BOOLEAN)
                if (leverValue == null) leverValue = BooleanValue.falseValueString;
            if (leverValue != null)
                value.setFromString(leverValue);
        }
        return true;
    }

    private String getLeverList()  {
        String context = "";
        Map<String, Object> pageVariables = new HashMap();
        int maxSize = Levers.getMaxStringValueSize();
        for (Lever lever : Levers.list){
            if (!lever.isChangeable()) continue;
            pageVariables.put("lever_name", lever.getName().toLowerCase() + "_lever");
            String value = lever.toValue().toHtmlString();
            pageVariables.put("lever_value", value);
            pageVariables.put("lever_note", lever.toValue().getNote());
            pageVariables.put("lever_size", maxSize);
            context += fillTemplate("html/" + lever.getTemplateFile(), pageVariables);
        }
        return context;
    }

    private String getConfigList() {
        String context = "";
        for (String configName : Database.configs.getList())
            context += getConfig(configName);
        return context;
    }

    private String getConfig(String configName) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("config_name", configName);
        return fillTemplate("html/control/config.html", pageVariables);
    }


}
