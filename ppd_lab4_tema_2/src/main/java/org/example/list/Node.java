package org.example.list;

import java.util.Objects;

public class Node {
    private int participantID;
    private int score;
    private Node next;

    public Node() {}

    public Node(int participantID, int score) {
        this.participantID = participantID;
        this.score = score;
        this.next = null;
    }

    public Node(int participantID, int score, Node next) {
        this.participantID = participantID;
        this.score = score;
        this.next = next;
    }

    public int getParticipantID() {
        return participantID;
    }

    public int getScore() {
        return score;
    }

    public Node getNext() {
        return next;
    }

    public void setParticipantID(int participantID) {
        this.participantID = participantID;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return participantID == node.participantID && score == node.score && Objects.equals(next, node.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(participantID, score, next);
    }

    @Override
    public String toString() {
        return "Node{" +
                "participantID=" + participantID +
                ", score=" + score +
                '}';
    }
}