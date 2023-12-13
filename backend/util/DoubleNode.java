package util;

public class DoubleNode<T> {
    public T value;
    public DoubleNode<T> next;
    public DoubleNode<T> prev;

    public DoubleNode(T value) {
        this.value = value;
        this.next = null;
        this.prev = null;
    }

    public DoubleNode(T value, DoubleNode<T> next, DoubleNode<T> prev) {
        this.value = value;
        this.next = next;
        this.prev = prev;
    }
}
