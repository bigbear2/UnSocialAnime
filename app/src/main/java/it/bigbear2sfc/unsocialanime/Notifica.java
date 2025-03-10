package it.bigbear2sfc.unsocialanime;

public class Notifica {
    private int id;
    private String titolo;
    private String descrizione;
    private String link;
    private String data;
    private String icona;

    // Getter e Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // Getter e Setter
    public String getIcona() {
        return icona;
    }

    public void setIcona(String icona) {
        this.icona = icona;
    }

    @Override
    public String toString() {
        return "Notifica{" +
                "id='" + id + '\'' +
                ", titolo='" + titolo + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", link='" + link + '\'' +
                ", data='" + data + '\'' +
                ", icona='" + icona + '\'' +
                '}';
    }
}