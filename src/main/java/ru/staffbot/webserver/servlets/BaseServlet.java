package ru.staffbot.webserver.servlets;

import ru.staffbot.webserver.AccountService;
import javax.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

    protected AccountService accountService;

    public BaseServlet(AccountService accountService){
        this.accountService = accountService;
    }

}
