import java.time.LocalDateTime;
import java.util.Random;

public class Animal extends Thread{
    private final Curte curte;
    private final Registru registru;
    private final int id, ct = 7;

    public Animal(int id, Curte curte, Registru registru) {
        this.id = id;
        this.curte = curte;
        this.registru = registru;
    }
    @Override
    public void run() {
        while(true) {
            try {
                int nrPortiiConsumate = curte.consuma();
                if(nrPortiiConsumate == 0) {
                    break;
                }
                LocalDateTime timp = LocalDateTime.now();
                Inregistrare inregistrare = new Inregistrare(id, 1, timp, "consuma");
                registru.addInregistrare(inregistrare);
                sleep(ct);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("done animal");
    }
}
