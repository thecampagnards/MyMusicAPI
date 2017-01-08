package mymusicapi.builders;

import mymusicapi.entities.Musique;
import mymusicapi.tools.DataBase;

import com.mpatric.mp3agic.Mp3File;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;


public class MusiqueBuilder {

    private MusiqueBuilder(){}

    public static ArrayList<Musique> getMusiques() throws Exception
    {
        ArrayList<Musique> musiques = new ArrayList<>();
        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM MUSIQUE");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            musiques.add(createObjectMusique(rs));
        }
        return musiques;
    }

    public static Musique getMusique(Long id) throws Exception
    {

        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM MUSIQUE WHERE id = ?");
        ps.setLong(1,id);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            return createObjectMusique(rs);
        }
        return null;
    }

    public static Musique addMusique(Musique musique) throws Exception
    {

        //on récupère le fichier
        File file = new File(System.getProperty("java.io.tmpdir") + "/tmp.mp3");
        file.deleteOnExit();
        FileUtils.copyURLToFile(new URL(musique.getLink()), file);

        //on check le fichier
        //https://github.com/mpatric/mp3agic
        Mp3File mp3file = new Mp3File(file);

        //ajout des données du fichier à la fiche dans la bdd
        if(musique.getTitle().isEmpty()){
            if(mp3file.getId3v1Tag() != null){
                musique.setTitle(mp3file.getId3v1Tag().getTitle());
            }else if(mp3file.getId3v2Tag() != null){
                musique.setTitle(mp3file.getId3v2Tag().getTitle());
            }else{
                musique.setTitle(mp3file.getFilename());
            }
        }
        if(musique.getLength() != null){
            musique.setLength(mp3file.getLengthInSeconds());
        }

        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "INSERT INTO MUSIQUE " +
                        "(title, length) " +
                        "VALUES(?,?)"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestMusique(ps, musique);
        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Création de la musique a échoué, il vous manque des attributs.");
        }
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                musique.setId(generatedKeys.getLong(1));
                //enregistrement du fichier mp3
                ResourceBundle rb = ResourceBundle.getBundle("main");
                mp3file.save(rb.getString("RESOURCE_PATH") +"/upload/"+musique.getId() + ".mp3");
                musique.setLink("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".mp3");
            }
            else {
                throw new SQLException("Création de le musique a échoué, pas d'ID obtenu.");
            }
        }

        return musique;
    }

    public static Musique editMusique(Musique musique) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "UPDATE MUSIQUE SET " +
                        "title = ?, " +
                        "length = ?" +
                        "WHERE id = ?"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestMusique(ps, musique);
        ps.executeUpdate();
        return musique;
    }

    private static PreparedStatement createRequestMusique(PreparedStatement ps, Musique musique) throws SQLException {
        ps.setString(1,musique.getTitle());
        ps.setLong(2,musique.getLength());
        if(musique.getId() != null){
            ps.setLong(4,musique.getId());
        }
        return ps;
    }

    private static Musique createObjectMusique(ResultSet rs) throws SQLException, UnknownHostException {
        Musique musique = new Musique();
        musique.setId(rs.getLong("id"));
        musique.setTitle(rs.getString("title"));
        musique.setLength(rs.getLong("length"));
        musique.setLink("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".mp3");
        return musique;
    }
}
