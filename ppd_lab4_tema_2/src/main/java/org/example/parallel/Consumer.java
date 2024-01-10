package org.example.parallel;

import org.example.parallel.queue.MyQueue;
import org.example.list.MyLinkedList;
import org.example.list.Node;

import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer implements Runnable {
    private final MyQueue<Node> myQueue;
    private final MyLinkedList myLinkedList;
    private boolean running = false;
    private final int pid;
    private AtomicBoolean stillMoreWork = new AtomicBoolean();

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
            System.out.println("Error: " + e.getMessage());
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
            while(stillMoreWork.get()){
                while (!myQueue.isEmpty()) {
                    Node poppedNode = new Node();
                    try{
                        poppedNode = myQueue.dequeue();
                    } catch(RuntimeException re){
                        System.out.println("Error: " + re.getMessage());
                    }
                    if (poppedNode != null) {
                        myLinkedList.addScore(poppedNode.getParticipantID(), poppedNode.getScore());
                    }
                }
            }
        }
    }
}