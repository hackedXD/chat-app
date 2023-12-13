package server;

import core.UserService;

import java.io.PrintWriter;
import java.util.HashMap;


public class Response {

    private static final String ACCESS_CONTROL_HEADER = "Access-Control-Allow-Origin: *";
    private static final String ACCESS_CREDS_HEADER = "Access-Control-Allow-Credentials: true";

    public static final String SET_COOKIE_FORMAT = "jwt=%s; Path=/; HttpOnly; ";

    private HashMap<String, String> headers = new HashMap<>();

    public enum Status {
        OK(200, "OK"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        UNAUTHORIZED(401, "Unauthorized");

        private int code;
        private String message;

        Status(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public String toString() {
            return code + " " + message;
        }
    }

    private Status status;

    public Response(Status status) {
        this.status = status;
    }

    public void addHeader(String header, String value) {
        headers.put(header, value);
    }

    public void send(PrintWriter out) {
        System.out.println("Sending response: " + status);
        out.println("HTTP/1.1 " + status);
        out.println(ACCESS_CONTROL_HEADER);
        out.println(ACCESS_CREDS_HEADER);
        for (String header : headers.keySet()) {
            out.println(header + ": " + headers.get(header));
        }
        out.println();
        out.flush();
    }
}
