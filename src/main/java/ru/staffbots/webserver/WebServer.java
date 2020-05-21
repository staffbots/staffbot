package ru.staffbots.webserver;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import ru.staffbots.database.Database;
import ru.staffbots.database.journal.Journal;
import ru.staffbots.database.journal.NoteType;
import ru.staffbots.tools.colors.ColorSchema;
import ru.staffbots.tools.resources.Resources;
import ru.staffbots.webserver.servlets.*;
import ru.staffbots.windows.MainWindow;
import java.io.IOException;
import java.net.*;

public class WebServer {

    private WebServer() {}

    private static final WebServer instance = new WebServer();

    public static WebServer getInstance() {
        return instance;
    }

    ////////////////////////////////////////////////////////////////
    private String adminLogin = "admin";

    public String getAdminLogin() {
        return adminLogin;
    }

    public void setAdminLogin(String value) {
        if (value == null) return;
        if (value.trim().isEmpty()) return;
        adminLogin = value;
    }

    ////////////////////////////////////////////////////////////////
    private String adminPassword = "";

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String value) {
        if (value == null) return;
        adminPassword = value;
    }

    ////////////////////////////////////////////////////////////////
    private int httpPort = 80;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(Integer value) {
        if (value == null) return;
        httpPort = value;
    }

    ////////////////////////////////////////////////////////////////
    private int httpsPort = 443;

    public void setHttpsPort(Integer value) {
        if (value == null) return;
        httpsPort = value;
    }

    ////////////////////////////////////////////////////////////////
    private boolean httpUsed = false;

    public boolean getHttpUsed() {
        return httpUsed;
    }

    public void setHttpUsed(Boolean value) {
        if (value == null) return;
        httpUsed = value;
    }

    ////////////////////////////////////////////////////////////////
    private String keyStore = "keystore";

    public void setKeyStore(String value) {
        if (value == null) return;
        keyStore = value;
    }

    ////////////////////////////////////////////////////////////////
    private String storePassword = "staffbots";

    public void setStorePassword(String value) {
        if (value == null) return;
        storePassword = value;
    }

    ////////////////////////////////////////////////////////////////
    private String managerPassword = "staffbots";

    public void setManagerPassword(String value) {
        if (value == null) return;
        managerPassword = value;
    }

    ////////////////////////////////////////////////////////////////
    /**
     * Задержка между запросами на обновление данных на страницах, милисек.
     */
    private int updateDelay = 10000;

    public int getUpdateDelay() {
        return updateDelay;
    }

    public void setUpdateDelay(Integer value) {
        if (value == null) return;
        updateDelay = value;
    }

    ////////////////////////////////////////////////////////////////
    private ColorSchema сolorSchema;

    public void setColorSchema(String mainColor) {
        сolorSchema = new ColorSchema(mainColor);
    }

    public ColorSchema getColorSchema() {
        return сolorSchema;
    }

    ////////////////////////////////////////////////////////////////
    private String fontFamily = "sans-serif";

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String value) {
        if (value == null) return;
        fontFamily = value;
    }

    ////////////////////////////////////////////////////////////////
    /**
     *
     */
    private Server server = null;

    private void checkPort(int port) {
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

    private ServerConnector getHttpConnector() {
        checkPort(httpPort);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(httpPort);
        return connector;
    }

    private ServerConnector getHttpsConnector() {
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

    private Connector[] getConnectors() {
        return httpUsed ?
               new Connector[] {getHttpsConnector(), getHttpConnector()} :
               new Connector[] {getHttpsConnector()};
    }

    /**
     *
     */
    public void init() {
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
            if (!MainWindow.getFrameUsed())
                server.join();
        } catch (Exception e){
            Journal.add(NoteType.ERROR, "start_server", e.getMessage());
            System.exit(0);
        }
    }

    public URL getLocalURL(boolean SSL){
        try {
            return SSL ?
                new URL("https://localhost:" + httpsPort) :
                new URL("http://localhost:" + httpPort);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

}
