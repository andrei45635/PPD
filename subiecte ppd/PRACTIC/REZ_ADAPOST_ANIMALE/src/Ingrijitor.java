import java.time.LocalDateTime;
import java.util.Random;

public class Ingrijitor extends Thread{
    private final int id;
    private final Curte curte;
    private final Registru registru;
    private final int nrSpatii;
    private final int x = 10, y = 30;

    public Ingrijitor(int id, Curte curte, Registru registru, int nrSpatii) {
        this.id = id;
        this.curte = curte;
        this.registru = registru;
        this.nrSpatii = nrSpatii;
    }

    @Override
    public void run() {
        Random random = new Random();
        int lt = random.nextInt(x, y);
        for(int i = 0; i < 100; i++) {
            int nrPortii = random.nextInt(1, nrSpatii + 1);
            try {
                curte.ofera(nrPortii);
                LocalDateTime timp = LocalDateTime.now();
                Inregistrare inregistrare = new Inregistrare(id, nrPortii, timp, "oferit");
                registru.addInregistrare(inregistrare);
                sleep(lt);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        curte.setFinishedIngrijitori();
        System.out.println("done ingrijitor");
    }
}
