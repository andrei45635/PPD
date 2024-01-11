package org.example.list;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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
        disqualifiedLock.writeLock().unlock();
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

    public void sort() {
        try {
            globalLock.lock();

            List<ListNode> tempList = new ArrayList<>();
            ListNode currentNode = this.start.getNext();
            while (currentNode != null && currentNode != this.end) {
                tempList.add(currentNode);
                currentNode = currentNode.getNext();
            }

            tempList.sort(Comparator.comparingInt(ListNode::getScore).reversed());

            ListNode prevNode = this.start;
            for (ListNode node : tempList) {
                prevNode.setNext(node);
                prevNode = node;
            }
            prevNode.setNext(this.end);

        } finally {
            globalLock.unlock();
        }
    }

    public String printList() {
        Gson gson = new Gson();
        ListNode currentNode = start.getNext();
        StringBuilder builder = new StringBuilder();
        sort();
        while (currentNode != null && currentNode != end) {
            //System.out.println("ID: " + currentNode.getId() + ", Score: " + currentNode.getScore() + ", Country: " + currentNode.getCountry() + "\n");
            builder.append("ID: ").append(currentNode.getId()).append(", Score: ").append(currentNode.getScore()).append(", Country: ").append(currentNode.getCountry()).append("\n");
            currentNode = currentNode.getNext();
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\proiect1\\server\\src\\main\\resources\\results.txt", true))) {
            bufferedWriter.write(builder.toString());
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("C:\\Users\\GIGABYTE\\IdeaProjects\\PPD\\proiect1\\server\\src\\main\\resources\\country_leaderboard.txt", true))) {
            bufferedWriter.write(gson.toJson(calculateLeaderboard()));
        } catch (IOException e) {
            System.out.println("Error when writing: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return builder.toString();
    }
}
