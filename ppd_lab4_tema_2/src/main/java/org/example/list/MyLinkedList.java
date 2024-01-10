package org.example.list;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class MyLinkedList {
    private LinkedList<Node> participantsList;
    private Node head;
    private Set<Integer> disqualifiedParticipants = new HashSet<>();
    private boolean isParallel;
    private final Object lockObject = new Object();

    public MyLinkedList(boolean isParallel) {
        this.participantsList = new LinkedList<>();
        this.head = null;
        this.isParallel = isParallel;
    }

    public synchronized Node getHead() {
        return head;
    }

    public synchronized void setHead(Node head) {
        this.head = head;
    }

    public synchronized void addNode(Node target) {
        if (this.getHead() == null) {
            this.setHead(target);
            return;
        }
        if (orderList(target, this.getHead())) {
            target.setNext(this.getHead());
            this.setHead(target);
            return;
        }
        Node node = this.getHead();
        while (node.getNext() != null) {
            if (orderList(node, target) && orderList(target, node.getNext())) {
                target.setNext(node.getNext());
                node.setNext(target);
                return;
            }
            node = node.getNext();
        }
        node.setNext(target);
    }

    public synchronized boolean orderList(Node node1, Node node2) {
        return node1.getScore() > node2.getScore() || (node1.getScore() == node2.getScore() && node1.getParticipantID() <= node2.getParticipantID());
    }

    public synchronized Node getOrCreateNode(int participantID) {
        Node node = this.getHead();
        Node prev = null;
        while (node != null) {
            if (node.getParticipantID() == participantID) {
                if (prev != null) {
                    prev.setNext(node.getNext());
                } else {
                    this.setHead(node.getNext());
                }
                node.setNext(null);
                return node;
            }
            prev = node;
            node = node.getNext();
        }
        return new Node(participantID, 0, null);
    }

    public void addScore(int participantID, int score) {
        synchronized (lockObject) {
            try {
                if (disqualifiedParticipants.contains(participantID)) {
                    return;
                }
                Node node = this.getOrCreateNode(participantID);
                if (score < 0) {
                    disqualifiedParticipants.add(participantID);
                    return;
                }
                node.setScore(node.getScore() + score);
                addNode(node);
            } catch (Exception e) {
                System.out.println("Error when adding a score: " + e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node n = this.getHead(); n != null; n = n.getNext()) {
            stringBuilder.append(n.getParticipantID()).append(" ").append(n.getScore()).append("\n");
        }
        stringBuilder.append("Disqualified:\n");
//        for (int d : disqualifiedParticipants) {
//            stringBuilder.append(d).append("\n");
//        }
        return stringBuilder.toString();
    }
}
