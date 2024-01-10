package org.example.parallel.queue;

import org.example.list.Node;

import java.util.Objects;

public class QueueNode {
    private Node node;
    private QueueNode next;

    public QueueNode(Node node, QueueNode next) {
        this.node = node;
        this.next = next;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public QueueNode getNext() {
        return next;
    }

    public void setNext(QueueNode next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueueNode queueNode = (QueueNode) o;
        return Objects.equals(node, queueNode.node) && Objects.equals(next, queueNode.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, next);
    }

    @Override
    public String toString() {
        return "QueueNode{" +
                "node=" + node +
                ", next=" + next +
                '}';
    }
}
