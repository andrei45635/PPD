package org.example.parallel;

import org.example.list.ConcurrentLinkedList;
import org.example.list.MyLinkedList;
import org.example.queue.MyQueue;
import org.example.list.Node;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable {
    private final MyQueue<Node> myQueue;
    private MyLinkedList myLinkedList;
    private ConcurrentLinkedList concurrentLinkedList;
    private boolean running = false;
    private AtomicBoolean stillMoreWork = new AtomicBoolean();
    private ConcurrentHashMap<Integer, String> disqualified = new ConcurrentHashMap<>();

    public Consumer(MyLinkedList myLinkedList, MyQueue<Node> myQueue, AtomicBoolean stillMoreWork) {
        this.myLinkedList = myLinkedList;
        this.myQueue = myQueue;
        this.stillMoreWork = stillMoreWork;
    }

    public Consumer(ConcurrentLinkedList concurrentLinkedList, MyQueue<Node> myQueue) {
        this.concurrentLinkedList = concurrentLinkedList;
        this.myQueue = myQueue;
    }

    @Override
    public void run() {
        running = true;
        try {
            consume();
        } catch (RuntimeException e) {
            System.out.println("Error when running the consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        myQueue.notifyIsNotEmpty();
    }

    public void consume() throws RuntimeException {
        while (running) {
            if (myQueue.isEmpty()) {
                try {
                    myQueue.waitIsNotEmpty();
                } catch (InterruptedException ie) {
                    System.out.println("Error in consume method in Consumer: " + ie.getMessage());
                }
            }
            if (!running) {
                break;
            }
            while (!myQueue.isEmpty()) {
                System.out.println("in here -> means the queue isn't empty");
                Node poppedNode;
                try {
                    poppedNode = myQueue.dequeue();
                    System.out.println(poppedNode.toString());
                    if (poppedNode != null) {
                        this.concurrentLinkedList.updateList(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                    } else {
                        //TODO: why is poppedNode null??
                        System.out.println("poppedNode is null");
                    }
                } catch (RuntimeException re) {
                    System.out.println("Error when trying to dequeue: " + re.getMessage());
                }
            }
        }
    }
}