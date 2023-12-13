package util;

public class BlockingQueue<T> {
    private Node<T> head; // head of queue
    private Node<T> tail; // tail of queue (allows for O(1) enqueue)

    public BlockingQueue() {
        head = null; // starts of with head as null
        tail = null; // starts of with tail as null
    }

    public synchronized void enqueue(T value) {
        Node<T> newNode = new Node<>(value); // creates a new node with the value
        if (head == null) { // if nothing in list (list is empty), add as head and tail
            head = newNode;
            tail = newNode;
        } else { // if there is something in the list, add to tail
            tail.next = newNode;
            tail = newNode;
        }

        System.out.println("Enqueued " + value);
        notifyAll(); // notify all threads waiting for queue to be non-empty
    }

    public synchronized T dequeue() {
        try {
            while (head == null) { // if list is empty
                wait(); // waits until queue is non-empty
            }

            System.out.println("Dequeued " + head.value);

            T value = head.value; // stores head value
            head = head.next; // pop first in head
            tail = head == null ? null : tail; // if head is null, tail is null

            return value;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
