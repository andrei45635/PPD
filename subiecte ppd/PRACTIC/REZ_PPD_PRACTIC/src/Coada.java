import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Coada {
    private final Queue<Mesaj> messageList;
    private final int noDepartments, noBibliotecari;
    private int finishedDepartments, finishedBibliotecari;
    private final int MAX;
    private static final int chunkSize = 10;
    private int noMessagesWritten; //used by administrator

    public Coada(int MAX, int noDepartments, int noBibliotecari) {
        this.noDepartments = noDepartments;
        this.noBibliotecari = noBibliotecari;
        this.MAX = MAX;
        messageList = new LinkedList<>();
        this.finishedDepartments = 0;
        this.noMessagesWritten = 0;
    }

    public synchronized void adauga(List<Mesaj> mesageDepartament) throws InterruptedException {
        while (messageList.size() + mesageDepartament.size() > MAX) {
            wait();
        }
        messageList.addAll(mesageDepartament);
        noMessagesWritten += mesageDepartament.size();
        notifyAll();
    }

    public synchronized Mesaj preia() throws InterruptedException {
        while (messageList.size() == 0) {
            if(finishedDepartments == noDepartments) {
                return null;
            }
            wait();
        }
        Mesaj mesaj = messageList.poll();
        if(messageList.size() + 10 <= MAX) {
            notifyAll();
        }
        return mesaj;
    }

    public synchronized void setFinishedDepartments() {
        this.finishedDepartments++;
        notifyAll();
    }

    public boolean everyoneFinished() {
        return finishedDepartments == noDepartments && finishedBibliotecari == noBibliotecari;
    }

    public synchronized int getNoMessagesWritten() {
        return noMessagesWritten;
    }

    public synchronized void setFinishedNoBibliotecari() {
        this.finishedBibliotecari++;
    }
}
