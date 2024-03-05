package org.example;

public class ContBancar {
    private Integer ron, euro;
    private final Object lockRon, lockEuro;

    public ContBancar() {
        this.ron = this.euro = 0;
        this.lockRon = new Object();
        this.lockEuro = new Object();
    }

    public int depuneLei(Integer lei) {
        synchronized (lockRon) {
            ron += lei;
            return this.ron;
        }
    }

    public int depuneEuro(Integer sumEuro) {
        synchronized (lockEuro) {
            this.euro += sumEuro;
            return this.euro;
        }
    }

    public int retrageLei(Integer sumRon) {
        synchronized (lockRon) {
            if(sumRon > ron) {
                return -1;
            }
            this.ron -= sumRon;
            return this.ron;
        }
    }

    public int retrageEuro(Integer sumEuro) {
        synchronized (lockEuro) {
               if(sumEuro > this.euro) {
                   return -1;
               }
               this.euro -= sumEuro;
               return this.euro;
        }
    }

    public int getRon() {
        return this.ron;
    }

    public int getEuro() {
        return this.euro;
    }
}
