import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class RemoteExecHttpServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/execute", new ExecuteHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("Remote execution demo started at http://localhost:" + PORT);
        System.out.println("Open browser and submit Java source code.");
    }

    private static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain; charset=UTF-8");
                return;
            }
            send(exchange, 200, pageHtml(), "text/html; charset=UTF-8");
        }

        private String pageHtml() {
            String demoSource = "public class DemoTask {\\n"
                    + "    public static String execute() {\\n"
                    + "        return \\\"Hello from remote bytecode! time=\\\" + java.time.LocalTime.now();\\n"
                    + "    }\\n"
                    + "}\\n";

            return "<!doctype html>"
                    + "<html><head><meta charset='utf-8'><title>Remote Bytecode Execute Demo</title>"
                    + "<style>body{font-family:Menlo,Monaco,monospace;max-width:980px;margin:24px auto;padding:0 12px;}"
                    + "textarea,input{width:100%;box-sizing:border-box;margin-top:8px;font-family:inherit;}"
                    + "textarea{height:320px;}button{margin-top:10px;padding:8px 18px;}pre{background:#f5f7fa;padding:12px;overflow:auto;}"
                    + "</style></head><body>"
                    + "<h2>Chapter 9 Remote Execute Demo (B/S)</h2>"
                    + "<p>Contract: provide className and sourceCode. The class should expose <code>public static String execute()</code> or <code>public static void main(String[])</code>.</p>"
                    + "<form method='post' action='/execute'>"
                    + "<label>className</label>"
                    + "<input name='className' value='DemoTask' />"
                    + "<label>sourceCode</label>"
                    + "<textarea name='sourceCode'>" + escapeHtml(demoSource) + "</textarea>"
                    + "<button type='submit'>Compile & Execute</button>"
                    + "</form></body></html>";
        }
    }

    private static class ExecuteHandler implements HttpHandler {
        private final RemoteCodeExecutor executor = new RemoteCodeExecutor();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain; charset=UTF-8");
                return;
            }

            String body = readAll(exchange.getRequestBody());
            Map<String, String> form = parseForm(body);

            String className = form.getOrDefault("className", "").trim();
            String sourceCode = form.getOrDefault("sourceCode", "");

            ExecutionResult result = executor.execute(className, sourceCode);
            send(exchange, result.isSuccess() ? 200 : 400, result.toPlainText(), "text/plain; charset=UTF-8");
        }
    }

    private static void send(HttpExchange exchange, int statusCode, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String readAll(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseForm(String body) {
        Map<String, String> map = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) {
            return map;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx < 0) {
                continue;
            }
            String key = urlDecode(pair.substring(0, idx));
            String value = urlDecode(pair.substring(idx + 1));
            map.put(key, value);
        }
        return map;
    }

    private static String urlDecode(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }

    private static String escapeHtml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
