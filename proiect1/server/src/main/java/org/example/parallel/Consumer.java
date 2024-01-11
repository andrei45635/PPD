package org.example.parallel;

import org.example.list.ConcurrentLinkedList;
import org.example.list.MyLinkedList;
import org.example.list.Node;
import org.example.queue.MyQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable {
    private final MyQueue<Node> myQueue;
    private MyLinkedList myLinkedList;
    private ConcurrentLinkedList concurrentLinkedList;
    private boolean running = false;
    private AtomicBoolean stillMoreWork;
    private CountDownLatch latch;
    private ConcurrentHashMap<Integer, String> disqualified = new ConcurrentHashMap<>();

    public Consumer(MyLinkedList myLinkedList, MyQueue<Node> myQueue, AtomicBoolean stillMoreWork) {
        this.myLinkedList = myLinkedList;
        this.myQueue = myQueue;
        this.stillMoreWork = stillMoreWork;
    }

    public Consumer(ConcurrentLinkedList concurrentLinkedList, MyQueue<Node> myQueue, AtomicBoolean stillMoreWork, CountDownLatch latch) {
        this.concurrentLinkedList = concurrentLinkedList;
        this.myQueue = myQueue;
        this.stillMoreWork = stillMoreWork;
        this.latch = latch;
    }

    @Override
    public void run() {
        running = true;
        consume();
        //latch.countDown();
    }

    public void stop() {
        running = false;
        myQueue.notifyIsNotEmpty();
    }

    public void consume() {
        while (running) {
            if (myQueue.isEmpty()) {
                try {
                    myQueue.waitIsNotEmpty();
                } catch (InterruptedException ie) {
                    System.out.println("Error in consume method in Consumer: " + ie.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!running || !stillMoreWork.get()) {
                break;
            }
            while (!myQueue.isEmpty()) {
                Node poppedNode = myQueue.dequeue();
                System.out.println(poppedNode);
                if (poppedNode != null) {
                    this.concurrentLinkedList.updateList(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                }
            }
        }
    }
}
