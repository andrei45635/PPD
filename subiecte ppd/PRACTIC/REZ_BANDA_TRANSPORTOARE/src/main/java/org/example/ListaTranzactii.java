package org.example;

public class ListaTranzactii {
    private Nod radacina;
    private final Integer nrProducatori, nrConsumatori;
    private Integer finishedRobots;

    public ListaTranzactii(Integer nrProducatori, Integer nrConsumatori) {
        this.nrProducatori = nrProducatori;
        this.nrConsumatori = nrConsumatori;
        this.finishedRobots = 0;
    }

    public synchronized void afiseazaTranzactii() {
        System.out.println("AFISARE");
        Nod tranzactie = this.radacina;
        while(tranzactie != null) {
            System.out.println(tranzactie);
            tranzactie = tranzactie.getNextTranzactie();
        }
        System.out.println();
    }

    public synchronized void addTranzactie(Nod nod) {
        nod.setNextTranzactie(this.radacina);
        this.radacina = nod;
    }

    public synchronized void setFinishedRobots() {
        this.finishedRobots++;
    }

    public synchronized boolean robotsHaveFinished() {
        // System.out.println(finishedRobots);
        return this.nrProducatori + this.nrConsumatori == this.finishedRobots;
    }
}
