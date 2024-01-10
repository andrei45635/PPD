package org.example.parallel;

import org.example.list.MyLinkedList;
import org.example.list.Node;
import org.example.list.ProcessedList;
import org.example.parallel.queue.MyQueue;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Parallel {
    private final int producerCount;
    private final int consumerCount;
    private AtomicBoolean stillMoreWork = new AtomicBoolean(true);
    MyLinkedList myLinkedList = new MyLinkedList(true);
    MyQueue<Node> myQueue = new MyQueue<>(true, 100);
    private ConcurrentHashMap<Integer, String> disqualified = new ConcurrentHashMap<>();
    private ProcessedList processedList = new ProcessedList();
    private final ExecutorService service;

    public Parallel(int producerCount, int consumerCount) {
        this.producerCount = producerCount;
        this.consumerCount = consumerCount;
        this.service = Executors.newFixedThreadPool(producerCount);
    }

    public void parallel() {
        List<Thread> threads = new ArrayList<>();
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();

        for(int i = 1; i <= producerCount; i++) {
            Producer producer = new Producer(myQueue, i, producerCount);
            service.execute(producer);
            producers.add(producer);
        }

        for(int i = 1; i <= consumerCount; i++) {
            Consumer consumer = new Consumer(myLinkedList, myQueue, i, stillMoreWork);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            threads.add(consumerThread);
            consumers.add(consumer);
        }

        producers.forEach(Producer::stop);
        consumers.forEach(Consumer::stop);
        for(Thread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        disqualified.forEach((key, value) -> myLinkedList.addNode(key, -1, value));
        myLinkedList.sort();
    }

    public void write(String filePath) {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))){
            bufferedWriter.write(myLinkedList.toString());
            System.out.println(disqualified.toString());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
