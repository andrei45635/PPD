package org.example.parallel;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.example.list.Node;
import org.example.queue.MyQueue;

public class Producer implements Runnable {
    private final MyQueue<Node> myQueue;
    private boolean running = false;

    public Producer(MyQueue<Node> myQueue) {
        this.myQueue = myQueue;
    }

    @Override
    public void run() {
        running = true;
        try {
            produceParticipant();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        running = false;
        myQueue.notifyNoMoreWork();
    }

    public void produceParticipant() throws InterruptedException {
        ConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        try (Connection conn = cf.createConnection()) {
            conn.start();
            try (Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
                Destination destination = new ActiveMQQueue("test.queue");
                MessageConsumer consumer = session.createConsumer(destination);
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

                    Message message = consumer.receive(1000);
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        System.out.println("Received - Producer: " + textMessage.getText());
                        String[] parts = textMessage.getText().split(" ");
                        String cnt = parts[0];
                        int participantID = Integer.parseInt(parts[1]);
                        int score = Integer.parseInt(parts[2]);
                        Node node = new Node(participantID, score, cnt);
                        myQueue.enqueue(node);
                    }
                }
            }
        } catch (JMSException e) {
            System.out.println("Error when creating connection to ActiveMQ in produceParticipant method in Producer");
        }
    }
}
