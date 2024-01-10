package org.example;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
    private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
    private static final AtomicBoolean allProblemsProcessed = new AtomicBoolean(false);
    private static int lineCount = 0;
    private static BufferedReader currentFileReader = null;
    private static final int problemsPerCountry = 10;
    private static final int linesPerBatch = 20;
    private static int problemIndex = 0;
    private final int countryId;
    private static Connection connection;
    private static Session session;
    private static MessageProducer producer;
    private final int port;
    private final String host;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Client(int countryId, String host, int port) {
        this.countryId = countryId;
        this.host = host;
        this.port = port;
        initializeActiveMQ();
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client(Integer.parseInt(args[0]), "127.0.0.1", 1488);
        client.run();
    }

    private void initializeActiveMQ() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("test.queue");

            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException e) {
            System.out.println("Error initializing ActiveMQ connection: " + e.getMessage());
        }
    }

    public void run() throws IOException {
        clientSocket = new Socket(this.host, this.port);
        System.out.println("Successfully connected to server");
        int deltaX = 2000;
        service.scheduleAtFixedRate(this::sendData, 0, deltaX, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        try {
            if (currentFileReader != null) {
                currentFileReader.close();
            }
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (IOException | JMSException e) {
            System.out.println("Error closing file reader: " + e.getMessage());
        }
        System.out.println("Finished sending all the participants");
        service.shutdown();
        try {
            in.close();
        } catch (IOException e) {
            System.out.println("Error when closing InputStream in the stop method in the Client");
        }
        out.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.out.println("Error when closing the client's Server Socket in the stop method in the Client");
        }
    }

    public void sendToQueue(String data) throws JMSException {
        try {
            TextMessage message = session.createTextMessage(data);
            producer.send(message);
            System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
        } catch (JMSException e) {
            System.out.println("Error sending message in connectToActiveMQ method: " + e.getMessage());
        }
    }

    public void sendData() {
        try {
            while (problemIndex < problemsPerCountry) {
                if (currentFileReader == null) {
                    String fileName = String.format("proiect1/src/main/resources/data/C%d_P%d.txt", countryId, problemIndex + 1);
                    currentFileReader = new BufferedReader(new FileReader(fileName));
                }

                String line;
                while (lineCount < linesPerBatch && (line = currentFileReader.readLine()) != null) {
                    String[] parts = line.split(" ");
                    int participantID = Integer.parseInt(parts[0]);
                    int score = Integer.parseInt(parts[1]);
                    String cnt = "C" + countryId;
                    String message = cnt + " " + participantID + " " + score;
                    System.out.println(message);
                    sendToQueue(message);
                    lineCount++;
                }

                if (lineCount >= linesPerBatch) {
                    System.out.printf("Country %d sent 20 participants from problem %d...", countryId, problemIndex + 1);
                    lineCount = 0;
                    return;
                }

                //EOF, move to the next problem
                currentFileReader.close();
                currentFileReader = null;
                problemIndex++;
            }
        } catch (IOException | JMSException e) {
            System.out.println("Error when sending data in sendData method from Client: " + e.getMessage());
        }

        if (problemIndex >= problemsPerCountry) {
            allProblemsProcessed.set(true);
            stop();
        }
    }
}
