package mymusicapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import mymusicapi.builders.PlaylistBuilder;
import mymusicapi.entities.Playlist;
import mymusicapi.tools.Json;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/playlist")
public class PlaylistService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlaylists() throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(PlaylistBuilder.getPlaylists(), true)).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e, true)).build();
        }
    }

    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPlaylist(@PathParam("id") Long id) throws JsonProcessingException {
        try {
            return Response.status(200).entity(Json.serialize(PlaylistBuilder.getPlaylist(id), true)).build();
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
                            PlaylistBuilder.addPlaylist(
                                    objectMapper.readValue(data, Playlist.class)
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
    public Response editPlaylist(@PathParam("id") long id, String data) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            //on récupère la playlist
            Playlist playlist = PlaylistBuilder.getPlaylist(id);
            ObjectReader updater = objectMapper.readerForUpdating(playlist);
            //on merge la playlist avec les données recus
            playlist = updater.readValue(data);

            return Response.status(200).entity(
                    Json.serialize(PlaylistBuilder.editPlaylist(playlist),true)
            ).build();
        } catch (Exception e) {
            return Response.status(500).entity(Json.serialize(e.getMessage(), true)).build();
        }
    }

}
