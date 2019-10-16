package ru.staffbots.webserver.servlets;

import ru.staffbots.database.users.User;
import ru.staffbots.database.users.UserRole;
import ru.staffbots.webserver.AccountService;
import ru.staffbots.webserver.PageType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UsersServlet extends BaseServlet {

    public UsersServlet(AccountService accountService) {
        super(PageType.USERS, accountService);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        if (isAccessDenied(request, response)) return;

        Map<String, Object> pageVariables = new HashMap();
        HttpSession session = request.getSession();
        ArrayList<User> userList = accountService.users.getUserList();

        String login = accountService.getAttribute(session, "users_login");
        User user = accountService.users.getUser(login);
        UserRole role = (user == null) ? UserRole.INSPECTOR : user.role;

        pageVariables.put("users_role", UserRole.INSPECTOR.getName());
        pageVariables.put("users_empty_enabled", (userList.size() == 0) ? "disabled" : "");
        pageVariables.put("users_login_list", getLoginList(userList, login));
        pageVariables.put("users_role_list", getRoleList(role.getAccessLevel()));
        String content = FillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException{

        if (isAccessDenied(request, response)) return;

        HttpSession session = request.getSession();
        String radiobox = request.getParameter("users_radiobox");
        boolean newlogin = radiobox.equals("new");
        String login = request.getParameter(newlogin ? "users_new_login" : "users_select_login");
        accountService.setAttribute(session, "users_login", login);
        String role = request.getParameter("users_role");
        accountService.setAttribute(session, "users_radiobox", radiobox);
        if (request.getParameter("users_apply") != null){
            String password = request.getParameter("users_password");
            System.out.println("!!! Перехвачен пароль: " + password);
            if (!login.equals(""))
                accountService.users.setUser(new User(login, password, UserRole.valueByName(role)));
        }
        if (request.getParameter("users_delete") != null){
            accountService.users.delete(login);
        }
        doGet(request, response);
    }

    private String getLoginList(ArrayList<User> userList, String selectedLogin) {
        String context = "";// "<option>" + "</option>";
        for (User user : userList){
            context += "<option " +
                    ((user.login.equalsIgnoreCase(selectedLogin)) ? "selected" : "") +
                    ">" + user.login + "</option>";
        }
        return context;
    }

    private String getRoleList(int accessLevel) {
        String context = "";
        for (UserRole userRole : UserRole.values()) {
            context += "<option value=" + userRole + " " +
                    ((accessLevel == userRole.getAccessLevel()) ? "selected" : "") +
                     ">" + userRole.getDescription() + "</option>";
        }
        return context;
    }

}