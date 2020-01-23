package ru.staffbots.webserver.servlets;

import ru.staffbots.database.users.User;
import ru.staffbots.database.users.UserRole;
import ru.staffbots.tools.Translator;
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
        getParameters.put("rolelist", (String login) -> getRoleList(accountService.users.getRole(login)));
    }

    // Вызывается при запросе странице с сервера
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAccessDenied(request, response)) return;
        if (getResponse(request, response)) return;

        Map<String, Object> pageVariables = Translator.getSection(pageType.getName());
        ArrayList<User> userList = accountService.users.getUserList();

        String login = accountService.getAttribute(request.getSession(), "login");

        pageVariables.put("role", UserRole.defaultRole.getName());
        pageVariables.put("loginlist", getLoginList(userList, login));
        pageVariables.putAll(Translator.getSection(pageType.getName()));

        String content = FillTemplate("html/" + pageType.getName()+".html", pageVariables);
        super.doGet(request, response, content);
    }

    // Вызывается при отправке страницы на сервер
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        if (isAccessDenied(request, response)) return;

        HttpSession session = request.getSession();

        String radiobox = request.getParameter("radiobox");
        boolean newlogin = radiobox.equals("new");
        accountService.setAttribute(session, "radiobox", radiobox);

        String login = request.getParameter(newlogin ? "newlogin" : "selectlogin");
        accountService.setAttribute(session, "login", login);

        String role = request.getParameter("role");
        if (request.getParameter("apply") != null){
            String password = request.getParameter("password");
            if (!login.equals(""))
                accountService.users.setUser(new User(login, password, role));
        }
        if (request.getParameter("delete") != null){
            accountService.users.delete(login);
        }
        doGet(request, response);
    }

    private String getLoginList(ArrayList<User> userList, String selectedLogin) {
        String logins = "";
        for (User user : userList)
            logins += getLogin(user.login, user.login.equalsIgnoreCase(selectedLogin));
        return logins;
    }

    private String getLogin(String login, boolean selected) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("selected", selected ? "selected" : "");
        pageVariables.put("login", login);
        return FillTemplate("html/users/login.html", pageVariables);
    }

    private String getRoleList(UserRole selectedRole) {
        String roles = "";
        for (UserRole role : UserRole.values())
            roles += getRole(role, selectedRole == role);
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("roles", roles);
        pageVariables.putAll(Translator.getSection(pageType.getName()));
        return FillTemplate("html/users/rolelist.html", pageVariables);
    }

    private String getRole(UserRole role, boolean selected) {
        Map<String, Object> pageVariables = new HashMap();
        pageVariables.put("name", role.getName());
        pageVariables.put("selected", selected ? "selected" : "");
        pageVariables.put("description", role.getDescription());
        return FillTemplate("html/users/role.html", pageVariables);
    }

}