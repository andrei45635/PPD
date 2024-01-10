package org.example.parallel.queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MyQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    //private final int maxSize = Integer.MAX_VALUE;
    private final Object IS_NOT_FULL = new Object();
    private final Object IS_NOT_EMPTY = new Object();
    private final Object lock = new Object();
    private boolean isParallel;
    private final AtomicBoolean noMoreWork = new AtomicBoolean(false);


    public MyQueue(boolean isParallel) {
        //this.maxSize = maxSize;
        this.isParallel = isParallel;
    }

    public void waitIsNotFull() throws InterruptedException {
        synchronized (IS_NOT_FULL) {
            IS_NOT_FULL.wait();
        }
    }

    public void notifyIsNotFull() {
        synchronized (IS_NOT_FULL) {
            IS_NOT_FULL.notify();
        }
    }

    public void waitIsNotEmpty() throws InterruptedException {
        synchronized (IS_NOT_EMPTY) {
            IS_NOT_EMPTY.wait();
        }
    }

    public void notifyIsNotEmpty() {
        synchronized (IS_NOT_EMPTY) {
            IS_NOT_EMPTY.notify();
        }
    }

    public void notifyNoMoreWork() {
        noMoreWork.set(true);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public synchronized void enqueue(T node) {
        queue.add(node);
        notifyIsNotEmpty();
    }

    public synchronized T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T node = queue.poll();
        notifyIsNotFull();
        return node;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized int size() {
        return queue.size();
    }
}