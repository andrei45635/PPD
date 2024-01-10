package org.example.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ProcessedList {
    public static class Pair {
        private int id;
        private String country;

        public Pair(int id, String country) {
            this.id = id;
            this.country = country;
        }

        public int getId() {
            return id;
        }

        public String getCountry() {
            return country;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return id == pair.id && Objects.equals(country, pair.country);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, country);
        }
    }

    private List<Pair> processedList = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    public boolean contains(Pair pair) {
        lock.lock();
        boolean res = processedList.contains(pair);
        lock.unlock();
        return res;
    }

    public void add(Pair pair) {
        lock.lock();
        processedList.add(pair);
        lock.unlock();
    }

    public List<Pair> getProcessedList() {
        return processedList;
    }
}
