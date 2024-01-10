package org.example.parallel;

import org.example.list.MyLinkedList;
import org.example.list.Node;
import org.example.list.ProcessedList;
import org.example.parallel.queue.MyQueue;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable {
    private final MyQueue<Node> myQueue;
    private final MyLinkedList myLinkedList;
    private boolean running = false;
    private final int pid;
    private AtomicBoolean stillMoreWork = new AtomicBoolean();
    private ConcurrentHashMap<Integer, String> disqualified = new ConcurrentHashMap<>();
    private ProcessedList processedList = new ProcessedList();

    public Consumer(MyLinkedList myLinkedList, MyQueue<Node> myQueue, int pid, AtomicBoolean stillMoreWork) {
        this.myLinkedList = myLinkedList;
        this.myQueue = myQueue;
        this.pid = pid;
        this.stillMoreWork = stillMoreWork;
    }

    @Override
    public void run() {
        running = true;
        try {
            consume(pid);
        } catch (RuntimeException e) {
            System.out.println("Error when running the consumer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        running = false;
        myQueue.notifyIsNotEmpty();
    }

    public void consume(int pid) throws RuntimeException {
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
                Node poppedNode = new Node();
                try {
                    poppedNode = myQueue.dequeue();
                } catch (RuntimeException re) {
                    System.out.println("Error when trying to dequeue: " + re.getMessage());
                }
                poppedNode.lock();
                if (!disqualified.containsKey(poppedNode.getParticipantID())) {
                    if (poppedNode.getScore() == -1) {
                        myLinkedList.deleteNode(poppedNode.getParticipantID());
                        disqualified.put(poppedNode.getParticipantID(), poppedNode.getCountry());
                    } else {
                        Node target = myLinkedList.updateNode(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                        if (target == null) {
                            myLinkedList.addNode(poppedNode.getParticipantID(), poppedNode.getScore(), poppedNode.getCountry());
                        }
                    }
                }
                poppedNode.unlock();
            }
        }
    }
}