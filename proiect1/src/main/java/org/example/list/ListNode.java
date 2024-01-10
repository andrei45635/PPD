package org.example.list;

import java.util.concurrent.locks.ReentrantReadWriteLock;

//Credit: Alex Florian - gr.233/2
public class ListNode {
    private int id;
    private int score;
    private String country;
    private ListNode next;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ListNode(){
        this.id = -1;
        this.score = -1;
        this.country = "";
        this.next = null;
    }

    public ListNode(int id, int score, String country){
        this.id = id;
        this.score = score;
        this.country = country;
        this.next = null;
    }

    public void setNext(ListNode next) {
        lock.writeLock().lock();
        this.next = next;
        lock.writeLock().unlock();
    }

    public ListNode getNext(){
        lock.readLock().lock();
        try{
            return this.next;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public int getId(){
        lock.readLock().lock();
        try{
            return this.id;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void setScore(int score){
        lock.writeLock().lock();
        this.score = score;
        lock.writeLock().unlock();
    }

    public int getScore(){
        lock.readLock().lock();
        try{
            return this.score;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public String getCountry(){
        lock.readLock().lock();
        try{
            return this.country;
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public void appendNode(ListNode newNode){
        lock.writeLock().lock();
        ListNode nextNode = this.next;
        this.next = newNode;
        newNode.setNext(nextNode);
        lock.writeLock().unlock();
    }
}
