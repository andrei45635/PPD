package org.example.parallel;

import org.example.list.Node;
import org.example.parallel.queue.MyQueue;
import org.example.list.MyLinkedList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Parallel {
    private final int producerCount;
    private final int consumerCount;
    private AtomicBoolean stillMoreWork = new AtomicBoolean(true);
    MyLinkedList myLinkedList = new MyLinkedList(true);
    MyQueue<Node> myQueue = new MyQueue<>(true);

    public Parallel(int producerCount, int consumerCount) {
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
    }

    public void parallel() {
        List<Thread> threads = new ArrayList<>();
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();
        for(int i = 1; i <= producerCount; i++) {
            Producer producer = new Producer(myQueue, producerCount, i);
            Thread producerThread = new Thread(producer);
            producerThread.start();
            threads.add(producerThread);
            producers.add(producer);
        }
        for(int i = 1; i <= consumerCount; i++) {
            Consumer consumer = new Consumer(myLinkedList, myQueue, i, stillMoreWork);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            threads.add(consumerThread);
            consumers.add(consumer);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        producers.forEach(Producer::stop);
        stillMoreWork.set(false);
        consumers.forEach(Consumer::stop);
        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String filePath) {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))){
            bufferedWriter.write(myLinkedList.toString());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
