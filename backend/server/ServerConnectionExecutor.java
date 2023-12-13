package server;

import util.BlockingQueue;
import util.DoubleNode;
import util.Node;

public class ServerConnectionExecutor {

    private Thread[] httpThreads; // array of http threads
    private BlockingQueue<Handler> requests = new BlockingQueue<>();; // queue of requests (linked list)

    public ServerConnectionExecutor(int threadCount) {
        httpThreads = new Thread[threadCount]; // creates array of threads

        for (int i = 0; i < threadCount; i++) { // for each thread
            httpThreads[i] = new Thread(() -> { // create a new thread
                while (true) { // while true
                    Handler handler = requests.dequeue(); // dequeue a request
                    handler.run(); // run the request
                }
            });
            httpThreads[i].start(); // start the thread
        }
    }

    public void execute(Handler handler) {
        requests.enqueue(handler);
    }
}
