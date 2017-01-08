package mymusicapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import mymusicapi.builders.UtilisateurBuilder;
import mymusicapi.entities.Utilisateur;
import mymusicapi.tools.Json;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.InputStream;

@Path("/utilisateur")
public class UtilisateurService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUtilisateurs() throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(UtilisateurBuilder.getUtilisateurs(), true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUtilisateur(@PathParam("id") Long id) throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(UtilisateurBuilder.getUtilisateur(id), true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUtilisateur(String data) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return Response.status(200).entity(
                    Json.serialize(
                        UtilisateurBuilder.addUtilisateur(
                            objectMapper.readValue(data, Utilisateur.class)
                        ),
                     true)
            ).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response editUtilisateur(@PathParam("id") long id, String data) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            //on récupère le user
            Utilisateur utilisateur = UtilisateurBuilder.getUtilisateur(id);
            ObjectReader updater = objectMapper.readerForUpdating(utilisateur);
            //on merge le user avec les données recus
            utilisateur = updater.readValue(data);

            return Response.status(200).entity(
                    Json.serialize(UtilisateurBuilder.editUtilisateur(utilisateur),true)
            ).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e.getMessage(), true)).build();
        }
    }

    @POST
    @Path("/upload")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public Response uploadFileWithData(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition cdh) throws Exception{

        Image img = ImageIO.read(fileInputStream);
        JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(img)));
        System.out.println(cdh.getName());

        return Response.ok("Cool Tools!").build();
    }

}
