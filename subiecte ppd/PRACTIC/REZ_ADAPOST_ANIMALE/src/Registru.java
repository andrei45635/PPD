import java.util.ArrayList;
import java.util.List;

public class Registru {
    private final List<Inregistrare> inregistrareList;

    public Registru() {
        this.inregistrareList = new ArrayList<>();
    }

    public synchronized void addInregistrare(Inregistrare inregistrare) {
        this.inregistrareList.add(inregistrare);
    }
}
