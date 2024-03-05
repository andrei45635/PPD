import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Departament extends Thread{
    private final int id;
    private final Coada coada;
    private static final int T = 5;

    public Departament(int id, Coada coada) {
        this.id = id;
        this.coada = coada;
    }

    @Override
    public void run() {
        try {
            String filename = "dep" + id + ".txt";
            File myFile = new File(filename);
            Scanner myReader = new Scanner(myFile);
            int readMessages = 0;
            List<Mesaj> messages = new ArrayList<>();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] parts = data.split(",");
                Mesaj mesaj = new Mesaj(parts[0], parts[1]);
                messages.add(mesaj);
                readMessages++;
                if(readMessages == 10) {
                    coada.adauga(messages);
                    readMessages = 0;
                    messages = new ArrayList<>(); //we empty the collection
                }
                sleep(T);
            }
            coada.setFinishedDepartments();
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
