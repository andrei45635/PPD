package org.example;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.example.list.ConcurrentLinkedList;
import org.example.parallel.Consumer;
import org.example.parallel.Producer;
import org.example.queue.MyQueue;
import org.example.list.Node;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private ServerSocket serverSocket;
    private final int port;
    private final int workers = 4;
    private final int deltaT = 1;
    private static final int readers = 4;
    private static final MyQueue<Node> myQueue = new MyQueue<>(true, 100);
    private static final ConcurrentLinkedList concurrentLinkedList = new ConcurrentLinkedList();
    private static final ExecutorService service = Executors.newFixedThreadPool(readers);
    public String output = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\proiect1\\src\\main\\resources\\results.txt";
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Server(int port) {
        this.port = port;
    }

    public void receiveData() {
        ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection conn = null;
        Session session = null;
        try {
            conn = cf.createConnection();
            conn.start();
            session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = new ActiveMQQueue("test.queue");
            MessageConsumer consumer = session.createConsumer(destination);
            while (true) {
                Message message = consumer.receive();
                if (message instanceof TextMessage) {
                    TextMessage textMessage = (TextMessage) message;
                    System.out.println("Received: " + textMessage.getText());
                } else {
                    break;
                }
            }
        } catch (JMSException e) {
            System.out.println("Error when receiving message in Server main thread");
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (JMSException e) {
                System.out.println("Error when closing the session and/or connection in Server main thread");
            }
        }
    }

    public void start() {
        System.out.println("Starting server...");
        try {
            serverSocket = new ServerSocket(this.port);
            while (true) {
                try {
                    System.out.println("Waiting for clients...");
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected...");
                    new Thread( () -> {
                       //receiveData();
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            System.out.println("Error closing client socket in start method of server");
                        }
                    }).start();
                } catch (IOException e) {
                    System.out.println("Error when creating connection with client in start method in Server");
                }
            }
        } catch (IOException e) {
            System.out.println("Error creating ServerSocket in the start method in the Server");
            try {
                stop();
            } catch (IOException ex) {
                System.out.println("Error when stopping in start method in Server");
            }
        }
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }

    public static void write(String filePath) {
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))){
            bufferedWriter.write(concurrentLinkedList.printList());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(1488);
        List<Thread> threads = new ArrayList<>();
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();

        System.out.println(myQueue.size());
        for(int i = 1; i <= readers; i++) {
            Producer producer = new Producer(myQueue);
            service.execute(producer);
            producers.add(producer);
        }

        for(int i = 1; i <= server.workers; i++) {
            Consumer consumer = new Consumer(concurrentLinkedList, myQueue);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            threads.add(consumerThread);
            consumers.add(consumer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            producers.forEach(Producer::stop);
            consumers.forEach(Consumer::stop);
            for(Thread thread: threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        concurrentLinkedList.sort();
        System.out.println(concurrentLinkedList.printList());
        write(server.output);
        server.start();
    }
}
