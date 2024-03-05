import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {
    private static final int P = 5;
    private static final int C = 15;
    private static final int MAX = 30;
    public static void main(String[] args) throws InterruptedException {
        Coada coada = new Coada(MAX, P, C);
        Map<Character, Set<Mesaj>> mapTitlu = new HashMap<>();
        Map<Character, Set<Mesaj>> mapAutor = new HashMap<>();
        Departament[] departaments = new Departament[P];
        for(int i = 0; i < P; i++) {
            departaments[i] = new Departament(i+1, coada);
            departaments[i].start();
        }
        Bibliotecar[] bibliotecars = new Bibliotecar[C];
        for(int i = 0; i < C; i++) {
            bibliotecars[i] = new Bibliotecar(coada, mapTitlu, mapAutor);
            bibliotecars[i].start();
        }
        Administrator administrator = new Administrator(mapTitlu, mapAutor, coada);
        administrator.start();
        for(int i = 0; i < P; i++) {
            departaments[i].join();
        }
        for(int i = 0; i < C; i++) {
            bibliotecars[i].join();
        }
        administrator.join();
        System.out.println("Gata inca un examen! :D");
        System.out.println(mapAutor);
    }
}