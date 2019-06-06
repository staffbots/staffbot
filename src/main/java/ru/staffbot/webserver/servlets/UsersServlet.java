package ru.staffbot.webserver.servlets;

import ru.staffbot.database.Database;
import ru.staffbot.database.users.User;
import ru.staffbot.database.users.UserRole;
import ru.staffbot.webserver.AccountService;

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
public class UsersServlet extends MainServlet {

    public UsersServlet(AccountService accountService) {
        super(PageType.USERS, accountService);
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Database.connected()){
            response.sendRedirect("/about");
            return;
        }
        Map<String, Object> pageVariables = new HashMap();
        HttpSession session = request.getSession();
        ArrayList<User> userList = accountService.userDAO.getUserList();
        String radiobox = accountService.getAttribute(session,"users_radiobox");
        if (radiobox.equals("")) radiobox = request.getParameter("users_radiobox");
        if (radiobox == null) radiobox = "new";
        boolean newlogin = radiobox.equals("new");
        if (userList.size() == 0) newlogin = true;


        String login = accountService.getAttribute(session, "users_login");
        User user = accountService.userDAO.getUser(login);
        UserRole role = (user == null) ? UserRole.GUEST : user.role;

        pageVariables.put("users_role", UserRole.GUEST.getName());
        pageVariables.put("users_new_login","");
        pageVariables.put("users_radiobox_new", newlogin ? "checked" : "" );
        pageVariables.put("users_radiobox_old", !newlogin ? "checked" : "");
        pageVariables.put("users_empty_enabled", (userList.size() == 0) ? "disabled" : "");
        pageVariables.put("users_new_enabled", newlogin ? "disabled" : "");
        pageVariables.put("users_new_disabled", newlogin ? "" : "disabled");
        pageVariables.put("users_login_list", getLoginList(userList, login));
        pageVariables.put("users_role_list", getRoleList(role.getAccessLevel()));
        pageVariables.put("site_bg_color", site_bg_color);
        pageVariables.put("page_bg_color", page_bg_color);
        String content = PageGenerator.getPage(pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        HttpSession session = request.getSession();
        String radiobox = request.getParameter("users_radiobox");
        boolean newlogin = radiobox.equals("new");
        String login = request.getParameter(newlogin ? "users_new_login" : "users_select_login");
        accountService.setAttribute(session, "users_login", login);
        String role = request.getParameter("users_role");
        accountService.setAttribute(session, "users_radiobox", radiobox);
        if (request.getParameter("users_apply") != null){
            String password = request.getParameter("users_password");
            accountService.userDAO.setUser(new User(login, password, UserRole.valueByName(role)));
        }
        if (request.getParameter("users_delete") != null){
            accountService.userDAO.delete(login);
        }
        doGet(request, response);
    }

    public String getLoginList(ArrayList<User> userList, String selectedLogin) {
        String context = "";// "<option>" + "</option>";
        for (User user : userList){
            context += "<option " +
                    ((user.login.equalsIgnoreCase(selectedLogin)) ? "selected" : "") +
                    ">" + user.login + "</option>";
        }
        return context;
    }

    public String getRoleList(int accessLevel) {
        String context = "";
        for (UserRole userRole : UserRole.values()) {
            context += "<option " +
                    ((accessLevel == userRole.getAccessLevel()) ? "selected" : "") +
                     ">" + userRole + "</option>";
        }
        return context;
    }

}