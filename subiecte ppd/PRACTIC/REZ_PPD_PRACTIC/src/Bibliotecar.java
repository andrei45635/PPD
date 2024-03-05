import com.sun.source.tree.Tree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Bibliotecar extends Thread{
    private final Coada coada;
    private final Map<Character, Set<Mesaj>> mapTitlu;
    private final Map<Character, Set<Mesaj>> mapAutor;
    private static final int T = 5;

    public Bibliotecar(Coada coada, Map<Character, Set<Mesaj>> mapTitlu, Map<Character, Set<Mesaj>> mapAutor) {
        this.coada = coada;
        this.mapTitlu = mapTitlu;
        this.mapAutor = mapAutor;
    }

    @Override
    public void run() {
        while(true) {
            Mesaj mesaj;
            try {
                mesaj = coada.preia();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(mesaj == null) {
                break; //noMoreMessages and all departments finished
            }
            synchronized (mapTitlu) {
                Character character = mesaj.getTitlu().charAt(0);
                if(mapTitlu.get(character) == null) {
                    mapTitlu.put(character, new TreeSet<>());
                }
                Set<Mesaj> messages = mapTitlu.get(character);
                messages.add(mesaj);
            }
            synchronized (mapAutor) {
                Character character = mesaj.getAutor().charAt(0);
                if(mapAutor.get(character) == null) {
                    mapAutor.put(character, new TreeSet<>());
                }
                Set<Mesaj> messages2 = mapAutor.get(character);
                messages2.add(mesaj);
            }
        }
        coada.setFinishedNoBibliotecari();
    }
}
