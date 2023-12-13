package core;

import util.DoubleNode;

import java.util.UUID;

public class Conversation {

    private DoubleNode<Message> messages;

    private User user1;
    private User user2;

    private String id;

    public Conversation(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.id = UUID.randomUUID().toString();
    }

    public void addMessage(Message message) {
        DoubleNode<Message> newNode = new DoubleNode<>(message);
        if (messages == null) {
            messages = newNode;
        } else {
            newNode.next = messages;
            messages.prev = newNode;
            messages = newNode;
        }
    }

    public Message[] getMessageArray() {
        if (messages == null) {
            return new Message[0];
        }

        DoubleNode<Message> curr = messages;
        int size = 1;
        while (curr.next != null) {
            size++;
            curr = curr.next;
        }

        Message[] messageArray = new Message[size];
        for (int i = size - 1; i >= 0; i--) {
            messageArray[i] = curr.value;
            curr = curr.prev;
        }

        return messageArray;
    }

    public String getID() {
        return id;
    }

    public User getOtherUser(User main) {
        if (main.equals(user1)) {
            return user2;
        } else {
            return user1;
        }
    }

    public User getUser1() {
        return user1;
    }

    public User getUser2() {
        return user2;
    }
}
