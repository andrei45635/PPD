package org.example;

import java.util.Random;

public class MyThread extends Thread{
    private int threadId;
    private final ContBancar contBancar;
    private final ListaTranzactii listaTranzactii;
    private final Random random = new Random();

    public MyThread(int threadId, ContBancar contBancar, ListaTranzactii listaTranzactii) {
        this.threadId = threadId;
        this.contBancar = contBancar;
        this.listaTranzactii = listaTranzactii;
    }

    private Nod generateRandomNod() {
        Nod nod = new Nod();
        nod.setUserIndex(threadId);
        int nr = random.nextInt(0, 2);
        String valuta = "RON";
        if(nr == 1) {
            valuta = "EUR";
        }
        nod.setTipValuta(valuta);
        nr = random.nextInt(0, 2);
        String tipTranzactie = "depunere";
        if(nr == 1) {
            tipTranzactie = "retragere";
        }
        nod.setTipTranzactie(tipTranzactie);
        int suma = random.nextInt(1, 1001);
        nod.setSumaTranzactionata(suma);
        return nod;
    }
    @Override
    public void run() {
        for(int i = 0; i < 20; i++) {
            Nod tranzactieRandom = generateRandomNod();
            if(tranzactieRandom.getTipTranzactie().equals("depunere")) {
                int soldCurent;
                if(tranzactieRandom.getTipValuta().equals("RON")) {
                    soldCurent = contBancar.depuneLei(tranzactieRandom.getSumaTranzactionata());
                }
                else {
                    soldCurent = contBancar.depuneEuro(tranzactieRandom.getSumaTranzactionata());
                }
                tranzactieRandom.setSoldCurent(soldCurent);
                try {
                    listaTranzactii.adaugaTranzactie(tranzactieRandom);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
            else {
                int soldCurent;
                if(tranzactieRandom.getTipValuta().equals("RON")) {
                    soldCurent = contBancar.retrageLei(tranzactieRandom.getSumaTranzactionata());
                }
                else {
                    soldCurent = contBancar.retrageEuro(tranzactieRandom.getSumaTranzactionata());
                }
                if(soldCurent >= 0) {
                    tranzactieRandom.setSoldCurent(soldCurent);
                    try {
                        listaTranzactii.adaugaTranzactie(tranzactieRandom);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            try {
                sleep(10L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        listaTranzactii.setFinishedThread();
    }
}
