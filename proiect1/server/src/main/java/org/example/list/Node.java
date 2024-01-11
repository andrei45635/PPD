package org.example.list;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Node {
    public final Lock lock = new ReentrantLock();
    private int participantID;
    private int score;
    private String country;
    private Node next;
    private Node previous;

    public Node() {}

    public Node(int participantID, int score, String country) {
        this.participantID = participantID;
        this.score = score;
        this.country = country;
        this.next = null;
        this.previous = null;
    }

    public Node(int participantID, int score, String country, Node next, Node previous) {
        this.participantID = participantID;
        this.score = score;
        this.next = next;
        this.previous = previous;
        this.country = country;
    }

    public void lock(){
        lock.lock();
    }

    public void unlock(){
        lock.unlock();
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

    public void setScore(int score) {
        this.score = score;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getPrevious() {
        return previous;
    }

    public void setPrevious(Node previous) {
        this.previous = previous;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return participantID == node.participantID && score == node.score && Objects.equals(lock, node.lock) && Objects.equals(country, node.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lock, participantID, score, country);
    }

    @Override
    public String toString() {
        return "Node{" +
                "lock=" + lock +
                ", participantID=" + participantID +
                ", score=" + score +
                ", country='" + country + '\'' +
                '}';
    }
}