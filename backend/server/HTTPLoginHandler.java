package server;

import core.User;
import core.UserService;
import util.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class HTTPLoginHandler extends Handler {


    public HTTPLoginHandler(Socket client, BufferedReader in, PrintWriter out, Request request) throws IOException {
        super(client, in, out, request);
    }

    @Override
    public void run() {
        try {
            int contentLen = request.getHeader("Content-Length") == null ? 0 : Integer.parseInt(request.getHeader("Content-Length"));
            char[] body = new char[contentLen];

            in.read(body, 0, contentLen);

            HashMap<String, Object> bodyMap = JSON.parseJSON(new String(body));

            String username = (String) bodyMap.get("username");
            String password = (String) bodyMap.get("password");

            if (username == null || password == null) {
                new Response(Response.Status.BAD_REQUEST).send(out);
                client.close();
                return;
            }


            byte[] hashedPasswordBytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder passwordBuilder = new StringBuilder();
            for (byte b : hashedPasswordBytes) {
                passwordBuilder.append(String.format("%02x", b));
            }


            User user = UserService.findUser(username);

            if (user == null || !user.getHashedPassword().equals(passwordBuilder.toString())) {
                String returnVal = JSON.createJSON(
                        Map.of(
                                "error", (user == null) ? "User does not exist" : "Incorrect password",
                                "success", false
                        )
                );

                new DataResponse(Response.Status.UNAUTHORIZED, returnVal.getBytes(), "application/json").send(out);
                client.close();
                return;
            }

            String returnVal = JSON.createJSON(
                    Map.of(
                            "success", true
                    )
            );


            Response r = new DataResponse(Response.Status.OK, returnVal.getBytes(), "application/json");
            r.addHeader("Set-Cookie", Response.SET_COOKIE_FORMAT.formatted(UserService.generateJWT(user)));
            r.send(out);
            client.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "Login" + super.toString();
    }
}
