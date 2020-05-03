package ru.staffbots.database.users;

import ru.staffbots.tools.languages.Language;
import ru.staffbots.tools.languages.Languages;

public class User {

    public String login;
    public String password;
    public UserRole role;
    public Language language;

    private void init(String login, String password, String languageCode, UserRole role){
        this.login = login;
        this.password = password;
        this.language = Languages.get(languageCode);
        this.role = role;
    }

    public User(String login, String password, String languageCode, UserRole role) {
        init(login, password, languageCode, role);
    }

    public User(String login, String password, String languageCode, int accessLevel) {
        init(login, password, languageCode, UserRole.valueOf(accessLevel));
    }

    public User(String login, String password, String languageCode, String roleName) {
        init(login, password, languageCode, UserRole.valueByName(roleName));
    }

    public User(String login, String password, String languageCode) {
        init(login, password, languageCode, UserRole.INSPECTOR);
    }

    @Override
    public String toString() {
        return "login: " + login +
               "\nrole: " + role.getDescription(Languages.getDefaultCode());
    }
}
