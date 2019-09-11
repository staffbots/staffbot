package ru.staffbots.webserver;


import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.Resources;
import ru.staffbots.webserver.servlets.*;
import ru.staffbots.windows.MainWindow;

import java.net.*;

public class WebServer {

    public static Integer HTTP_PORT = 80;

    public static Integer HTTPS_PORT = 8080;

    public static String ADMIN = "admin";

    public static String PASSWORD = "admin";

    public static String key_store = "keystore";

    public static String key_store_password = "staffbots";

    public static String key_manager_password = "staffbots";

    /**
     *
     */
    private static Server server = null;

    /**
     * <b>Запуск</b> веб-сервера<br>
     * @return удачность
     */
    public static void init() {
        // Если сервер уже запущен, то останавливаем его
        if (server != null) {
            try {
                server.stop();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Если порт занят, то сервер запустить не получится
        if(hostIsBusy()){
            Journal.add("Неудачная попытка запустить веб-сервер. Порт уже занят", NoteType.ERROR);
            System.exit(0);
        }

        server = new Server();

        // HTTP connector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(HTTP_PORT);

        String keyStorePath = Resources.ExtractFromJar("/" + key_store);

        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());            // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();
        //SslContextFactory sslContextFactory = new JettySslContextFactory(configuration.getSslProviders());

        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(key_store_password);
        sslContextFactory.setKeyManagerPassword(key_manager_password);

        // Configuring the connector
        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        sslConnector.setPort(HTTPS_PORT);

        // HTTPS connectors
//        server.setConnectors(new Connector[]{connector});
//        server.setConnectors(new Connector[]{sslConnector});
        server.setConnectors(new Connector[]{connector, sslConnector});

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

        server.setHandler(handlers);
        try {
            server.start();
            Journal.add("Веб-сервер запущен");
            if (!MainWindow.USED)
                server.join();
        } catch (Exception e){
            Journal.add("Ошибка запуска веб-сервера", NoteType.ERROR);
            System.exit(0);
        }
    }

    public static boolean hostIsBusy(){
        try {
            HttpURLConnection urlConnect = (HttpURLConnection) getURL().openConnection();
            urlConnect.getContent();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    public static URL getURL(){
        try {
            return new URL("http://localhost:" + HTTP_PORT);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

}
