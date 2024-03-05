package org.example;

public class Nod {
    private Integer userIndex;
    private String tipValuta, tipTranzactie;
    private Integer sumaTranzactionata, soldCurent;
    private Nod next;

    public Nod() {}

    public Nod(Integer userIndex, String tipValuta, String tipTranzactie, Integer sumaTranzactionata, Integer soldCurent, Nod next) {
        this.userIndex = userIndex;
        this.tipValuta = tipValuta;
        this.tipTranzactie = tipTranzactie;
        this.sumaTranzactionata = sumaTranzactionata;
        this.soldCurent = soldCurent;
        this.next = next;
    }

    public Integer getUserIndex() {
        return userIndex;
    }

    public void setUserIndex(Integer userIndex) {
        this.userIndex = userIndex;
    }

    public String getTipValuta() {
        return tipValuta;
    }

    public void setTipValuta(String tipValuta) {
        this.tipValuta = tipValuta;
    }

    public String getTipTranzactie() {
        return tipTranzactie;
    }

    public void setTipTranzactie(String tipTranzactie) {
        this.tipTranzactie = tipTranzactie;
    }

    public Integer getSumaTranzactionata() {
        return sumaTranzactionata;
    }

    public void setSumaTranzactionata(Integer sumaTranzactionata) {
        this.sumaTranzactionata = sumaTranzactionata;
    }

    public Integer getSoldCurent() {
        return soldCurent;
    }

    public void setSoldCurent(Integer soldCurent) {
        this.soldCurent = soldCurent;
    }

    public Nod getNext() {
        return next;
    }

    public void setNext(Nod next) {
        this.next = next;
    }

    @Override
    public String toString() {
        return "Nod{" +
                "userIndex=" + userIndex +
                ", tipValuta='" + tipValuta + '\'' +
                ", tipTranzactie='" + tipTranzactie + '\'' +
                ", sumaTranzactionata=" + sumaTranzactionata +
                ", soldCurent=" + soldCurent +
                '}';
    }
}
