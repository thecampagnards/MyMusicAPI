package mymusicapi.builders;

import mymusicapi.entities.Musique;
import mymusicapi.entities.Playlist;
import mymusicapi.tools.DataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PlaylistBuilder {

    private PlaylistBuilder(){}

    public static ArrayList<Playlist> getPlaylists() throws Exception
    {
        ArrayList<Playlist> playlists = new ArrayList<>();
        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM PLAYLIST");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            playlists.add(createObjectPlaylist(rs));
        }
        return playlists;
    }

    public static Playlist getPlaylist(Long id) throws Exception
    {

        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM PLAYLIST WHERE id = ?");
        ps.setLong(1,id);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            return createObjectPlaylist(rs);
        }
        return null;
    }

    public static Playlist addPlaylist(Playlist playlist) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "INSERT INTO PLAYLIST " +
                        "(title, status, id_utilisateur) " +
                        "VALUES(?,?,?)"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestPlaylist(ps, playlist);

        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Création de la musique a échoué, il vous manque des attributs.");
        }

        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                playlist.setId(generatedKeys.getLong(1));
                // on ajoute les musiques
                addPlaylistMusiques(playlist);
            }
            else {
                throw new SQLException("Création de la musique a échoué, pas d'ID obtenu.");
            }
        }

        return playlist;
    }

    public static Playlist editPlaylist(Playlist playlist) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "UPDATE PLAYLIST SET " +
                        "title = ?, " +
                        "status = ?, " +
                        "id_utilisateur = ? " +
                        "WHERE id = ?"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestPlaylist(ps, playlist);
        ps.executeUpdate();
        ps = DataBase.getConnection().prepareStatement("DELETE FROM PLAYLIST_MUSIQUE WHERE id_playlist = ?");
        ps.setLong(1,playlist.getId());
        ps.executeUpdate();
        addPlaylistMusiques(playlist);
        return playlist;
    }

    private static void addPlaylistMusiques(Playlist playlist) throws SQLException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        // on re-ajoute les musiques
        PreparedStatement ps;
        for (Musique musiques : playlist.getMusiques()){
            ps = DataBase.getConnection().prepareStatement(
                    "INSERT INTO PLAYLIST_MUSIQUE " +
                            "(id_musique, id_playlist) " +
                            "VALUES(?,?)"
                    , Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1,musiques.getId());
            ps.setLong(2,playlist.getId());
            ps.executeUpdate();
        }
    }


    private static PreparedStatement createRequestPlaylist(PreparedStatement ps, Playlist playlist) throws SQLException {
        ps.setString(1,playlist.getTitle());
        ps.setByte(2,playlist.getStatus());
        ps.setLong(3,playlist.getUtilisateur().getId());

        if(playlist.getId() != null){
            ps.setLong(4,playlist.getId());
        }
        return ps;
    }

    private static Playlist createObjectPlaylist(ResultSet rs) throws Exception {
        Playlist playlist = new Playlist();
        playlist.setId(rs.getLong("id"));
        playlist.setTitle(rs.getString("title"));
        playlist.setStatus(rs.getByte("status"));
        playlist.setUtilisateur(UtilisateurBuilder.getUtilisateur(rs.getLong("id_utilisateur")));

        // recuperer les musiques
        ArrayList<Musique> musiques = new ArrayList<>();
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "SELECT * FROM MUSIQUE AS m, PLAYLIST_MUSIQUE AS pm WHERE pm.id_musique = m.id AND pm.id_playlist = ?"
        );
        ps.setLong(1,playlist.getId());
        rs = ps.executeQuery();
        while(rs.next()){
            musiques.add(MusiqueBuilder.createObjectMusique(rs));
        }
        playlist.setMusiques(musiques);

        return playlist;
    }
}
