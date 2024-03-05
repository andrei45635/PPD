import java.util.Random;

public class Main {
    private static final int nrIngrijitori = 3, nrAnimale = 10;
    private static final int portiiInitiale = 0, nrSpatii = 5;
    public static void main(String[] args) throws InterruptedException {
        Ingrijitor[] ingrijitors = new Ingrijitor[nrIngrijitori];
        Animal[] animals = new Animal[nrAnimale];
        Curte curte = new Curte(portiiInitiale, nrIngrijitori);
        Registru registru = new Registru();
        for(int i = 0; i < nrIngrijitori; i++) {
            ingrijitors[i] = new Ingrijitor(i+1, curte, registru, nrSpatii);
            ingrijitors[i].start();

        }
        for(int i = 0; i < nrAnimale; i++) {
            animals[i] = new Animal(nrIngrijitori + i + 1, curte, registru);
            animals[i].start();
        }

        for(int i = 0; i < nrIngrijitori; i++) {
            ingrijitors[i].join();
        }
        for(int i = 0; i < nrAnimale; i++) {
            animals[i].join();
        }
    }
}