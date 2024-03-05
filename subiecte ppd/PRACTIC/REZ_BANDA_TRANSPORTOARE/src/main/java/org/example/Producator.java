package org.example;

public class Producator extends Thread{
    private static final int noIterations = 100;
    private final int threadId;
    private final Banda banda;
    private final ListaTranzactii listaTranzactii;

    public Producator(int threadId, Banda banda, ListaTranzactii listaTranzactii) {
        this.threadId = threadId;
        this.banda = banda;
        this.listaTranzactii = listaTranzactii;
    }
    @Override
    public void run() {
        for(int i = 0; i < 100; i++) {
            try {
                int nrObiecte = banda.puneObiecte();
                if(nrObiecte == -1) {
                    break;
                }
                Nod nod = new Nod(threadId, "preia", nrObiecte);
                listaTranzactii.addTranzactie(nod);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            try {
                sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Done producator");
        banda.setFinishedProducers();
        listaTranzactii.setFinishedRobots();
    }
}
