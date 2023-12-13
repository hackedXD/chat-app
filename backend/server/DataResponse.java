package server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;

/**
 * This class is used to send a HTTP response to the client ALONGSIDE data (usually json).
 */
public class DataResponse extends Response {

    /**
     * The data to send to the client.
     */
    private byte[] data;


    /**
     * Constructor for DataResponse.
     * @param status The status of the response.
     * @param data The data to send to the client.
     * @param contentType The content type of the data.
     */
    public DataResponse(Status status, byte[] data, String contentType) {
        super(status);
        this.data = data;
        addHeader("Content-Type", contentType);
        addHeader("Content-Length", String.valueOf(data.length));
    }


    /**
     * Sends the response to the client.
     * @param out The print stream to send the response to.
     */
    @Override
    public void send(PrintWriter out) {
        super.send(out);
        for (byte b : data) {
            out.write(b);
        }
        out.write("\r\n");
        out.flush();
    }

    /**
    * Sends the response to the client.
    * @param out The print stream to send the response to.
    * @param os The output stream to send the data to.
     */
    public void send(PrintWriter out, OutputStream os) throws IOException { // same as above but used in file sending as for some mysterious reason doesnt work without i
        super.send(out);
        os.write(data, 0, data.length);
        os.write("\r\n".getBytes());
        out.flush();
    }
}
