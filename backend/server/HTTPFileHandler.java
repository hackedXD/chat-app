package server;

import java.io.*;
import java.net.Socket;

/**
 * Handles serving all files, also fallback for every path incase not found
 */
public class HTTPFileHandler extends Handler {

    /**
     * Constructor for Handler.
     * @param client The client socket.
     * @param in The buffered reader.
     * @param out The print writer.
     * @param request The request object.
     * @throws IOException If an I/O error occurs.
     */
    public HTTPFileHandler(Socket client, BufferedReader in, PrintWriter out, Request request) throws IOException {
        super(client, in, out, request);
    }


    /**
     * Runs the handler.
     */
    public void run() { // is called when receiving a file request (which is a wildcard)
        try {
            if (request.getPath().equals("/")) request.setPath("/index.html"); // if searching for / => serve index.html

            InputStream inputStream = getClass().getResourceAsStream(request.getPath()); // gets input stream from file

            if (inputStream == null) request.setPath("/index.html"); // if file not found => serve index.html
            inputStream = getClass().getResourceAsStream(request.getPath()); // get local resource

            byte[] data = inputStream.readAllBytes(); // reads all bytes from file

            new DataResponse(Response.Status.OK, data, getContentType()).send(out, client.getOutputStream()); // return file
            client.close(); // close conn
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the content type of the file.
     * @return The content type of the file.
     */
    private String getContentType() {
        String[] parts = request.getPath().split("\\."); // get file extension
        String extension = parts[parts.length - 1]; // get last part of path (which is the extension), assumes that dot does exist in path
        return switch (extension) { // matches extension to content type
            case "html" -> "text/html";
            case "js" -> "text/javascript";
            case "css" -> "text/css";
            case "png" -> "image/png";
            case "jpg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";
            default -> "text/plain";
        };
    }

    @Override
    public String toString() {
        return "HTTPFile" + super.toString();
    }
}