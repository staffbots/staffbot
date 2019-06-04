package ru.staffbot.webserver;

import ru.staffbot.database.users.Users;
import ru.staffbot.database.users.UserRole;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    // Карта сессий sessionId -> login
    private Map<String, String> sessions = new HashMap<>();

    public Users userDAO = new Users();


    public int verifyUser(String login, String password){
        if (userDAO.isAdmin(login))
            if (WebServer.PASSWORD.equals(password))
                return UserRole.ADMIN.getAccessLevel();
        return userDAO.verify(login, password);
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

    public UserRole getUserRole(String login){
        if (userDAO.isAdmin(login))
            return UserRole.ADMIN;
        return userDAO.getRole(login);
    }

    public int getUserAccessLevel(String login){
        return getUserRole(login).getAccessLevel();
    }

    public String getAttribute(HttpSession session, String attribute){
        Object value = session.getAttribute(attribute);
        return (value == null) ? "" : value.toString();
    }

    public void setAttribute(HttpSession session, String attribute, String value){
        session.setAttribute(attribute, value);
    }

    public Boolean isAccessDenied(HttpSession session){
        return (getUserLogin(session) == null);
    }
}
