package ru.staffbot.database.users;

import ru.staffbot.database.Database;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.webserver.WebServer;

import java.sql.*;
import java.util.ArrayList;

public class Users {

    public static final String DB_TABLE_NAME = "sys_users";

    public boolean isAdmin(String login){
        return WebServer.ADMIN.equalsIgnoreCase(login);
    }

    public void delete(String login){
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "DELETE FROM " + DB_TABLE_NAME + " WHERE (login = ?)");
            statement.setString(1, login);
            statement.executeUpdate();
            Journal.add("Пользоатель " + login + " удалён");
        } catch (SQLException e) {
            Journal.add(e.getMessage(), NoteType.ERROR);
        }
    }

    public boolean setUser(User user){
        try {
            if (isAdmin(user.login)) {
                Journal.add("Добавить пользоателя " + user.login + " нельзя, т.к. БОГ един и имя его занято!");
                return false;
            }
            boolean newLogin = (getUserList(user.login).size() == 0);
            PreparedStatement statement = Database.getConnection().prepareStatement(
                newLogin ?
                    "INSERT INTO " + DB_TABLE_NAME + " (password, role, login) VALUES (?, ?, ?)" :
                    "UPDATE " + DB_TABLE_NAME + " SET password = ? , role = ? WHERE login = ?"
            );
            statement.setString(3, user.login);
            statement.setString(1, user.password);
            statement.setInt(2, user.role.getAccessLevel());
            statement.executeUpdate();
            Journal.add(newLogin ?
                    "Добавлен пользоатель " + user.login + " с ролью " + user.role.getDescription() :
                    "Пользоатель" + user.login + " изменён");
            return true;
        } catch (SQLException e) {
            Journal.add(e.getMessage(), NoteType.ERROR);
            return false;
        }
    }

    public User getUser(String login){
        if (login == null) return null;
        ArrayList<User> userList = getUserList(login);
        return (userList.size() > 0) ? userList.get(0) : null;
    }

    public int verify(String login, String password){
        User user = getUser(login);
        return (user == null) ? -1 : user.password.equals(password) ? user.role.getAccessLevel() : -1;
    }

    public UserRole getRole(String login){
        User user = getUser(login);
        return user.role;
    }

    public ArrayList<User> getUserList() {
        return getUserList(null);
    }

    private ArrayList<User> getUserList(String login){
        ArrayList<User> userList = new ArrayList<>();
        if (!Database.connected()) return userList;
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "SELECT login, password, role FROM "  + DB_TABLE_NAME
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
            Journal.add(e.getMessage(), NoteType.ERROR);
        }
        return userList;
    }

}
