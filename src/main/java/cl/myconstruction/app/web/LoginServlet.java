package cl.myconstruction.app.web;

import cl.myconstruction.app.db.Database;
import cl.myconstruction.app.db.User;
import cl.myconstruction.app.security.PasswordHasher;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public final class LoginServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        writeForm(resp, Optional.empty());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");

        String email = Optional.ofNullable(req.getParameter("email")).orElse("").trim();
        String password = Optional.ofNullable(req.getParameter("password")).orElse("");

        if (email.isBlank() || password.isBlank()) {
            writeForm(resp, Optional.of("Debe ingresar email y contraseña."));
            return;
        }

        Database db = (Database) req.getServletContext().getAttribute(Database.CTX_KEY);
        try {
            Optional<User> userOpt = db.findUserByEmail(email);
            if (userOpt.isEmpty()) {
                writeForm(resp, Optional.of("Credenciales inválidas."));
                return;
            }
            User user = userOpt.get();
            boolean ok = PasswordHasher.verify(password, user.passwordHash(), user.passwordSalt());
            if (!ok) {
                writeForm(resp, Optional.of("Credenciales inválidas."));
                return;
            }

            req.getSession(true).setAttribute(SessionKeys.USER_ID, user.id());
            req.getSession(true).setAttribute(SessionKeys.USER_EMAIL, user.email());
            resp.sendRedirect("/app/dashboard");
        } catch (SQLException e) {
            resp.setStatus(500);
            writeForm(resp, Optional.of("Error interno al validar credenciales."));
        }
    }

    private void writeForm(HttpServletResponse resp, Optional<String> error) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String errorHtml = error.map(msg -> "<p class=\"error\">" + Html.escape(msg) + "</p>").orElse("");
        String body = """
                <div class="card">
                  <h1>Iniciar sesión</h1>
                  %s
                  <form method="post" action="/login">
                    <div class="row">
                      <label>Email</label><br>
                      <input type="email" name="email" required>
                    </div>
                    <div class="row">
                      <label>Contraseña</label><br>
                      <input type="password" name="password" required>
                    </div>
                    <button type="submit">Ingresar</button>
                  </form>
                  <p class="row"><a href="/">Volver al inicio</a></p>
                </div>
                """.formatted(errorHtml);
        resp.getWriter().write(Html.page("Login - My Construction", body));
    }
}

