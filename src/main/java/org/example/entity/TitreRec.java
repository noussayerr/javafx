package org.example.entity;

public class TitreRec {


    private int idtitre;
    private String titrerec;

    public TitreRec() {
    }

    public TitreRec(int i, String s) {
        idtitre = i;
        titrerec = s;
    }

    public int getIdtitre() {
        return idtitre;
    }

    public void setIdtitre(int idtitre) {
        this.idtitre = idtitre;
    }

    public String getTitrerec() {
        return titrerec;
    }

    public void setTitrerec(String titrerec) {
        this.titrerec = titrerec;
    }

    @Override
    public String toString() {
        return titrerec;
    }
}
