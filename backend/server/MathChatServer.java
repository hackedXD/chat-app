package server;

import core.User;
import core.UserService;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Server class for MathChat
 * <p>Handles requests from clients and sends to corresponding handler</p>
 */
public class MathChatServer {

    /**
     * Port number to run server on
     */
    private final int port;
    private final int threads;

    /**
     * Custom server executor using custom blocking queue (using linked list as queue)
     */
    private ServerConnectionExecutor executor;


    /**
     * Boolean to check if server is running
     */
    private boolean running = true;

    /**
     * HashMap of requests to handlers, used to find handler when request is made
     */
    private final HashMap<Request, Class<? extends Handler>> handlers = new HashMap<>();

    public static final HashMap<String, User> webSocketConnections = new HashMap<>();

    /**
     * Constructor for MathChatServer
     * @param port port number to run server on, stored in {@link #port}
     */
    public MathChatServer(int port, int threads) {
        this.port = port; // sets custom port
        this.threads = threads;
    }

    /**
     * Registers handler to server, adds to {@link #handlers}
     *
     * <p></p>
     * <p><b>Precondition:</b> request is not null, handler is not null, request is not already in {@link #handlers}</p>
     * <p><b>Postcondition:</b> request is in {@link #handlers} with corresponding handler</p>
     *
     * @param request request to register handler to
     * @param handler handler to register
     */
    public void registerHandler(Request request, Class<? extends Handler> handler) {
        handlers.put(request, handler); // adds the request and handler to the map
    }

    /**
     * Overloaded method for {@link #registerHandler(Request, Class)}. Creates request from method and path
     *
     * <p></p>
     * <p><b>Precondition:</b> method is not null, path is not null</p>
     * <p><b>Postcondition:</b> request is in {@link #handlers} with corresponding handler</p>
     *
     * @param method method to register handler to
     * @param path path to register handler to
     * @param handler handler to register
     */
    public void registerHandler(String method, String path, Class<? extends Handler> handler) {
        registerHandler(new Request(method, path), handler); // overloads registerHandler to take in method and path
    }

    /**
     * Runs server
     * Creates server socket, then creates custom executor
     * While server is running, accepts client and handles request using {@link #handleRequest(Socket)}
     *
     * <p></p>
     * <p><b>Precondition:</b> Port is set</p>
     * <p><b>Postcondition:</b> Server is running and accepting requests</p>
     */
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.port); // takes in server socket

            executor = new ServerConnectionExecutor(this.threads); // instantiates custom executor with 8 threads
            while (running) handleRequest(serverSocket.accept()); // handles request for every client accepted
        } catch (IOException e) {
            e.printStackTrace(); // printed on errors, usually port already in use
        }
    }

    /**
     * Handles request for client
     * Creates buffered reader and print writer for input and output
     * Parses request from first line of input using {@link #parseRequest(BufferedReader, PrintWriter)}
     * If request is null, then bad request, so we return
     * Gets handler from {@link #handlers} using request
     * If handler is null, then we return 404 not found
     * If handler is not null, then we create handler using reflection and add to executor
     *
     * <p></p>
     * <p><b>Precondition:</b> Socket is connected</p>
     * <p><b>Postcondition:</b> Corresponding handler is in executor queue</p>
     *
     * @param client
     */
    public void handleRequest(Socket client) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream())); // creates buffered reader for input (uses buffered reader as more efficient for reading)
            PrintWriter out = new PrintWriter(new BufferedOutputStream(client.getOutputStream()), true); // same thing but for writing, wraps in printwriter

            Request request = parseRequest(in, out); // parses request from first line of input
            if (request == null) return; // if bad request, return val will be null so we quit

            Class<? extends Handler> handlerClass = handlers.get(request); // get corresponding handler for request

            User user = null;
            if (webSocketConnections.containsKey(request.getPath())) { // looks for temp websocket connection
                handlerClass = WebSocketHandler.class;
                user = webSocketConnections.remove(request.getPath()); // if does exist pops it off
            }

            if (handlerClass == null) handlerClass = handlers.get(new Request(request.getMethod(), "*")); // if not found, put under wildcard handler



            if (handlerClass == null) { // if no wild card, handler (and thus webpage) not found
                new Response(Response.Status.NOT_FOUND).send(out); // sends 404 not found
                return;
            }

            Handler handler = handlerClass.getConstructor(Socket.class, BufferedReader.class, PrintWriter.class, Request.class).newInstance(client, in, out, request); // gets handler constructor
            if (user != null) handler.setUser(user); // if not null set user to something (in our case only WebSocketHandler has user)
            executor.execute(handler); // adds handler to execution queue
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Creates request object from input stream using regex
     * <p></p>
     * <p><b>Precondition:</b> Input and Output streams are connected and from client. First line of input contains HTTP request</p>
     * <p><b>Postcondition:</b> Request containing method type and path from first line of input. null if bad request</p>
     *
     * @param in Input stream to read from
     * @param out Output stream to write to
     * @return Request object from first line of input (method and path)
     * @throws IOException Exception handled by {@link #handleRequest(Socket)}
     */
    private Request parseRequest(BufferedReader in, PrintWriter out) throws IOException {
        String requestHeader = in.readLine(); // takes first line


        Matcher httpRequestPattern = Pattern.compile("(GET|POST) (\\S+) HTTP/1.1").matcher(requestHeader); // regex with two groups, group 1 gives type, group 2 gives path

        if (!httpRequestPattern.find()) { // if didnt find match for our regex, uninterested bad request
            out.println("HTTP/1.1 400 Bad Request");
            out.println();
            out.flush();
            return null;
        }

        String method = httpRequestPattern.group(1); // takes group 1: method
        String path = httpRequestPattern.group(2); // takes group 2: path

        HashMap<String, String> headers = new HashMap<>(); // map of headers

        String prevLine = in.readLine(); // get curr line
        while (!prevLine.isEmpty()) { // while the last line is empty
            String[] header = prevLine.split(": "); // read header
            headers.put(header[0], header[1]); // split and add
            prevLine = in.readLine(); // read next line
        }


        return new Request(method, path, headers); // returns new request(just wrapper of both method and path)
    }
}
