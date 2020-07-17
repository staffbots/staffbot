package ru.staffbots.database.tables.users;

import ru.staffbots.database.tables.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.Executor;
import ru.staffbots.database.tables.journal.Journal;
import ru.staffbots.database.tables.journal.NoteType;
import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.languages.Languages;
import ru.staffbots.webserver.WebServer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Users extends DBTable {

    private Users(){
        super("sys_users",
                "login VARCHAR(16) NOT NULL UNIQUE, " +
                "password VARCHAR(32), " +
                "role INT, " +
                "language VARCHAR(2)");
    }

    private static Users instance = null;

    public static Users getInstance() {
        if (instance == null)
            if (Database.connected())
                synchronized (Users.class) {
                    if (instance == null)
                        instance = new Users();
                }
        return instance;
    }

    public static void delete(String login){
        if (instance == null) return;
        Executor executor = new Executor("delete_user", login);
        executor.execUpdate("DELETE FROM " + instance.getTableName() + " WHERE (login = ?)", login);
    }

    public static boolean setUser(User user){
        boolean newLogin = (getUserList(user.login).size() == 0);
        if (isAdmin(user.login)) {
            Journal.add(NoteType.WARNING, "add_user", user.login);
            return false;
        }
        if (instance == null) return false;
        Executor executor = new Executor(newLogin ? "add_user" :"change_user", user.login, user.role.getDescription(user.language.getCode()));
        return executor.execUpdate(newLogin ?
                        "INSERT INTO " + instance.getTableName() + " (role, language, password, login) VALUES (?, ?, ?, ?)" :
                        "UPDATE " + instance.getTableName() + " SET role = ?, language = ?, password = ? WHERE login = ?",
                String.valueOf(user.role.getAccessLevel()),
                user.language.getCode(),
                cryptWithMD5(user.password),
                user.login) > 0;
    }

    public static UserRole getRole(String login){
        if (isAdmin(login))
            return UserRole.ADMIN;
        User user = getUser(login);
        return (user == null) ? UserRole.INSPECTOR : user.role;
    }

    public static Language getLanguage(String login){
        if (isAdmin(login))
            return Languages.get();
        User user = getUser(login);
        if (user != null)
            return user.language;
        return Languages.get();
    }

    public static boolean setLanguage(String login, Language language){
        if (isAdmin(login))
            return Languages.setDefaultCode(language.getCode());
        User user = getUser(login);
        if (user == null)
            return false;
        if (user.language.getCode().equals(language.getCode()))
            return false;
        user.language = language;
        if (instance == null) return false;
        Executor executor = new Executor();
        return executor.execUpdate(
            "UPDATE " + instance.getTableName() + " SET language = ? WHERE login = ?",
                user.language.getCode(),
                user.login) > 0;
    }

    public static User getUser(String login){
        if (login == null) return null;
        ArrayList<User> userList = getUserList(login);
        return (userList.size() > 0) ? userList.get(0) : null;
    }

    public static ArrayList<User> getUserList() {
        return getUserList(null);
    }

    private static ArrayList<User> getUserList(String login){
        ArrayList<String> parameters = new ArrayList(0);
        if (login != null) {
            if (login.trim().equals(""))
                return new ArrayList(0);
            parameters.add(login);
        }
        if (instance == null) return new ArrayList(0);
        Executor<ArrayList<User>> executor = new Executor();
        return executor.execQuery(
                "SELECT login, password, language, role FROM "  + instance.getTableName() + (login == null ? "" : " WHERE (login = ?)"),
                (resultSet) -> {
                    ArrayList<User> userList = new ArrayList(0);
                    while (resultSet.next())
                        userList.add(new User(
                                resultSet.getString(1),
                                resultSet.getString(2),
                                resultSet.getString(3),
                                resultSet.getInt(4)));
                    return userList;
                },
                parameters.stream().toArray(String[]::new));
    }

    private static String cryptWithMD5(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passBytes = password.getBytes();
            md.reset();
            byte[] digested = md.digest(passBytes);
            StringBuffer sb = new StringBuffer();
            for(int i=0;i<digested.length;i++){
                sb.append(Integer.toHexString(0xff & digested[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException exception) {
            Journal.add(NoteType.ERROR, "CryptPassword", exception.getMessage());
        }
        return password;
    }

    public static boolean isAdmin(String login){
        return WebServer.getInstance().getAdminLogin().equalsIgnoreCase(login);
    }

    public static int verify(String login, String password){
        if (isAdmin(login))
            return WebServer.getInstance().getAdminPassword().equals(password) ? UserRole.ADMIN.getAccessLevel() : -1;
        User user = getUser(login);
        return (user == null) ? -1 : user.password.equals(cryptWithMD5(password)) ? user.role.getAccessLevel() : -1;
    }

}
