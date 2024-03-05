package org.example;

public class DisplayThread extends Thread{
    private final ListaTranzactii listaTranzactii;

    public DisplayThread(ListaTranzactii listaTranzactii) {
        this.listaTranzactii = listaTranzactii;
    }
    @Override
    public void run() {
        while(true) {
            if(listaTranzactii.robotsHaveFinished()) {
                System.out.println("No robots are working, I should get outta here!");
                break;
            }
            listaTranzactii.afiseazaTranzactii();
            try {
                sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
