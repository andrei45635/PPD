import java.time.LocalDateTime;

public class Inregistrare {
    private int id, nrPortii;
    private LocalDateTime timp;
    private String actiune;

    public Inregistrare(int id, int nrPortii, LocalDateTime timp, String actiune) {
        this.id = id;
        this.nrPortii = nrPortii;
        this.timp = timp;
        this.actiune = actiune;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNrPortii() {
        return nrPortii;
    }

    public void setNrPortii(int nrPortii) {
        this.nrPortii = nrPortii;
    }

    public LocalDateTime getTimp() {
        return timp;
    }

    public void setTimp(LocalDateTime timp) {
        this.timp = timp;
    }

    public String getActiune() {
        return actiune;
    }

    public void setActiune(String actiune) {
        this.actiune = actiune;
    }
}
