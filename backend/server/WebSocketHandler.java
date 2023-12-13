package server;

import core.*;
import util.JSON;
import util.Node;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class WebSocketHandler extends Handler {

    private Node<byte[]> message;
    private Node<byte[]> messageEnd;

    public WebSocketHandler(Socket client, BufferedReader in, PrintWriter out, Request request) throws IOException {
        super(client, in, out, request);
    }

    @Override
    public void run() {
        try {
            boolean handshakeSuccessful = websocketHandshake();
            if (!handshakeSuccessful) return;

            user.addSession(this);

            Map<String, Object> userData = new HashMap<>();
            userData.put("type", "userData");
            userData.put("name", user.getName());
            userData.put("username", user.getUsername());
            userData.put("conversations", user.getConversations());
            sendMessage(JSON.createJSON(userData));

            while (!client.isClosed() && client.isConnected()) decode();
        } finally {
            try {
                client.close();
                user.removeSession(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessage(String message) {
        Map<String, Object> json = JSON.parseJSON(message);

        String type = (String) json.get("type");

        switch (type) {
            case "addConversation":
                String otherUser = (String) json.get("otherUser");

                if (otherUser.equals(user.getUsername())) {
                    sendMessage(JSON.createJSON(Map.of("type", "conversations", "error", "You can't add yourself")));
                    return;
                }

                User other = UserService.findUserAround(otherUser, user);

                if (other == null) {
                    sendMessage(JSON.createJSON(Map.of("type", "conversations", "error", "User not found")));
                    return;
                }

                if (user.getConversations().containsValue(other)) {
                    sendMessage(JSON.createJSON(Map.of("type", "conversations", "error", "Conversation already exists")));
                    return;
                }

                Conversation c = new Conversation(user, other);
                user.addConversation(c);
                other.addConversation(c);

                other.sendMessage(JSON.createJSON(Map.of("type", "conversations", "conversations", other.getConversations())));
                user.sendMessage(JSON.createJSON(Map.of("type", "conversations", "conversations", user.getConversations())));

                break;
            case "message":
                String conversationID = (String) json.get("conversation");
                String messageText = ((String) json.get("message")).trim();

                Conversation conversation = user.getConversation(conversationID);
                if (conversation == null) {
                    sendMessage(JSON.createJSON(Map.of("type", "error", "message", "Conversation not found")));
                    return;
                }

                if (messageText.startsWith("$") && messageText.endsWith("$") && messageText.length() > 2) {
                    conversation.addMessage(new MathMessage(messageText.substring(1, messageText.length() - 1), user));
                } else {
                    conversation.addMessage(new Message(messageText, user));
                }

                User otherUserObj = conversation.getOtherUser(user);
                user.sendMessage(JSON.createJSON(Map.of("type", "conversation", "id", conversationID, "messages", conversation.getMessageArray())));
                otherUserObj.sendMessage(JSON.createJSON(Map.of("type", "conversation", "id", conversationID, "messages", conversation.getMessageArray())));

                break;
            case "getConversation":
                String conversationID2 = (String) json.get("conversation");

                Conversation conversation2 = user.getConversation(conversationID2);
                if (conversation2 == null) {
                    sendMessage(JSON.createJSON(Map.of("type", "error", "message", "Conversation not found")));
                    return;
                }

                sendMessage(JSON.createJSON(Map.of("type", "conversation", "id", conversationID2, "messages", conversation2.getMessageArray())));

                break;
        }
    }

    private String concatenateMessage() {
        StringBuilder sb = new StringBuilder();
        Node<byte[]> node = message;

        while (node != null) {
            sb.append(new String(node.value, StandardCharsets.UTF_8));
            node = node.next;
        }

        message = null;
        messageEnd = null;

        return sb.toString();
    }

    private boolean websocketHandshake() {
        try {
            String websocketKey = request.getHeader("Sec-WebSocket-Key");

            if (websocketKey == null) {
                out.println("HTTP/1.1 400 Bad Request");
                out.println();
                out.flush();
                return false;
            }


            String websocketAccept = websocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            byte[] sha1 = MessageDigest.getInstance("SHA-1").digest(websocketAccept.getBytes());
            websocketAccept = Base64.getEncoder().encodeToString(sha1);

            out.print(
                "HTTP/1.1 101 Switching Protocols\r\n" +
                "Upgrade: websocket\r\n" +
                "Connection: Upgrade\r\n" +
                "Sec-WebSocket-Accept: " + websocketAccept + "\r\n" +
                "\r\n"
            );
            out.flush();

            return true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void decode() {
        try {
            InputStream is = client.getInputStream();

            int firstByte = is.read();
            int secondByte = is.read();
            if (firstByte == -1 || secondByte == -1) return;

            boolean fin = (firstByte & 0b10000000) != 0;
            int opcode = firstByte & 0b00001111;

            boolean mask = (secondByte & 0b10000000) != 0;
            int length = secondByte & 0b01111111;

            if (length > 125) {
                boolean isEightByteLength = length == 127;

                length = 0;

                for (int i = 0; i < (isEightByteLength ? 8 : 2); i++) {
                    length = (length << 8) | (is.read() & 0xFF);
                }
            }

            byte[] key = new byte[4];
            is.read(key);

            byte[] data = new byte[length];
            is.read(data);

            if (mask) {
                for (int i = 0; i < data.length; i++) {
                    data[i] = (byte) (data[i] ^ key[i % 4]);
                }
            }

            switch (opcode) {
                case 0x0: // continuation frame
                    Node<byte[]> node = new Node<>(data);
                    if (messageEnd != null) {
                        messageEnd.next = node;
                        messageEnd = node;
                    } else {

                    }
                    break;
                case 0x1: // text frame
                    message = new Node<>(data);
                    messageEnd = message;

                    if (fin) {
                        handleMessage(concatenateMessage());
                    }

                    break;
                case 0x2: // binary frame (not supported / used in program)
                    break;
                case 0x8: // connection close
                    sendStopConnection();
                    client.close();
                    break;
                case 0x9: // ping
                    pong(data);
                    break;
                case 0xA: // pong
                    break;
                default:
                // ignoring rn
                return;
            }



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ping() {
        try {
            OutputStream is = client.getOutputStream();
            is.write(encode("ping", (byte) 0x9));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pong(byte[] message) {
        try {
            OutputStream is = client.getOutputStream();
            is.write(encode(new String(message, StandardCharsets.UTF_8), (byte) 0xA));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) {
        try {
            OutputStream is = client.getOutputStream();
            is.write(encode(msg, (byte) 0x1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendStopConnection() {
        try {
            OutputStream is = client.getOutputStream();
            is.write(encode("", (byte) 0x8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] encode(String msg, byte opcode) {
        byte[] payloadData = msg.getBytes(StandardCharsets.UTF_8); // get message bytes

        int headerLen = 1; // standard additional len
        if (payloadData.length <= 125) {
            headerLen += 1; // if standard just do two
        } else if (payloadData.length < (1 << 16)) {
            headerLen += 3; // else you get 4 bytes to store length
        } else {
            headerLen += 9; // else you get 10 bytes to store length
        }

        byte[] response = new byte[payloadData.length + headerLen]; // create new response payload
        response[0] = (byte) (0b10000000 | opcode); // alw final message, adds passed in opcode at the end
        response[1] = 0; // mask bit is 0, length will be set

        if (payloadData.length <= 125) {
            response[1] |= payloadData.length; // if standard just set length
        } else if (payloadData.length < (1 << 16)) {
            response[1] |= 126; // else set to 126
            for (int i = 0; i < 2; i++) {
                response[2 + i] = (byte) (payloadData.length >> (8 * (1 - i))); // and then encode length in next 2 bytes (+ 2 original)
            }
        } else {
            response[1] |= 127; // else set to 127
            for (int i = 0; i < 8; i++) {
                response[2 + i] = (byte) (payloadData.length >> (8 * (7 - i))); // and then encode length in next 8 bytes
            }
        }

        System.arraycopy(payloadData, 0, response, headerLen, payloadData.length); // copy payload (no mask)

        return response;
    }

    @Override
    public String toString() {
        return "WebSocket" + super.toString();
    }
}