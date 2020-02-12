package ru.staffbots.webserver;

import ru.staffbots.database.Database;
import ru.staffbots.database.users.User;
import ru.staffbots.database.users.Users;
import ru.staffbots.database.users.UserRole;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    /**
     * sessionId -> login
     */
    private Map<String, String> sessions = new HashMap<>();

    public int verifyUser(String login, String password){
        return  Users.verify(login, password);
    }

    public void addSession(HttpSession session, String login){
        forgetSession(session);
        String sessionId = session.getId();
        sessions.put(sessionId, login);
    }

    public void forgetSession(HttpSession session){
        String sessionId = session.getId();
        if (sessions.containsKey(sessionId)) {
            sessions.remove(sessionId);
            while(session.getAttributeNames().hasMoreElements())
                session.removeAttribute(session.getAttributeNames().nextElement());
        }
    }

    public String getUserLogin(HttpSession session){
        String sessionId = session.getId();
        return sessions.containsKey(sessionId) ? sessions.get(sessionId) : null;
    }

    public int getUserAccessLevel(String login){
        UserRole role = Users.isAdmin(login) ? UserRole.ADMIN : Database.users.getRole(login);
        return (role == null) ? -1 : role.getAccessLevel();
    }

    public int getUserAccessLevel(HttpServletRequest request){
        if (request == null ) return -1;
        String login = getUserLogin(request.getSession());
        if (login == null ) return -1;
        return getUserAccessLevel(login);
    }

    public String getAttribute(HttpServletRequest request, String attribute, String defaultValue){
        Object attributeValue = request.getSession().getAttribute(attribute);
        String value = (attributeValue == null) ? "" : attributeValue.toString();
        if (value.isEmpty())
            value = (defaultValue == null) ? request.getParameter(attribute) : defaultValue;
        if (value == null) value = "";
        setAttribute(request, attribute, value);
        return value;
    }

    public String getAttribute(HttpServletRequest request, String attribute){
        return getAttribute(request, attribute, null);
    }

    public void setAttribute(HttpServletRequest request, String attribute, String value){
        request.getSession().setAttribute(attribute, value);
    }

    public String setAttribute(HttpServletRequest request, String attribute){
        String value = request.getParameter(attribute);
        setAttribute(request, attribute, value);
        return value;
    }

}
