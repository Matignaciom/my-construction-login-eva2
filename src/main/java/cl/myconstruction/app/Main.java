package cl.myconstruction.app;

import cl.myconstruction.app.config.AppConfig;
import cl.myconstruction.app.db.Database;
import cl.myconstruction.app.web.AuthFilter;
import cl.myconstruction.app.web.DashboardServlet;
import cl.myconstruction.app.web.HomeServlet;
import cl.myconstruction.app.web.LoginServlet;
import cl.myconstruction.app.web.LogoutServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.BindException;
import java.util.EnumSet;

import jakarta.servlet.DispatcherType;

public final class Main {
    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.fromSystem();
        Database database = Database.create(config.databaseConfig());
        database.ensureSchema();
        database.ensureDemoUser();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setAttribute(Database.CTX_KEY, database);

        context.addServlet(new ServletHolder(new HomeServlet()), "/");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new LogoutServlet()), "/logout");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/app/dashboard");

        context.addFilter(new FilterHolder(new AuthFilter()), "/app/*", EnumSet.of(DispatcherType.REQUEST));

        Server server = startServerWithRetry(config.serverPort(), context);
        server.join();
    }

    private static Server startServerWithRetry(int requestedPort, ServletContextHandler context) throws Exception {
        int attempts = parseInt(System.getenv("APP_PORT_TRIES"), 20);
        if (requestedPort == 0) {
            attempts = 0;
        }

        Exception last = null;
        for (int i = 0; i <= attempts; i++) {
            int port = requestedPort == 0 ? 0 : requestedPort + i;
            Server server = new Server(port);
            server.setHandler(context);
            try {
                server.start();
                int actualPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
                System.out.println("Servidor iniciado en puerto " + actualPort);
                return server;
            } catch (Exception e) {
                if (isBindException(e) && requestedPort != 0 && i < attempts) {
                    System.out.println("Puerto " + port + " ocupado. Intentando con " + (port + 1) + "...");
                    last = e;
                    continue;
                }
                throw e;
            }
        }
        throw last != null ? last : new IllegalStateException("No se pudo iniciar el servidor.");
    }

    private static boolean isBindException(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof BindException) return true;
            cur = cur.getCause();
        }
        return false;
    }

    private static int parseInt(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

