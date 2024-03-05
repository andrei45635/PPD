package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);
        int p = scanner.nextInt();
        int c = scanner.nextInt();
        int capacitateBanda = scanner.nextInt();
        System.out.println("producatori=" + p + " consumatori=" + c + " capacitate=" + capacitateBanda);

        Producator[] producatori = new Producator[p];
        Consumator[] consumatori = new Consumator[c];
        Banda banda = new Banda(capacitateBanda, p, c);
        ListaTranzactii listaTranzactii = new ListaTranzactii(p, c);
        for(int i = 0; i < p; i++) {
            producatori[i] = new Producator(i+1, banda, listaTranzactii);
            producatori[i].start();
        }
        for(int i = 0; i < c; i++) {
            consumatori[i] = new Consumator(p+i+1, banda, listaTranzactii);
            consumatori[i].start();
        }
        DisplayThread displayThread = new DisplayThread(listaTranzactii);
        displayThread.start();

        for(int i = 0; i < p; i++) {
            producatori[i].join();
        }
        for(int i = 0; i < c; i++) {
            consumatori[i].join();
        }
        displayThread.join();

    }
}