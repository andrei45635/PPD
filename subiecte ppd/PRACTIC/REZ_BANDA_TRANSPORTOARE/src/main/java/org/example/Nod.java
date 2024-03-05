package org.example;

public class Nod {
    private Integer threadId;
    private String tipOperatie;
    private Integer nrObiecte;
    private Nod nextTranzactie;

    public Nod() {
    }

    public Nod(Integer threadId, String tipOperatie, Integer nrObiecte) {
        this.threadId = threadId;
        this.tipOperatie = tipOperatie;
        this.nrObiecte = nrObiecte;
    }

    public Integer getThreadId() {
        return threadId;
    }

    public void setThreadId(Integer threadId) {
        this.threadId = threadId;
    }

    public String getTipOperatie() {
        return tipOperatie;
    }

    public void setTipOperatie(String tipOperatie) {
        this.tipOperatie = tipOperatie;
    }

    public Integer getNrObiecte() {
        return nrObiecte;
    }

    public void setNrObiecte(Integer nrObiecte) {
        this.nrObiecte = nrObiecte;
    }

    public Nod getNextTranzactie() {
        return nextTranzactie;
    }

    public void setNextTranzactie(Nod nextTranzactie) {
        this.nextTranzactie = nextTranzactie;
    }

    @Override
    public String toString() {
        return "Nod{" +
                "threadId=" + threadId +
                ", tipOperatie='" + tipOperatie + '\'' +
                ", nrObiecte=" + nrObiecte +
                '}';
    }
}
