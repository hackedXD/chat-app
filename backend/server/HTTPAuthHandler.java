package server;

import core.User;
import core.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Faux handler class, used as an authentication layer for the WebSocketHandler
 */
public class HTTPAuthHandler extends Handler {

    /**
     * Constructor for Handler.
     * @param client The client socket.
     * @param in The buffered reader.
     * @param out The print writer.
     * @param request The request object.
     * @throws IOException If an I/O error occurs.
     */
    public HTTPAuthHandler(Socket client, BufferedReader in, PrintWriter out, Request request) throws IOException {
        super(client, in, out, request);
    }

    /**
     * Runs the handler.
     */
    @Override
    public void run() {
        authHandlerTry: // label for readability (breaks out of try block)
        try {
            String jwt = request.getHeader("Cookie"); // gets cookie
            if (jwt == null) break authHandlerTry;

            Matcher match = Pattern.compile("jwt=([^;]+)").matcher(jwt); // searches for JWT
            if (!match.find()) break authHandlerTry;

            jwt = match.group(1); // jwt=...

            Map<String, Object> payload = UserService.verifyJWT(jwt); // verifies JWT (null if invalid)

            if (payload == null) break authHandlerTry; // invalid JWT
            if (!payload.containsKey("exp") || (long) payload.get("exp") < System.currentTimeMillis()) break authHandlerTry; // expired JWT

            String username = (String) payload.get("sub"); // gets username from JWT
            if (username == null) break authHandlerTry;

            User user = UserService.findUser(username); // finds corresponding user
            if (user == null) break authHandlerTry;

            String path = "/api/" + UUID.randomUUID().toString(); // creates temporary api path
            String body = "{\"path\": \"" + path + "\"}"; // creates return body (contains path)

            MathChatServer.webSocketConnections.put(path, user); // adds temporary path

            new DataResponse(Response.Status.OK, body.getBytes(StandardCharsets.UTF_8), "application/json").send(out); // sends return body with path (only if authenticated)
            client.close();
        } catch (Exception ignored) {} finally {
            try {
                if (!client.isClosed()) {
                    new Response(Response.Status.UNAUTHORIZED).send(out);
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
