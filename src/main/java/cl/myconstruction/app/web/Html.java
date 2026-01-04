package cl.myconstruction.app.web;

public final class Html {
    public static String page(String title, String body) {
        return """
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>%s</title>
                  <style>
                    body{font-family:system-ui,-apple-system,Segoe UI,Roboto,Arial,sans-serif;margin:40px;max-width:900px}
                    .card{border:1px solid #ddd;border-radius:12px;padding:20px}
                    input{padding:10px;border:1px solid #ccc;border-radius:8px;width:100%%;max-width:420px}
                    button{padding:10px 16px;border:0;border-radius:10px;background:#111;color:#fff;cursor:pointer}
                    a{color:#0b57d0}
                    .error{color:#b00020}
                    .row{margin:12px 0}
                  </style>
                </head>
                <body>
                  %s
                </body>
                </html>
                """.formatted(escape(title), body);
    }

    public static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private Html() {}
}

