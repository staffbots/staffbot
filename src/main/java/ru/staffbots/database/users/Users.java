package ru.staffbots.database.users;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Translator;
import ru.staffbots.webserver.WebServer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;

public class Users extends DBTable {

    private static final String staticTableName = "sys_users";
    private static final String staticTableFields =
            "login VARCHAR(16), password VARCHAR(32), role INT";

    public Users(){
        super(staticTableName, Translator.getValue("database", "users_table"), staticTableFields);
    }

    public boolean isAdmin(String login){
        return WebServer.defaultAdmin.equalsIgnoreCase(login);
    }

    public void delete(String login){
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "DELETE FROM " + getTableName() + " WHERE (login = ?)");
            statement.setString(1, login);
            statement.executeUpdate();
            Journal.add("delete_user", login);
        } catch (SQLException e) {
            Journal.add(NoteType.ERROR, "delete_user", e.getMessage());
        }
    }

    public boolean setUser(User user){
        boolean newLogin = (getUserList(user.login).size() == 0);
        try {
            if (isAdmin(user.login)) {
                Journal.add(NoteType.WARNING, "add_user", user.login.toLowerCase());
                return false;
            }
            //newLogin = (getUserList(user.login).size() == 0);
            PreparedStatement statement = Database.getConnection().prepareStatement(
                newLogin ?
                    "INSERT INTO " + getTableName() + " (role, password, login) VALUES (?, ?, ?)" :
                    "UPDATE " + getTableName() + " SET role = ?, password = ? WHERE login = ?"
            );
            statement.setString(3, user.login);
            statement.setString(2, cryptWithMD5(user.password));
            statement.setInt(1, user.role.getAccessLevel());
            statement.executeUpdate();
            Journal.add(newLogin ? "add_user" : "change_user",
                    user.login, user.role.getDescription());
            return true;
        } catch (SQLException e) {
            Journal.add(NoteType.ERROR, newLogin ? "add_user" :"change_user", e.getMessage());
            return false;
        }
    }

    public int verify(String login, String password){
        if (isAdmin(login))
            if (WebServer.adminPassword.equals(password))
                return UserRole.ADMIN.getAccessLevel();
        User user = getUser(login);
        return (user == null) ? -1 : user.password.equals(cryptWithMD5(password)) ? user.role.getAccessLevel() : -1;
    }

    public UserRole getRole(String login){
        if (isAdmin(login))
            return UserRole.ADMIN;
        User user = getUser(login);
        return (user == null) ? UserRole.INSPECTOR : user.role;
    }

    public User getUser(String login){
        if (login == null) return null;
        ArrayList<User> userList = getUserList(login);
        return (userList.size() > 0) ? userList.get(0) : null;
    }

    public ArrayList<User> getUserList() {
        return getUserList(null);
    }

    private ArrayList<User> getUserList(String login){
        ArrayList<User> userList = new ArrayList<>();
        if (Database.disconnected()) return userList;
        if (login != null)
            if (login.trim().equals(""))
                return userList;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT login, password, role FROM "  + getTableName()
                            + ((login == null) ? "" : " WHERE (login = ?)"));
            if (login != null) statement.setString(1, login);
            if(statement.execute()) {
                ResultSet resultSet = statement.getResultSet();
                while (resultSet.next())
                    userList.add(new User(
                            resultSet.getString(1),
                            resultSet.getString(2),
                            resultSet.getInt(3)));
            }
        } catch (SQLException e) {
            Journal.add(NoteType.ERROR, "get_user", login, e.getMessage());
        }
        return userList;
    }

    private String cryptWithMD5(String password){
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

}
