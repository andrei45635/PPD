import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Administrator extends Thread{
    private final Map<Character, Set<Mesaj>> mapTitlu;
    private final Map<Character, Set<Mesaj>> mapAutor;
    private final Coada coada;
    private static final int TA = 15;

    public Administrator(Map<Character, Set<Mesaj>> mapTitlu, Map<Character, Set<Mesaj>> mapAutor, Coada coada) {
        this.mapTitlu = mapTitlu;
        this.mapAutor = mapAutor;
        this.coada = coada;
    }

    @Override
    public void run() {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("raport.txt"));
            while(true) {
                writeToFile(writer);
                if (coada.everyoneFinished()){
                    writer.write("\nBilant final:\n");
                    writeToFile(writer);
                    writer.close();
                    break;
                }
                try {
                    sleep(TA);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeToFile(BufferedWriter writer) throws IOException {
        int nrMesajeScrise = coada.getNoMessagesWritten();
        int nrMesajeMapTitlu = getNoMessagesTitlu();
        int nrMesajeMapAutor = getNoMessagesAutor();
        String mesaj = "mesaje scrise=" + nrMesajeScrise + " | mesajeMapTitlu=" + nrMesajeMapTitlu + " | mesajeMapAutor=" + nrMesajeMapAutor + "\n";
        writer.write(mesaj);
    }

    private int getNoMessagesTitlu() {
        int sum = 0;
        synchronized (mapTitlu) {
            for(Character character: mapTitlu.keySet()) {
                sum += mapTitlu.get(character).size();
            }
        }
        return sum;
    }

    private int getNoMessagesAutor() {
        int sum = 0;
        synchronized (mapAutor) {
            for (Character character: mapAutor.keySet()) {
                sum += mapAutor.get(character).size();
            }
        }
        return sum;
    }
}
