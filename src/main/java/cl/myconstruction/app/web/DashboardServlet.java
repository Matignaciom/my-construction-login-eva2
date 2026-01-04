package cl.myconstruction.app.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String email = String.valueOf(req.getSession(true).getAttribute(SessionKeys.USER_EMAIL));
        String body = """
                <div class="card">
                  <h1>Panel</h1>
                  <p>Bienvenido: <strong>%s</strong></p>
                  <p><a href="/logout">Cerrar sesión</a></p>
                </div>
                """.formatted(Html.escape(email));

        resp.getWriter().write(Html.page("Panel - My Construction", body));
    }
}

