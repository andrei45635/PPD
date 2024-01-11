package org.example.list;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//Credit: Alex Florian - gr.233/2
public class ConcurrentLinkedList {
    private final ListNode start;
    private final ListNode end;
    private final List<Integer> disqualifiedIDs = new ArrayList<>();
    private final ReentrantReadWriteLock disqualifiedLock = new ReentrantReadWriteLock();
    private final ReentrantLock globalLock = new ReentrantLock();
    private final int nodeIDMutexArraySize = 200;
    private final ReentrantLock[] nodeIDLockArray = new ReentrantLock[nodeIDMutexArraySize];

    private boolean isDisqualified(int idToCheck) {
        try {
            disqualifiedLock.readLock().lock();
            for (Integer id : disqualifiedIDs) {
                if (id == idToCheck) {
                    return true;
                }
            }
            return false;
        } finally {
            disqualifiedLock.readLock().unlock();
        }
    }

    private void disqualify(int idToDisqualify) {
        disqualifiedLock.writeLock().lock();
        disqualifiedIDs.add(idToDisqualify);
        disqualifiedLock.writeLock().lock();
    }

    public ConcurrentLinkedList() {
        this.start = new ListNode();
        this.end = new ListNode();
        start.setNext(end);
        for (int i = 0; i < nodeIDMutexArraySize; i++) {
            nodeIDLockArray[i] = new ReentrantLock();
        }
    }

    public void updateList(int id, int score, String country) {
        try {
            nodeIDLockArray[id % nodeIDMutexArraySize].lock();

            boolean disqualified = score < 0;
            ListNode previousNode = this.start;
            ListNode currentNode = this.start.getNext();

            if (isDisqualified(id)) {
                return;
            }

            if (disqualified) {
                disqualify(id);
            }

            while (currentNode != null) {
                if (currentNode.getId() == id) {
                    if (disqualified) {
                        previousNode.setNext(currentNode.getNext());
                    } else {
                        currentNode.setScore(currentNode.getScore() + score);
                    }
                    return;
                }
                previousNode = currentNode;
                currentNode = currentNode.getNext();
            }

            if (!disqualified) {
                ListNode newNode = new ListNode(id, score, country);
                start.appendNode(newNode);
            }

        } finally {
            nodeIDLockArray[id % nodeIDMutexArraySize].unlock();
        }
    }

    public ConcurrentHashMap<String, Integer> calculateLeaderboard() {
        ConcurrentHashMap<String, Integer> countryScores = new ConcurrentHashMap<>();
        globalLock.lock();
        try {
            ListNode currentNode = start.getNext();
            while (currentNode != null && currentNode != end) {
                countryScores.put(currentNode.getCountry(), countryScores.getOrDefault(currentNode.getCountry(), 0) + currentNode.getScore());
                currentNode = currentNode.getNext();
            }
        } finally {
            globalLock.unlock();
        }

        return sortCountryScores(countryScores);
    }

    private ConcurrentHashMap<String, Integer> sortCountryScores(ConcurrentHashMap<String, Integer> countryScores) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(countryScores.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        ConcurrentHashMap<String, Integer> sortedMap = new ConcurrentHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    //ChatGPT code as placeholder code for sorting, was too tired to finish this
    public void sort() {
        // Acquire a global lock or locks on all nodes
        try {
            // Assuming a globalLock for the entire list
            globalLock.lock();

            // Copy data to a temporary list for sorting
            List<ListNode> tempList = new ArrayList<>();
            ListNode currentNode = this.start.getNext();
            while (currentNode != null && currentNode != this.end) {
                tempList.add(currentNode);
                currentNode = currentNode.getNext();
            }

            // Sort the temporary list
            tempList.sort(Comparator.comparingInt(ListNode::getScore));

            // Rebuild the linked list with sorted elements
            ListNode prevNode = this.start;
            for (ListNode node : tempList) {
                prevNode.setNext(node);
                prevNode = node;
            }
            prevNode.setNext(this.end);

        } finally {
            // Release the global lock
            globalLock.unlock();
        }
    }

    public String printList() {
        ListNode currentNode = start.getNext();
        StringBuilder builder = new StringBuilder();
        while (currentNode != null && currentNode != end) {
            System.out.println("ID: " + currentNode.getId() + ", Score: " + currentNode.getScore() + ", Country: " + currentNode.getCountry());
            builder.append("ID: ").append(currentNode.getId()).append(", Score: ").append(currentNode.getScore()).append(", Country: ").append(currentNode.getCountry());
            currentNode = currentNode.getNext();
        }
        return builder.toString();
    }
}
