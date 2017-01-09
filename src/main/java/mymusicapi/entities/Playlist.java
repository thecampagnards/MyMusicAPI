package mymusicapi.entities;

import java.util.ArrayList;

public class Playlist {

    private Long id;
    private String title;
    private Byte status;
    private Utilisateur utilisateur;
    private ArrayList<Musique> musiques;

    public Playlist() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public ArrayList<Musique> getMusiques() {
        return musiques;
    }

    public void setMusiques(ArrayList<Musique> musiques) {
        this.musiques = musiques;
    }
}
