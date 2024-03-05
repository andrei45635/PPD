package org.example;

public class Main {
    private static final int nrThreads = 3;
    public static void main(String[] args) throws InterruptedException {
        ContBancar contBancar = new ContBancar();
        ListaTranzactii listaTranzactii = new ListaTranzactii(3);

        MyThread[] threads = new MyThread[nrThreads];
        for(int i = 1; i <= nrThreads; i++) {
            threads[i-1] = new MyThread(i, contBancar, listaTranzactii);
            threads[i-1].start();
        }
        DisplayThread displayThread = new DisplayThread(listaTranzactii, contBancar);
        displayThread.start();

        for(int i = 0; i < nrThreads; i++) {
            threads[i].join();
        }
        displayThread.join();
    }
}