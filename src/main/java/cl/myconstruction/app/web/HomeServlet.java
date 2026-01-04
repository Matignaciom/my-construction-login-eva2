package cl.myconstruction.app.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public final class HomeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        Object userId = req.getSession(true).getAttribute(SessionKeys.USER_ID);
        String body;
        if (userId != null) {
            body = """
                    <div class="card">
                      <h1>My Construction</h1>
                      <p>Sesión iniciada.</p>
                      <p><a href="/app/dashboard">Ir al panel</a> · <a href="/logout">Cerrar sesión</a></p>
                    </div>
                    """;
        } else {
            body = """
                    <div class="card">
                      <h1>My Construction</h1>
                      <p>Página de inicio (demo).</p>
                      <p><a href="/login">Iniciar sesión</a></p>
                    </div>
                    """;
        }

        resp.getWriter().write(Html.page("Inicio - My Construction", body));
    }
}

