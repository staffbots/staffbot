package ru.staffbots.webserver;

import ru.staffbots.database.users.Users;
import ru.staffbots.database.users.UserRole;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class AccountService {

    /**
     * sessionId -> login
     */
    private Map<String, String> sessions = new HashMap<>();

    public Users users = new Users();

    public int verifyUser(String login, String password){
        return users.verify(login, password);
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
        UserRole role = users.getRole(login);
        return (role == null) ? -1 : role.getAccessLevel();
    }

    public int getUserAccessLevel(HttpServletRequest request){
        if (request == null ) return -1;
        String login = getUserLogin(request.getSession());
        if (login == null ) return -1;
        return getUserAccessLevel(login);
    }

    public String getAttribute(HttpSession session, String attribute){
        Object value = session.getAttribute(attribute);
        return (value == null) ? "" : value.toString();
    }

    public void setAttribute(HttpSession session, String attribute, String value){
        session.setAttribute(attribute, value);
    }

}
