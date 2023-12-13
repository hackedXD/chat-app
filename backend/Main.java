import server.*;

public class Main {
    public static void main(String[] args) {
        if (args[0] == null) {
            args[0] = "8080"; // if no argument provided set to default 8080
        }

        if (args[1] == null) {
            args[1] = String.valueOf(8); // if no argument provided set to default 8
        }

        System.out.println("Starting server on port " + args[0] + " with " + args[1] + " threads");

        MathChatServer server = new MathChatServer(Integer.parseInt(args[0]), Integer.parseInt(args[1])); // custom server using ServerSocket api (takes in port)

        server.registerHandler("GET", "*", HTTPFileHandler.class); // registers everything else to file server
        server.registerHandler("POST", "/login", HTTPLoginHandler.class); // register login handler to login route
        server.registerHandler("POST", "/register", HTTPRegisterHandler.class); // register signup handler to signup route
        server.registerHandler("GET", "/auth", HTTPAuthHandler.class); // register faux auth handler to /auth route

        server.run(); // runs the server
    }
}
