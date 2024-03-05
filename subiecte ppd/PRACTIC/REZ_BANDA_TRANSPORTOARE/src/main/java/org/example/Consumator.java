package org.example;

public class Consumator extends Thread{
    private static final int noIterations = 100;
    private final int threadId;
    private final Banda banda;
    private final ListaTranzactii listaTranzactii;

    public Consumator(int threadId, Banda banda, ListaTranzactii listaTranzactii) {
        this.threadId = threadId;
        this.banda = banda;
        this.listaTranzactii = listaTranzactii;
    }
    @Override
    public void run() {
        for(int i = 0; i < 100; i++) {
            try {
                int nrObiecte = banda.iaObiecte();
                if(nrObiecte == -1) {
                    break; //toti producatorii au terminat de pus obiecte, consumatorii ar tb sa termine si ei prematur
                }
                Nod nod = new Nod(threadId, "preia", nrObiecte);
                listaTranzactii.addTranzactie(nod);
                try {
                    sleep(8);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Done consumator");
        banda.setFinishedConsumers();
        listaTranzactii.setFinishedRobots();
    }
}
