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

import java.io.IOException;
import java.net.*;

public class WebServer {

    public static Integer httpPort = 80;

    public static Integer httpsPort = 443;

    public static String adminLogin = "admin";

    public static String adminPassword = "";

    public static Boolean httpUsed = false;

    public static String keyStore = "keystore";

    public static String storePassword = "staffbots";

    public static String managerPassword = "staffbots";

    /**
     * Задержка между запросами на обновление данных на страницах, милисек.
     */
    public static Integer updateDelay = 10000;

    /**
     *
     */
    private static Server server = null;

    private static void checkPort(int port) {
        try {
            Socket socket = new Socket("localhost", port);
            if (socket.isConnected()) {
                Journal.add(NoteType.ERROR, "busy_port", String.valueOf(port));
                System.exit(0);
            }
        } catch (IOException e) {
            // No actions
        }
    }

    private static ServerConnector getHttpConnector() {
        checkPort(httpPort);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(httpPort);
        return connector;
    }

    private static ServerConnector getHttpsConnector() {
        checkPort(httpsPort);
        String keyStorePath = Resources.getAsFile(keyStore).getPath();
        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());            // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();
        //SslContextFactory sslContextFactory = new JettySslContextFactory(configuration.getSslProviders());
        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(storePassword);
        sslContextFactory.setKeyManagerPassword(managerPassword);
        // Configuring the connector
        ServerConnector connector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, "http/1.1"),
                new HttpConnectionFactory(https));
        connector.setPort(httpsPort);
        return connector;
    }

    private static Connector[] getConnectors() {
        return httpUsed ?
               new Connector[] {getHttpsConnector(), getHttpConnector()} :
               new Connector[] {getHttpsConnector()};
    }

    /**
     *
     */
    public static void init() {
        if (server != null) { // If server already run
            try {
                server.stop();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        server = new Server();
        server.setConnectors(getConnectors());
        AccountService accountService = new AccountService();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ServletHolder entryServletHolder = new ServletHolder(new EntryServlet(accountService));
        context.addServlet(entryServletHolder,"");
        context.addServlet(entryServletHolder,"/entry");
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
            Journal.add("start_server");
            if (!MainWindow.frameUsed)
                server.join();
        } catch (Exception e){
            Journal.add(NoteType.ERROR, "start_server", e.getMessage());
            System.exit(0);
        }
    }

    public static URL getLocalURL(boolean SSL){
        try {
            return SSL ?
                new URL("https://localhost:" + httpsPort) :
                new URL("http://localhost:" + httpPort);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

}
