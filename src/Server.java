import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.io.*;
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

        OutputStream os = exchange.getResponseBody();

        // Normal
        if (requestURI.getPath().equals("/")) {
            String response = "<html><body><h1>hi, welcome to my super good java app</body></html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            os.write(response.getBytes());
        }
        // PATH Traversal from file URL param
        // example -> http://localhost:9999/file?file=../pathTRAVERSAL.html
        else if (requestURI.getPath().equals("/file")) {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            File path = new File(params.get("file"));
            System.out.println(path.exists());
            if (path.exists()) {
                exchange.sendResponseHeaders(200, path.length());
                os.write(Files.readAllBytes(path.toPath()));
            } else {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.getBytes().length);
                os.write(response.getBytes());
            }
        }
        // OS cmd injection, get output from test URL param
        // example -> http://localhost:9999/cmd?test=ls
        else if (requestURI.getPath().equals("/cmd")) {
            Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
            System.out.println("HERE!!!\n");
            String testQ = params.get("test");
            Process process = Runtime.getRuntime().exec(testQ);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                line += tmp + "\n";
            }
            exchange.sendResponseHeaders(200, line.getBytes().length);
            os.write(line.getBytes());
        }
        // Normal 404
        else {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            os.write(response.getBytes());
        }
        System.out.println("Close out and send back");
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
