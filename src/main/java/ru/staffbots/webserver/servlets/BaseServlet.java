package ru.staffbots.webserver.servlets;

import ru.staffbots.webserver.AccountService;
import javax.servlet.http.HttpServlet;

public abstract class BaseServlet extends HttpServlet {

    protected AccountService accountService;

    public BaseServlet(AccountService accountService){
        this.accountService = accountService;
    }

}
