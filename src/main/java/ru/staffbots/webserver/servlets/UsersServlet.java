package ru.staffbots.webserver.servlets;

import ru.staffbots.database.tables.users.User;
import ru.staffbots.database.tables.users.UserRole;
import ru.staffbots.database.tables.users.Users;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UsersServlet extends BaseServlet {

    public UsersServlet(AccountService accountService) {
        super(PageType.USERS, accountService);
        getParameters.put("role_list", (HttpServletRequest request) -> getRoleList(request));
        setParameters.put("apply_button", (HttpServletRequest request) -> buttonApplyClick(request));
        setParameters.put("delete_button", (HttpServletRequest request) -> buttonDeleteClick(request));
        doGet = (HttpServletRequest request, HttpServletResponse response) -> doGet(request, response);
    }

    // Вызывается при запросе странице с сервера
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        if (getResponse(request, response)) return;
        if (isAccessDenied(request, response)) return;

        String login = accountService.getUserLogin(request);
        Language language = accountService.getUserLanguage(request);

        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        ArrayList<User> userList = Users.getUserList();


        pageVariables.put("login_list", getLoginList(userList, login));

        String content = fillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    private String getLoginFromRequest(HttpServletRequest request){
        String radiobox = request.getParameter("users_radiobox");
        if (radiobox != null) {
            String login = request.getParameter(radiobox.equals("new") ? "new_login" : "select_login");
            accountService.setAttribute(request, "users_login", login);
        }
        return accountService.getAttribute(request, "users_login");
    }


    private boolean buttonApplyClick(HttpServletRequest request){
        String login = getLoginFromRequest(request);
        String role = request.getParameter("users_role");
        String password = request.getParameter("users_password");
        String languageCode = accountService.getUserLanguage(request).getCode();
        if (!login.equals(""))
            Users.setUser(new User(login, password, languageCode, role));
        return true;
    }

    // Обработка кнопок для работы с конфигурацией (удалить)
    private boolean buttonDeleteClick(HttpServletRequest request) {
        String login = getLoginFromRequest(request);
        Users.delete(login);
        return true;
    }

    private String getLoginList(ArrayList<User> userList, String selectedLogin) {
        String logins = "";
        for (User user : userList)
            logins += getLogin(user.login, user.login.equalsIgnoreCase(selectedLogin));
        return logins;
    }

    private String getLogin(String login, boolean selected) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("login_selected", selected ? "selected" : "");
        pageVariables.put("login_name", login);
        return fillTemplate("html/users/login.html", pageVariables);
    }

    private String getRoleList(HttpServletRequest request) {
        String login = request.getParameter("login_name");
        UserRole selectedRole = Users.getRole(login);
        Language language = accountService.getUserLanguage(request);
        String roles = "";
        for (UserRole role : UserRole.values())
            roles += getRole(language.getCode(), role, selectedRole == role);
        Map<String, Object> pageVariables = language.getSection(pageType.getName());
        pageVariables.put("role_list", roles);
        return fillTemplate("html/users/rolelist.html", pageVariables);
    }

    private String getRole(String languageCode, UserRole role, boolean selected) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("role_name", role.getName());
        pageVariables.put("role_selected", selected ? "selected" : "");
        pageVariables.put("role_description", role.getDescription(languageCode));
        return fillTemplate("html/users/role.html", pageVariables);
    }

}