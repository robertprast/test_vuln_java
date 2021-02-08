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

public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(Server::handleRequest);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        // printRequestInfo(exchange);
        String name = new File(requestURI.getPath()).getName();
        File path = new File("./", name);
        OutputStream os = exchange.getResponseBody();

        if (path.exists()) {
            exchange.sendResponseHeaders(200, path.length());
            os.write(Files.readAllBytes(path.toPath()));
        } else {
            System.err.println("File not found: " + path.getAbsolutePath());

            exchange.sendResponseHeaders(404, 0);
            os.write("404 File not found.".getBytes());
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
