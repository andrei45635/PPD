package org.example.parallel;

import org.example.list.Node;
import org.example.parallel.queue.MyQueue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Producer implements Runnable {
    private final MyQueue<Node> myQueue;
    private boolean running = false;
    private final Object lockObj = new Object();
    private final int pid;
    private final int pr;

    public Producer(MyQueue<Node> myQueue, int pid, int pr) {
        this.myQueue = myQueue;
        this.pid = pid;
        this.pr = pr;
    }

    @Override
    public void run() {
        running = true;
        try {
            produce(pid, pr);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
        myQueue.notifyNoMoreWork();
    }

    public void produceParticipant(String data){
        while(running){
            if(myQueue.isFull()){
                try {
                    myQueue.waitIsNotFull();
                } catch (InterruptedException ie) {
                    System.out.println("Error while waiting to produce messages.");
                    break;
                }
            }

            if(!running) {
                break;
            }

            String[] parts = data.split(" ");
            String country = parts[0];
            int participantID = Integer.parseInt(parts[1]);
            int score = Integer.parseInt(parts[2]);
            Node node = new Node(participantID, score, country);
            myQueue.enqueue(node);
        }
    }

    public void produce(int pid, int pr) throws InterruptedException {
        while (running) {
            if (myQueue.isFull()) {
                try {
                    myQueue.waitIsNotFull();
                } catch (InterruptedException ie) {
                    System.out.println("Error while waiting to produce messages.");
                    break;
                }
            }

            if (!running) {
                break;
            }

            int filesPerProducer = 50 / pr;
            int remainingFiles = 50 % pr;

            int startIndex = pid * filesPerProducer;
            if (remainingFiles <= pid) {
                startIndex += remainingFiles;
            } else {
                startIndex++;
            }
            int endIndex = (pid + 1) * filesPerProducer;
            endIndex += Math.min(remainingFiles, pid + 1);

            for (int i = startIndex; i < endIndex; i++) {
                int country = (i / 10) % 5 + 1;
                int problem = i % 10 + 1;
                String fileName = String.format("ppd_lab4_tema_2/src/main/resources/data/C%d_P%d.txt", country, problem);
                try (BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] parts = line.split(" ");
                        int participantID = Integer.parseInt(parts[0]);
                        int score = Integer.parseInt(parts[1]);
                        String cnt = "C" + country;
                        Node node = new Node(participantID, score, cnt);
                        myQueue.enqueue(node);
                    }
                } catch (IOException e) {
                    System.out.println("Error in produce method in Producer when reading from file: " + e.getMessage());
                    break;
                }
            }
        }
    }
}
