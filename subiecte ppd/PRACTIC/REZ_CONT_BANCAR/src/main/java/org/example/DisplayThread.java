package org.example;

public class DisplayThread extends Thread{
    private final ListaTranzactii listaTranzactii;
    private final ContBancar contBancar;

    public DisplayThread(ListaTranzactii listaTranzactii, ContBancar contBancar) {
        this.listaTranzactii = listaTranzactii;
        this.contBancar = contBancar;
    }
    @Override
    public void run() {
        while(true) {
            if(listaTranzactii.allThreadsFinished()) {
                System.out.println("RON: " + contBancar.getRon());
                System.out.println("EUR: " + contBancar.getEuro());
                break;
            }
            listaTranzactii.afiseazaListaTranzactii();
        }
    }
}
