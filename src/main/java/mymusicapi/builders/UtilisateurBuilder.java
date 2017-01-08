package mymusicapi.builders;

import mymusicapi.entities.Utilisateur;
import mymusicapi.tools.DataBase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class UtilisateurBuilder {

    private UtilisateurBuilder(){}

    public static ArrayList<Utilisateur> getUtilisateurs() throws Exception
    {
        ArrayList<Utilisateur> utilisateurs = new ArrayList<>();
        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM UTILISATEUR");
        ResultSet rs = ps.executeQuery();
        while(rs.next()){
            utilisateurs.add(createObjectUtilisateur(rs));
        }
        return utilisateurs;
    }

    public static Utilisateur getUtilisateur(Long id) throws Exception
    {

        PreparedStatement ps = DataBase.getConnection().prepareStatement("SELECT * FROM UTILISATEUR WHERE id = ?");
        ps.setLong(1,id);
        ResultSet rs = ps.executeQuery();
        if(rs.next()) {
            return createObjectUtilisateur(rs);
        }
        return null;
    }

    public static Utilisateur addUtilisateur(Utilisateur utilisateur) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "INSERT INTO UTILISATEUR " +
                        "(pseudo, motdepasse, email) " +
                        "VALUES(?,?,?)"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestUtilisateur(ps, utilisateur);
        int affectedRows = ps.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Création de l'utilisateur a échoué, il vous manque des attributs.");
        }
        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                utilisateur.setId(generatedKeys.getLong(1));
            }
            else {
                throw new SQLException("Création de l'utilisateur a échoué, pas d'ID obtenu.");
            }
        }

        return utilisateur;
    }

    public static Utilisateur editUtilisateur(Utilisateur utilisateur) throws Exception
    {
        PreparedStatement ps = DataBase.getConnection().prepareStatement(
                "UPDATE UTILISATEUR SET " +
                        "pseudo = ?, " +
                        "motdepasse = ?, " +
                        "email = ? " +
                        "WHERE id = ?"
                , Statement.RETURN_GENERATED_KEYS
        );
        ps = createRequestUtilisateur(ps, utilisateur);
        ps.executeUpdate();
        return utilisateur;
    }

    private static PreparedStatement createRequestUtilisateur(PreparedStatement ps, Utilisateur utilisateur) throws SQLException {
        ps.setString(1,utilisateur.getPseudo());
        ps.setString(2,utilisateur.getMotdepasse());
        ps.setString(3,utilisateur.getEmail());
        if(utilisateur.getId() != null){
            ps.setLong(4,utilisateur.getId());
        }
        return ps;
    }

    private static Utilisateur createObjectUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getLong("id"));
        utilisateur.setPseudo(rs.getString("pseudo"));
        utilisateur.setMotdepasse(rs.getString("motdepasse"));
        utilisateur.setEmail(rs.getString("email"));
        return utilisateur;
    }
}
