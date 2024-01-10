package org.example.list;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;

public class MyLinkedList {
    private LinkedList<Node> participantsList;
    private Node head = new Node(0, 0, "", null, null);
    private Node tail = new Node(0, 0, "", null, null);
    private Set<Integer> disqualifiedParticipants = new HashSet<>();
    private boolean isParallel;
    private final Object lockObject = new Object();

    public MyLinkedList(boolean isParallel) {
        this.participantsList = new LinkedList<>();
        this.head.setNext(tail);
        this.tail.setPrevious(this.head);
        this.isParallel = isParallel;
    }

    public synchronized Node getHead() {
        return head;
    }

    public void addNode(int participantID, int score, String country) {
        head.lock();
        try {
            Node nextNode = head.getNext();
            nextNode.lock();
            try {
                Node node = new Node(participantID, score, country, null, null);
                node.setNext(nextNode);
                node.setPrevious(head);
                head.setNext(node);
                nextNode.setPrevious(node);
            } finally {
                nextNode.unlock();
            }
        } finally {
            head.unlock();
        }
    }

    public Node updateNode(int participantID, int score, String country) {
        Node target = head.getNext();
        while (target != tail) {
            target.lock();
            if (target.getParticipantID() == participantID && target.getScore() == score && Objects.equals(target.getCountry(), country)) {
                target.setScore(target.getScore() + score);
                target.unlock();
                return target;
            }
            target.unlock();
            target = target.getNext();
        }
        return null;
    }

    public void deleteNode(int participantID) {
        head.lock();
        head.getNext().lock();
        if (head.getNext() == tail) {
            head.unlock();
            head.getNext().unlock();
            return;
        }
        Node target = head.getNext();
        while (target != tail) {
            target.getNext().lock();
            if (target.getParticipantID() == participantID) {
                Node before = target.getPrevious();
                Node after = target.getNext();
                before.setNext(after);
                after.setPrevious(before);
                before.unlock();
                target.unlock();
                after.unlock();
                return;
            }
            target.getPrevious().unlock();
            target = target.getNext();
        }
        target.getPrevious().unlock();
        target.unlock();
    }

    public void sort() {
        head.setNext(mergeSort(head.getNext()));
    }

    private Node mergeSort(Node head) {
        if (head == null || head.getNext() == tail) {
            return head;
        }

        Node middle = getMiddle(head);
        Node nextOfMiddle = middle.getNext();

        middle.setNext(tail);

        Node left = mergeSort(head);
        Node right = mergeSort(nextOfMiddle);

        return sortedMerge(left, right);
    }

    private Node sortedMerge(Node a, Node b) {
        Node result;
        if (a == tail) {
            return b;
        }
        if (b == tail) {
            return a;
        }

        if (a.getScore() >= b.getScore()) {
            result = a;
            result.setNext(sortedMerge(a.getNext(), b));
        } else {
            result = b;
            result.setNext(sortedMerge(a, b.getNext()));
        }
        return result;
    }

    private Node getMiddle(Node head) {
        if (head == tail) {
            return head;
        }

        Node slow = head, fast = head;

        while (fast.getNext() != tail && fast.getNext().getNext() != tail) {
            slow = slow.getNext();
            fast = fast.getNext().getNext();
        }
        return slow;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Node n = this.getHead(); n != null; n = n.getNext()) {
            stringBuilder.append(n.getParticipantID()).append(" ").append(n.getScore()).append(" ").append(n.getCountry()).append("\n");
        }
        return stringBuilder.toString();
    }
}
