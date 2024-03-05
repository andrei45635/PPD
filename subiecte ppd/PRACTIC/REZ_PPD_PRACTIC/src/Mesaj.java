public class Mesaj implements Comparable<Mesaj>{
    private String titlu, autor;

    public Mesaj(String titlu, String autor) {
        this.titlu = titlu;
        this.autor = autor;
    }

    public String getTitlu() {
        return titlu;
    }

    public void setTitlu(String titlu) {
        this.titlu = titlu;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    @Override
    public String toString() {
        return "Mesaj{" +
                "titlu='" + titlu + '\'' +
                ", autor='" + autor + '\'' +
                '}';
    }

    @Override
    public int compareTo(Mesaj o) {
        return this.autor.compareTo(o.autor );
    }
}
