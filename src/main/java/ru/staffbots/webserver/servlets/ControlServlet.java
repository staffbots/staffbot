package ru.staffbots.webserver.servlets;

import ru.staffbots.database.tables.LeversSets;
import ru.staffbots.tools.languages.Language;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *
 */
public class ControlServlet extends BaseServlet {

    public ControlServlet(AccountService accountService) {
        super(PageType.CONTROL, accountService);
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
        doGet = (HttpServletRequest request, HttpServletResponse response) -> doGet(request, response);
    }

    // Вызывается при запросе страницы с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (getResponse(request, response)) return;
        if (isAccessDenied(request, response)) return;
        //String login = accountService.getUserLogin(request);
        Language language = accountService.getUserLanguage(request);
        //System.out.println(login + " - " + language);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());

        for (TasksStatus status : TasksStatus.values()) {
            String variable = status.getName() + "_visible";
            if (status == TasksStatus.PAUSE)
                pageVariables.put(variable, TasksStatus.START == Tasks.getStatus() ? "" : "disabled");
            else
                pageVariables.put(variable, status == Tasks.getStatus() ? "disabled" : "");
        }

        pageVariables.put("start_time", Long.toString(Tasks.getStartTime()));
        pageVariables.put("lever_list", getLeverList());
        pageVariables.put("config_name", accountService.getAttribute(request, "config_name", ""));
//        pageVariables.put("config_name", request.getAttribute("config_name"));
        pageVariables.put("config_list", getConfigList());
        pageVariables.put("tasks_display", Tasks.list.size() > 0 ? "inline-table" : "none");

        String content = fillTemplate("html/" + pageType.getName() + ".html", pageVariables);

        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
//    @Override
//    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        if (isAccessDenied(request, response)) return;
 //       if (setRequest(request)) doGet(request, response);
  //  }

    private boolean buttonApplyClick(HttpServletRequest request){
        setLeverList(request);
        accountService.setAttribute(request,"config_name", "");
        Tasks.reScheduleAll();
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (сохранить)
    private boolean buttonSaveClick(HttpServletRequest request) {
        buttonApplyClick(request);
        String setName = accountService.setAttribute(request,"config_name");
        LeversSets.save(setName);
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (загрузить)
    private boolean buttonLoadClick(HttpServletRequest request) {
        buttonApplyClick(request);
        String setName = accountService.setAttribute(request,"config_name");
        LeversSets.load(setName);
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (удалить)
    private boolean buttonDeleteClick(HttpServletRequest request) {
        buttonApplyClick(request);
        String setName = request.getParameter("config_name");
        LeversSets.delete(setName);
        accountService.setAttribute(request,"config_name", "");
        return true;
    }

    // Обработка управляющих кнопок (пуск)
    private boolean buttonStartClick(HttpServletRequest request){
        buttonApplyClick(request);
        Tasks.setStatus(TasksStatus.START);
        return true;
    }

    // Обработка управляющих кнопок (пауза)
    private boolean buttonPauseClick(HttpServletRequest request){
        buttonApplyClick(request);
        Tasks.setStatus(TasksStatus.PAUSE);
        return true;
    }

    // Обработка управляющих кнопок (стоп)
    private boolean buttonStopClick(HttpServletRequest request){
        buttonApplyClick(request);
        Tasks.setStatus(TasksStatus.STOP);
        return true;
    }

    // Проверяем, не запрос ли это на обработку нажатия кнопки
    // вызовом control_button_onclick() из .js
    private boolean buttonLeverClick(HttpServletRequest request) {
        for (ButtonLever buttonLever: Levers.getButtonList())
            if (request.getParameter(buttonLever.getName().toLowerCase() + "_lever") != null){
                buttonLever.onClick();
                return true;
            }
        return false;
    }

    private Boolean setLeverList(HttpServletRequest request) {
        for (Lever lever : Levers.getList()) {
            if (!lever.isChangeable()) continue;
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
        for (Lever lever : Levers.getList()){
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
        for (String configName : LeversSets.getList())
            context += getConfig(configName);
        return context;
    }

    private String getConfig(String configName) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("config_name", configName);
        return fillTemplate("html/control/config.html", pageVariables);
    }


}
