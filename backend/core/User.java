package core;

import server.WebSocketHandler;
import util.Node;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String name;
    private String email;
    private String hashedPassword;

    private Node<Conversation> messages;
    private Node<WebSocketHandler> sessions;

    public User next;
    public User prev;

    public User(String name, String email, String hashedPassword) {
        try {
            this.name = name;
            this.email = email;
            this.hashedPassword = hashedPassword;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsername() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getConversations() {
        Map<String, Object> map = new HashMap<>();

        Node<Conversation> curr = messages;
        while (curr != null) {
            map.put(curr.value.getID(), curr.value.getOtherUser(this).getName());
            curr = curr.next;
        }

        return map;
    }

    public void addConversation(Conversation c) {
        Node<Conversation> newNode = new Node<>(c);
        if (messages == null) {
            messages = new Node<>(c);
        } else {
            newNode.next = messages;
            messages = newNode;
        }
    }

    public Conversation getConversation(String id) {
        Node<Conversation> curr = messages;
        while (curr != null) {
            if (curr.value.getID().equals(id)) {
                return curr.value;
            }
            curr = curr.next;
        }
        return null;
    }

    public void addSession(WebSocketHandler session) {
        Node<WebSocketHandler> newNode = new Node<>(session);
        if (sessions == null) {
            sessions = newNode;
        } else {
            newNode.next = sessions;
            sessions = newNode;
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof User) && ((User) obj).email.equals(email);
    }

    public void sendMessage(String json) {
        Node<WebSocketHandler> curr = sessions;

        while (curr != null) {
            curr.value.sendMessage(json);
            curr = curr.next;
        }
    }

    public void removeSession(WebSocketHandler webSocketHandler) {
        Node<WebSocketHandler> curr = sessions;

        if (curr == null) {
            return;
        }

        if (curr.value.equals(webSocketHandler)) {
            sessions = sessions.next;
            return;
        }

        while (curr.next != null) {
            if (curr.next.value.equals(webSocketHandler)) {
                curr.next = curr.next.next;
                return;
            }
            curr = curr.next;
        }
    }
}
