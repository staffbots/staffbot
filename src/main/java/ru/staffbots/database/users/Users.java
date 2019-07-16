package ru.staffbots.database.users;

import ru.staffbots.database.DBTable;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.webserver.WebServer;

import java.sql.*;
import java.util.ArrayList;

public class Users extends DBTable {

    public static final String DB_TABLE_NAME = "sys_users";
    public static final String DB_TABLE_FIELDS = "login VARCHAR(16), password VARCHAR(16), role INT";

    public Users(){
        super(DB_TABLE_NAME, DB_TABLE_FIELDS);
    }

    public boolean isAdmin(String login){
        return WebServer.ADMIN.equalsIgnoreCase(login);
    }

    public void delete(String login){
        try {
            PreparedStatement statement = Database.getConnection().prepareStatement(
                    "DELETE FROM " + getTableName() + " WHERE (login = ?)");
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
                    "INSERT INTO " + getTableName() + " (password, role, login) VALUES (?, ?, ?)" :
                    "UPDATE " + getTableName() + " SET password = ? , role = ? WHERE login = ?"
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

    public int verify(String login, String password){
        User user = getUser(login);
        return (user == null) ? -1 : user.password.equals(password) ? user.role.getAccessLevel() : -1;
    }

    public UserRole getRole(String login){
        if (isAdmin(login))
            return UserRole.ADMIN;
        User user = getUser(login);
        return (user == null) ? null : user.role;
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
        if (!Database.connected()) return userList;
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
            Journal.add(e.getMessage(), NoteType.ERROR);
        }
        return userList;
    }

}
