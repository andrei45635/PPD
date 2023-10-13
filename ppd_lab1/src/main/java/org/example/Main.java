package org.example;

import java.util.Arrays;
import java.util.Random;

public class Main {
    public static class MyThread extends Thread {
        private final int id;
        private final int start;
        private final int end;
        private final int[] A;
        private final int[] B;
        private final int[] C;

        public MyThread(int id, int start, int end, int[] A, int[] B, int[] C) {
            this.id = id;
            this.start = start;
            this.end = end;
            this.A = A;
            this.B = B;
            this.C = C;
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                C[i] = A[i] + B[i];
            }
        }
    }

    public static class MyThreadTwo extends Thread {
        private final int id;
        private final int step;
        private final int[] A;
        private final int[] B;
        private final int[] C;

        public MyThreadTwo(int id, int step, int[] A, int[] B, int[] C) {
            this.id = id;
            this.step = step;
            this.A = A;
            this.B = B;
            this.C = C;
        }

        @Override
        public void run() {
            for (int i = id; i < A.length; i += step) {
                C[i] = A[i] + B[i];
            }
        }
    }

    public static void main(String[] args) {
        Random rand = new Random();
        int N = 10;
        int L = 5;

        int[] A = new int[N];
        int[] B = new int[N];
        int[] C = new int[N];

        for (int i = 0; i < A.length; i++) {
            A[i] = rand.nextInt(L) + 1;
            B[i] = rand.nextInt(L) + 1;
            C[i] = 0;
        }

        long startSeq = System.currentTimeMillis();

        for (int i = 0; i < N; i++) {
            C[i] = A[i] + B[i];
        }

        System.out.println("Sequential: " + Arrays.toString(C));

        long endSeq = System.currentTimeMillis();
        System.out.println("Sequential time in ms: " + (double) (endSeq - startSeq));

        int THREAD_COUNT = Runtime.getRuntime().availableProcessors() / 2;
        int start = 0, end;
        int cat = N / THREAD_COUNT;
        int rest = N % THREAD_COUNT;

        long startParallel = System.currentTimeMillis();
        MyThread[] threads = new MyThread[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            end = start + cat;
            if (rest > 0) {
                end++;
                rest--;
            }
            threads[i] = new MyThread(i, start, end, A, B, C);
            threads[i].start();
            start = end;
        }


        System.out.println("Parallel: " + Arrays.toString(A));
        System.out.println("Parallel: " + Arrays.toString(B));
        System.out.println("Parallel: " + Arrays.toString(C));


        for (int i = 0; i < THREAD_COUNT; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException ie) {
                System.out.println("Thread " + threads[i].getName() + " was interrupted");
            }
        }
        System.out.println("Time needed for start and end: " + ((System.nanoTime() - startParallel) * 1.0) / 1000000000 + " s");

        long startParallel2 = System.currentTimeMillis();
        MyThreadTwo[] threadTwos = new MyThreadTwo[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            threadTwos[i] = new MyThreadTwo(i, THREAD_COUNT, A, B, C);
            threadTwos[i].start();
        }

        System.out.println("Parallel 2: " + Arrays.toString(A));
        System.out.println("Parallel 2: " + Arrays.toString(B));
        System.out.println("Parallel 2: " + Arrays.toString(C));

        for (int i = 0; i < THREAD_COUNT; i++) {
            try {
                threadTwos[i].join();
            } catch (InterruptedException ie) {
                System.out.println("Thread " + threadTwos[i].getName() + " was interrupted");
            }
        }
        System.out.println("Time needed for step traversal: " + ((System.nanoTime() - startParallel2) * 1.0) / 1000000000 + " s");
    }
}