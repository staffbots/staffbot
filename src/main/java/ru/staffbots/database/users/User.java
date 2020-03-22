package ru.staffbots.database.users;


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

    public User(String login, String password, String roleName) {
        this.login = login;
        this.password = password;
        this.role = UserRole.valueByName(roleName);
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
        this.role = UserRole.INSPECTOR;
    }

    @Override
    public String toString() {
        return "login: " + login +
               "\nrole: " + role.getDescription();
    }
}
