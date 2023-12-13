package core;

public class Message {
    protected final String content;
    protected final User sender;

    public Message(String content, User sender) {
        this.content = content;
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public User getSender() {
        return sender;
    }


    @Override
    public String toString() {
        return "{\"from\": \"" + sender.getUsername() + "\", \"content\": \"" + content + "\"}";
    }
}
