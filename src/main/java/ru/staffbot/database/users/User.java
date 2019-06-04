package ru.staffbot.database.users;

import ru.staffbot.database.journal.Journal;

public class User {

    public String login;
    public String password;
    public UserRole role;

    public User(String login, String password, UserRole role) {
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public User(String login, String password, int accessLevel) {
        this.login = login;
        this.password = password;
        this.role = UserRole.valueOf(accessLevel);
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.role = UserRole.GUEST;
    }

    @Override
    public String toString() {
        return "login: " + login + "\npassword: " + "\nrole: " + role.getDescription();
    }
}
