package ru.staffbots.database.users;


import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.languages.Languages;

public class User {

    public String login;
    public String password;
    public UserRole role;
    public Language language;

    public User(String login, String password, String languageCode, UserRole role) {
        this.login = login;
        this.password = password;
        this.language = Languages.get(languageCode);
        this.role = role;
    }

    public User(String login, String password, String languageCode, int accessLevel) {
        this.login = login;
        this.password = password;
        this.language = Languages.get(languageCode);
        this.role = UserRole.valueOf(accessLevel);
    }

    public User(String login, String password, String languageCode, String roleName) {
        this.login = login;
        this.password = password;
        this.language = Languages.get(languageCode);
        this.role = UserRole.valueByName(roleName);
    }

    public User(String login, String password, String languageCode) {
        this.login = login;
        this.password = password;
        this.language = Languages.get(languageCode);
        this.role = UserRole.INSPECTOR;
    }

    @Override
    public String toString() {
        return "login: " + login +
               "\nrole: " + role.getDescription(Languages.getDefaultCode());
    }
}
