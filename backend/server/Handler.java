package server;

import core.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Abstract handler class, used to handle requests from clients
 */
public abstract class Handler implements Runnable { // standard handler

    /**
     * Client Socket
     */
    protected final Socket client; // client socket, if protected then it can be accessed by subclasses

    /**
     * Buffered Reader (wrapped around input stream as it is more efficient)
     */
    protected final BufferedReader in;


    /**
     * Print Writer (wrapped around output stream as it is more efficient)
     */
    protected final PrintWriter out;


    /**
     * Request object (stores method type and path)
     */
    protected final Request request;

    /**
     * User object (stores user data) (null for everything except WebSocketHandler)
     */
    protected User user = null;


    /**
     * Sets user object (used only in WebSocketHandler)
     * @param user The user object to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    public Handler(Socket client, BufferedReader in, PrintWriter out, Request request) throws IOException {
        this.client = client;
        this.in = in;
        this.out = out;
        this.request = request;
    }

    public abstract void run(); // abstract method, must be implemented by subclasses

    @Override
    public String toString() { // for debug
        return "Handler{" +
                "method=" + request.getMethod() +
                ", path=" + request.getPath() +
                ", client=" + client +
                ", in=" + in +
                ", out=" + out +
                '}';
    }
}
