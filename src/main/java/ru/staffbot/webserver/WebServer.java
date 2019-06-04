package ru.staffbot.webserver;


import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.staffbot.database.journal.Journal;
import ru.staffbot.database.journal.NoteType;
import ru.staffbot.webserver.servlets.*;

import java.net.*;

public class WebServer {

    public static Integer PORT = 8055;

    public static String ADMIN = "admin";

    public static String PASSWORD = "admin";

    /**
     *
     */
    private static Server server;

    /**
     * <b>Запуск</b> веб-сервера<br>
     * @return удачность
     */
    public static void init() {

        if(hostIsBusy()){
            Journal.add("Неудачная попытка запустить веб-сервер. Порт уже занят", NoteType.ERROR);
            System.exit(0);
        }


        AccountService accountService = new AccountService();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(new EntryServlet(accountService)),"");
        context.addServlet(new ServletHolder(new EntryServlet(accountService)),"/entry");
        context.addServlet(new ServletHolder(new ControlServlet(accountService)),"/control");
        context.addServlet(new ServletHolder(new StatusServlet(accountService)),"/status");
        context.addServlet(new ServletHolder(new JournalServlet(accountService)),"/journal");
        context.addServlet(new ServletHolder(new UsersServlet(accountService)),"/users");
        context.addServlet(new ServletHolder(new SystemServlet(accountService)),"/system");
        context.addServlet(new ServletHolder(new AboutServlet(accountService)),"/about");
        context.addServlet(new ServletHolder(new ResourceServlet(accountService)),"/resource");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);

        server = new Server(PORT);
        server.setHandler(handlers);
        try {
            server.start();
            Journal.add("Веб-сервер запущен");
        } catch (Exception e){
            Journal.add("Ошибка запуска веб-сервера", NoteType.ERROR);
            System.exit(0);
        }
    }

    public static boolean hostIsBusy(){
        try {
            URL url = new URL("http://localhost:" + PORT);
            HttpURLConnection urlConnect = (HttpURLConnection)url.openConnection();
            urlConnect.getContent();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

}
