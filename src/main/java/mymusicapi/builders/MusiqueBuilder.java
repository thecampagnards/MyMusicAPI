package mymusicapi.builders;

import mymusicapi.entities.Musique;
import mymusicapi.tools.DataBase;

import com.mpatric.mp3agic.Mp3File;

import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
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
        if(mp3file.hasId3v1Tag()){
            musique.setTitle(mp3file.getId3v1Tag().getTitle());
            musique.setArtist(mp3file.getId3v1Tag().getArtist());
        }else if(mp3file.hasId3v2Tag()){
            musique.setTitle(mp3file.getId3v2Tag().getTitle());
            musique.setArtist(mp3file.getId3v2Tag().getArtist());
        }
        if(musique.getLength() != null){
            musique.setLength(mp3file.getLengthInSeconds());
        }

        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "INSERT INTO MUSIQUE " +
                        "(title, artist, length) " +
                        "VALUES(?,?,?)"
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

                //recupération de l'image
                if (mp3file.hasId3v2Tag() && mp3file.getId3v2Tag().getAlbumImage() != null ) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(mp3file.getId3v2Tag().getAlbumImage()));
                    //enregistrement de l'image
                    File fileImage = new File(rb.getString("RESOURCE_PATH") +"/upload/"+musique.getId() + ".jpg");
                    ImageIO.write(img, "jpg", fileImage);

                    musique.setImage("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".jpg");
                }
                musique.setLink("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".mp3");
            }
            else {
                throw new SQLException("Création de le musique a échoué, pas d'ID obtenu.");
            }
        }

        return musique;
    }

    public static void addImageMusique(Long id, BufferedImage bufferedImage) throws Exception
    {
        ResourceBundle rb = ResourceBundle.getBundle("main");
        //on converti la photo en jpg
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
        //on enregistre la photo
        ImageIO.write(newBufferedImage, "jpg", new File(rb.getString("RESOURCE_PATH") + "/upload/" + id + ".jpg"));

        //on recupere le mp3
        Mp3File mp3file = new Mp3File(rb.getString("RESOURCE_PATH") + "/upload/" + id + ".mp3");
        if (mp3file.hasId3v2Tag()) {
            //recuperer l'image en byte
            WritableRaster raster = bufferedImage .getRaster();
            DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();
            mp3file.getId3v2Tag().setAlbumImage(data.getData(), "image");

            //on l'enregistre
            mp3file.save(rb.getString("RESOURCE_PATH") +"/upload/1.mp3");
        }
        return;
    }

    public static Musique editMusique(Musique musique) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "UPDATE MUSIQUE SET " +
                        "title = ?, " +
                        "artist = ?, " +
                        "length = ? " +
                        "WHERE id = ?"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestMusique(ps, musique);
        ps.executeUpdate();
        return musique;
    }

    private static PreparedStatement createRequestMusique(PreparedStatement ps, Musique musique) throws SQLException {
        ps.setString(1,musique.getTitle());
        ps.setString(2,musique.getArtist());
        ps.setLong(3,musique.getLength());
        if(musique.getId() != null){
            ps.setLong(4,musique.getId());
        }
        return ps;
    }

    public static Musique createObjectMusique(ResultSet rs) throws Exception {
        Musique musique = new Musique();
        musique.setId(rs.getLong("id"));
        musique.setTitle(rs.getString("title"));
        musique.setArtist(rs.getString("artist"));
        musique.setLength(rs.getLong("length"));
        musique.setLink("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".mp3");
        musique.setImage("http://"+InetAddress.getLocalHost().getHostAddress()+":8081/upload/"+ musique.getId() + ".jpg");
        musique.setUtilisateur(UtilisateurBuilder.getUtilisateur(rs.getLong("id_utilisateur")));
        return musique;
    }
}
