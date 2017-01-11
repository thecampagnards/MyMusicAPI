<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\DB;
use Illuminate\Http\Request;
use GuzzleHttp\Client;
use App\Http\Controllers\MusiqueController;

class PlaylistController extends Controller
{

  const DB_STRUCTURE = array('id', 'title', 'status', 'id_utilisateur');

  public function show($data = null){
    try{

      $data = json_decode(urldecode($data));

      // on recupère les playlists
      $playlists = DB::table('PLAYLIST')
      ->where($this->cleanForQuery($data))
      ->get();

      // on les parcours
      foreach ($playlists as &$playlist) {

        // on ajoute l'utilisateur
        if(!empty($playlist->id_utilisateur)){
          $playlist->utilisateur = DB::table('UTILISATEUR')
          ->where(['id' => $playlist->id_utilisateur])
          ->get();
        }

        // on ajoute les musiques
        $playlist->musiques = DB::table('MUSIQUE')
        ->join('PLAYLIST_MUSIQUE', 'MUSIQUE.id', '=', 'PLAYLIST_MUSIQUE.id_musique')
        ->where(['id_playlist' => $playlist->id])
        ->get();
        // on ajoute les fichiers
        foreach ($playlist->musiques as &$musique) {
          MusiqueController::addFiles($musique);
        }

      }
      return response()->json($playlists);
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function add(Request $request){
    try{
      return response()->json($this->builder($request->All()));
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function edit(Request $request, $id){
    try{
      return response()->json($this->builder($request->All()));
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function delete($id){
    try{
      DB::table('PLAYLIST')->where('id', $id)->delete();
      DB::table('PLAYLIST_MUSIQUE')->where('id_playlist', $id)->delete();

      return response()->json('ok');
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  private function cleanForQuery($playlist){
    if(!empty($playlist->utilisateur)){
      $playlist->id_utilisateur = $playlist->utilisateur->id;
    }

    return array_filter(
      (array) $playlist,
      function ($key) {
        return in_array($key, self::DB_STRUCTURE);
      },
      ARRAY_FILTER_USE_KEY
    );
  }

  public static function builder(Array $playlist){

    $playlist = (object)$playlist;

    // on insert en db + recuperation de l'id
    if(empty($playlist->id)){
      $playlist->id = DB::table('PLAYLIST')->insertGetId($this->cleanForQuery($playlist));
    }else{
      DB::table('PLAYLIST')->where('id', $playlist->id)->update($this->cleanForQuery($playlist));
    }

    // on supprime les données de la table de liaison
    DB::table('PLAYLIST_MUSIQUE')->where('id', $playlist->id)->delete();

    // On ajoute les musiques à la table de liaison
    foreach ($playlist->musiques as $musique) {
      DB::table('PLAYLIST_MUSIQUE')->insert(['id_musique' => $musique->id, 'id_playlist' => $playlist->id]);
    }
    return $playlist;
  }
}
