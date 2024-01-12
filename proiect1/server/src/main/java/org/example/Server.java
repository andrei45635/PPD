package org.example;

import com.google.gson.Gson;
import org.example.list.ConcurrentLinkedList;
import org.example.list.Node;
import org.example.parallel.Consumer;
import org.example.parallel.Producer;
import org.example.queue.MyQueue;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {
    private ServerSocket serverSocket;
    private final int port;
    private final int workers = 4;
    private final long deltaT = 1;
    private volatile ConcurrentHashMap<String, Integer> lastResponse = null;
    private volatile long lastResponseTime = 0;
    private static final int readers = 4;
    private static AtomicBoolean stillMoreWork = new AtomicBoolean(true);
    private static CountDownLatch latch = new CountDownLatch(readers);
    private static final MyQueue<Node> myQueue = new MyQueue<>(true, 100);
    private static final ConcurrentLinkedList concurrentLinkedList = new ConcurrentLinkedList();
    private static final ExecutorService service = Executors.newFixedThreadPool(readers);
    private static List<ClientConnection> activeConnections = Collections.synchronizedList(new ArrayList<>());
    private ExecutorService leaderboardCalculationService = Executors.newCachedThreadPool();
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    public String resultsOutput = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\proiect1\\server\\src\\main\\resources\\results.txt";
    public String leaderboardOutput = "C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\proiect1\\server\\src\\main\\resources\\country_leaderboard.txt";

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        System.out.println("Starting server...");
        try {
            serverSocket = new ServerSocket(this.port);
            while (true) {
                try {
                    System.out.println("Waiting for clients...");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected...");
                    new Thread(() -> {
                        handleClient(clientSocket);
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

    private void sendResults(Socket clientSocket) throws IOException {
        byte[] byteData = Files.readAllBytes(Path.of(resultsOutput));
        try(DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            dos.writeInt(byteData.length);
            dos.write(byteData);
        } catch (SocketException e) {
            System.out.println(" ");
        }
        clientSocket.getOutputStream().flush();
    }

    private void sendLeaderboard(Socket clientSocket) throws IOException {
        byte[] byteData = Files.readAllBytes(Path.of(leaderboardOutput));
        try(DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {
            dos.writeInt(byteData.length);
            dos.write(byteData);
        }
        catch (SocketException e) {
            System.out.println(" ");
        }
    }

    private void handleClient(Socket clientSocket) {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            activeConnections.add(new ClientConnection(clientSocket, out, in));
            Gson gson = new Gson();
            String clientRequest;
            try {
                while ((clientRequest = in.readLine()) != null) {
                    System.out.println("Received request: " + clientRequest);
                    ConcurrentHashMap<String, Integer> response = new ConcurrentHashMap<>();
                    response.put("RESPONSE", 0);
                    long currentTime = System.currentTimeMillis();
                    Future<ConcurrentHashMap<String, Integer>> future;
                    synchronized (this) {
                        if (lastResponse != null && (currentTime - lastResponseTime) < deltaT) {
                            future = CompletableFuture.completedFuture(lastResponse);
                        } else {
                            future = leaderboardCalculationService.submit(concurrentLinkedList::calculateLeaderboard);
                            lastResponse = future.get();
                            lastResponseTime = currentTime;
                        }
                    }
                    //Serializes the response
                    response = future.get();
                    String jsonResponse = gson.toJson(response);
                    out.println(jsonResponse);
                    out.flush();
                    System.out.println("Sent JSON response: " + jsonResponse + " to client: " + clientSocket.getPort());
                    //Send announcement that the server finished the list
                    if (stillMoreWork.get()) {
                        System.out.println("Sending announcement that the server finished the list...");
                        out.println("DONE");
                        out.flush();

                        String clientResponse = in.readLine();
                        if ("ACK".equals(clientResponse)) {
                            System.out.println("Sending the final list to the client...");
                            String listData = concurrentLinkedList.printList();
                            String jsonList = gson.toJson(listData);
                            sendResults(clientSocket);
                            clientSocket.getOutputStream().flush();
                            //out.println(jsonList);
                            //out.flush();
                            in.read();
                            System.out.println("Sending the final leaderboard to the client...");
                            ConcurrentHashMap<String, Integer> leaderboardList = concurrentLinkedList.calculateLeaderboard();
                            String jsonLeaderboard = gson.toJson(leaderboardList);
                            sendLeaderboard(clientSocket);
                            //out.println(jsonLeaderboard);
                            //out.flush();
                        }
                    }
                }
            } catch (SocketException e){
                System.out.println(".");
            }
        } catch (IOException e) {
            System.out.println("Error when reading request from the Client " + clientSocket.getPort() + "  in the Server\nError message: " + e.getMessage());
            e.printStackTrace();
        } catch (ExecutionException | InterruptedException e) {
            System.out.println("Error in the future in the handleClient method in the server\nError message: " + e.getMessage() + "\nStack trace: ");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client connection: " + e.getMessage());
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
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))) {
            bufferedWriter.write(concurrentLinkedList.printList());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void writeLeaderboard(String filepath) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filepath, true))) {
            bufferedWriter.write(concurrentLinkedList.calculateLeaderboard().toString());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Server server = new Server(1489);
        new Thread(server::start).start();
        long startTime = System.nanoTime();
        List<Thread> threads = new ArrayList<>();
        List<Producer> producers = new ArrayList<>();
        List<Consumer> consumers = new ArrayList<>();

        for (int i = 1; i <= readers; i++) {
            Producer producer = new Producer(myQueue);
            service.execute(producer);
            producers.add(producer);
        }

        for (int i = 1; i <= server.workers; i++) {
            Consumer consumer = new Consumer(concurrentLinkedList, myQueue, stillMoreWork, latch);
            Thread consumerThread = new Thread(consumer);
            consumerThread.start();
            threads.add(consumerThread);
            consumers.add(consumer);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            producers.forEach(Producer::stop);
            stillMoreWork.set(false);
            consumers.forEach(Consumer::stop);
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        long endTime = System.nanoTime();
        //System.out.println((double) (endTime - startTime) / 1E6);
        concurrentLinkedList.sort();
        System.out.println("List: " + concurrentLinkedList.printList());
        write(server.resultsOutput);
        writeLeaderboard(server.leaderboardOutput);
    }

    private static class ClientConnection {
        Socket socket;
        PrintWriter out;
        BufferedReader in;

        ClientConnection(Socket socket, PrintWriter out, BufferedReader in) {
            this.socket = socket;
            this.out = out;
            this.in = in;
        }
    }
}
