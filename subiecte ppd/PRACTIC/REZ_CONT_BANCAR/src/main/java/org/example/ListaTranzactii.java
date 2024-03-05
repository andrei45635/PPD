package org.example;

public class ListaTranzactii {
    private Nod radacina;
    int numarTranzactii;
    int nrThreads;
    int nrFinishedThreads;
    boolean jobDone; // flag used by the thread which displays the list to tell the other threads when he finished his job

    public ListaTranzactii(int nrThreads) {
        this.radacina = null;
        this.numarTranzactii = 0;
        this.jobDone = false;
        this.nrThreads = nrThreads;
    }

    public synchronized void adaugaTranzactie(Nod tranzactie) throws InterruptedException {
        while(this.numarTranzactii % 5 == 0 && !jobDone) {
            wait();
        }
        tranzactie.setNext(this.radacina);
        this.radacina = tranzactie;
        this.numarTranzactii++;
        if(numarTranzactii % 5 == 0) {
            jobDone = false;
            notify();
        }
    }

    public synchronized void afiseazaListaTranzactii() {
        System.out.println("AFISARE");
        Nod tranzactie = radacina;
        while(tranzactie != null) {
            System.out.println(tranzactie);
            tranzactie = tranzactie.getNext();
        }
        jobDone = true;
        notifyAll();
        System.out.println();
    }

    public synchronized void setFinishedThread() {
        this.nrFinishedThreads++;
    }

    public synchronized boolean allThreadsFinished() {
        return this.nrFinishedThreads == this.nrThreads;
    }
}
