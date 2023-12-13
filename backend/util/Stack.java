package util;

public class Stack<T> {
    private Node<T> top;

    public void push(T value) {
        Node<T> newNode = new Node<>(value);
        if (top == null) {
            top = newNode;
        } else {
            newNode.next = top;
            top = newNode;
        }
    }

    public T pop() {
        if (top == null) {
            return null;
        }

        T value = top.value;
        top = top.next;
        return value;
    }

    public T peek() {
        if (top == null) {
            return null;
        }

        return top.value;
    }

    public boolean isEmpty() {
        return top == null;
    }
}
