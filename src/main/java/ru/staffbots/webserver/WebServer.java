package ru.staffbots.webserver;


import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.servlets.*;
import ru.staffbots.windows.MainWindow;

import java.net.*;

public class WebServer {

    public static Integer HTTP_PORT = 80;

    public static Integer HTTPS_PORT = 443;

    public static String ADMIN = "admin";

    public static String PASSWORD = "admin";

    public static Boolean HTTP_USED = false;

    public static String key_store = "keystore";

    public static String key_store_password = "staffbots";

    public static String key_manager_password = "staffbots";

    /**
     *
     */
    private static Server server = null;

    private static ServerConnector getHttpConnector(){
        // HTTP connector
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(HTTP_PORT);
        return connector;
    }

    private static ServerConnector getHttpsConnector(){
        String keyStorePath = Resources.ExtractFromJar(key_store);

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
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        connector.setPort(HTTPS_PORT);

        // HTTPS connectors
//        server.setConnectors(new Connector[]{connector});
//        server.setConnectors(new Connector[]{sslConnector});
        return connector;
    }

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
        server.setConnectors(new Connector[]{getHttpsConnector()});
        if (HTTP_USED)
            server.addConnector(getHttpConnector());

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
        return getURL(false);
    }

    public static URL getURL(boolean SSL){
        try {
            return SSL ?
                new URL("https://localhost:" + HTTPS_PORT) :
                new URL("http://localhost:" + HTTP_PORT);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

}
