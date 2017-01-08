package mymusicapi.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import mymusicapi.builders.MusiqueBuilder;
import mymusicapi.entities.Musique;
import mymusicapi.tools.Json;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.regex.Pattern;


@Path("/musique")
public class MusiqueService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMusiques() throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(MusiqueBuilder.getMusiques(), true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMusique(@PathParam("id") Long id) throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(MusiqueBuilder.getMusique(id), true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addMusiqueByLink(String data) throws Exception {
        try {

            //on recupere les données du post
            ObjectMapper objectMapper = new ObjectMapper();
            Musique musique = objectMapper.readValue(data, Musique.class);

            //check si lien youtube
            Pattern pattern = Pattern.compile("(http:|https:)?\\/\\/(www\\.)?(youtube.com|youtu.be)\\/(watch)?(\\?v=)?(\\S+)?");
            if(pattern.matcher(musique.getLink()).find()){
                ObjectMapper mapper = new ObjectMapper();
                musique = mapper.readValue(
                        new URL("https://www.youtubeinmp3.com/fetch/?format=JSON&video=" + musique.getLink()),
                        Musique.class
                );
            }else{
                musique.setLink(musique.getLink());
            }

            musique = MusiqueBuilder.addMusique(musique);
            return Response.status(200).entity(Json.serialize(musique,true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response editMusique(@PathParam("id") long id, String data) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            //on récupère la musique
            Musique musique = MusiqueBuilder.getMusique(id);
            ObjectReader updater = objectMapper.readerForUpdating(musique);
            //on merge la musique avec les données recus
            musique = updater.readValue(data);

            return Response.status(200).entity(
                    Json.serialize(MusiqueBuilder.editMusique(musique),true)
            ).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e.getMessage(), true)).build();
        }
    }
}