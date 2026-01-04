package cl.myconstruction.app;

import cl.myconstruction.app.config.AppConfig;
import cl.myconstruction.app.db.Database;
import cl.myconstruction.app.web.AuthFilter;
import cl.myconstruction.app.web.DashboardServlet;
import cl.myconstruction.app.web.HomeServlet;
import cl.myconstruction.app.web.LoginServlet;
import cl.myconstruction.app.web.LogoutServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;

public final class Main {
    public static void main(String[] args) throws Exception {
        AppConfig config = AppConfig.fromSystem();
        Database database = Database.create(config.databaseConfig());
        database.ensureSchema();
        database.ensureDemoUser();

        Server server = new Server(config.serverPort());

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setAttribute(Database.CTX_KEY, database);

        context.addServlet(new ServletHolder(new HomeServlet()), "/");
        context.addServlet(new ServletHolder(new LoginServlet()), "/login");
        context.addServlet(new ServletHolder(new LogoutServlet()), "/logout");
        context.addServlet(new ServletHolder(new DashboardServlet()), "/app/dashboard");

        context.addFilter(new FilterHolder(new AuthFilter()), "/app/*", EnumSet.of(DispatcherType.REQUEST));

        server.setHandler(context);

        server.start();
        server.join();
    }
}

