import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;

public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(Server::handleRequest);
        server.start();
    }

    public static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        // printRequestInfo(exchange);

        String name = new File(requestURI.getPath()).getName();
        File path = new File("./", name);

        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        String testQ = params.get("test");

        // RUN OS cmd, get output
        Process process = Runtime.getRuntime().exec("ls");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        String tmp;
        while ((tmp = reader.readLine()) != null) {
            line+=tmp+"\n";
        }
        System.out.println(line);

        OutputStream os = exchange.getResponseBody();

        if (path.exists() && false) {
            exchange.sendResponseHeaders(200, path.length());
            os.write(Files.readAllBytes(path.toPath()));
        } else {
            exchange.sendResponseHeaders(200, line.getBytes().length);
            os.write(line.getBytes());
            // System.err.println("File not found: " + path.getAbsolutePath());
            // exchange.sendResponseHeaders(404, 0);
            // os.write("404 File not found.".getBytes());
        }

        // String response = "<html><body>" + requestURI + "</body></html>";
        // exchange.sendResponseHeaders(200, response.getBytes().length);

        // os.write(response.getBytes());
        os.close();

    }

    private static void printRequestInfo(HttpExchange exchange) {
        System.out.println("-- headers --");
        Headers requestHeaders = exchange.getRequestHeaders();
        requestHeaders.entrySet().forEach(System.out::println);

        System.out.println("-- principle --");
        HttpPrincipal principal = exchange.getPrincipal();
        System.out.println(principal);

        System.out.println("-- HTTP method --");
        String requestMethod = exchange.getRequestMethod();
        System.out.println(requestMethod);

        System.out.println("-- query --");
        URI requestURI = exchange.getRequestURI();
        String query = requestURI.getQuery();
        System.out.println(query);
    }

}
