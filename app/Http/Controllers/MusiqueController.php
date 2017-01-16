<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\DB;
use Illuminate\Http\Request;
use GuzzleHttp\Client;

class MusiqueController extends Controller
{

  const DB_STRUCTURE = array('id', 'title', 'length', 'artist', 'id_utilisateur');

  public function show($data = null){
    try{
      $data = json_decode(urldecode($data));
      // on recupère les musiques
      $musiques = DB::table('MUSIQUE')
      ->where($this->cleanForQuery($data))
      ->orderBy('id', 'DESC')
      ->get();

      // on les parcours
      foreach ($musiques as $key => &$musique) {

        $this->addFiles($musique);

        // on ajoute l'utilisateur
        if(!empty($musique->id_utilisateur)){
          $musique->utilisateur = DB::table('UTILISATEUR')
          ->where(['id' => $musique->id_utilisateur])
          ->first();
          unset($musique->utilisateur->password);
        }

        // on ajoute les playlists
        $musique->playlists = DB::table('PLAYLIST')
        ->join('PLAYLIST_MUSIQUE', 'PLAYLIST.id', '=', 'PLAYLIST_MUSIQUE.id_playlist')
        ->where(['id_musique' => $musique->id])
        ->get();

      }
      return response()->json($musiques);
    }catch (Exception $e){
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
      $musique->id = $id;
      $this->addFiles($musique);
      if(!empty($musique->link)){
        unlink(getcwd().'/upload/'.$musique->id.'.mp3');
      }
      if(!empty($musique->image)){
        unlink(getcwd().'/upload/'.$musique->id.'.jpg');
      }
      DB::table('MUSIQUE')->where('id', $musique->id)->delete();

      return response()->json('ok');
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  private function cleanForQuery($musique){
    if(!empty($musique->utilisateur)){
      $musique->id_utilisateur = $musique->utilisateur['id'];
    }

    return array_filter(
      (array) $musique,
      function ($key) {
        return in_array($key, self::DB_STRUCTURE);
      },
      ARRAY_FILTER_USE_KEY
    );
  }

  private function builder(Array $musique){

    $musique = (object)$musique;

    // si on a une url
    if(!empty($musique->url)){
      $client = new Client();
      //check si youtube
      if (preg_match("/(http:|https:)?\\/\\/(www\\.)?(youtube.com|youtu.be)\\/(watch)?(\\?v=)?(\\S+)?/", $musique->url) === 1){
        // promise de la requete l'api mp3 downloader
        $promise = $client->requestAsync('GET', 'https://www.youtubeinmp3.com/fetch/?format=JSON&video='.$musique->url);
        $promise->then(function ($response) use (&$musique){
          // on recupere le lien de dl
          $musique->url = json_decode($response->getBody())->link;
        });
      }
    }

    // on insert en db + recuperation de l'id
    if(empty($musique->id)){
      $musique->id = DB::table('MUSIQUE')->insertGetId($this->cleanForQuery($musique));
    }

    // on download le mp3
    if(!empty($musique->url)){
      // on check la promise + on la wait
      if(!empty($promise)){
        $promise->wait();
      }
      // on télécharge le mp3 avec un delay de 15s
      do{
        $response = $client->request('GET', $musique->url, ['sink' => getcwd().'/upload/'.$musique->id.'.mp3', 'connect_timeout' => 15, 'http_errors' => false]);
      }while($response->getStatusCode() !== 200);
      // on recupere les infos du mp3 + ajout du cover
      $newData = json_decode(exec(escapeshellcmd('python '.getcwd().'/../app/Scripts/getInfoMP3.py '.$musique->id.' '.getcwd().'/upload')));
      // on merge les données
      $musique = (object)array_merge((array)$newData, (array)$musique);
    }

    // on l'update
    DB::table('MUSIQUE')->where('id', $musique->id)->update($this->cleanForQuery($musique));

    // on ajoute les fichier
    $this->addFiles($musique);
    return $musique;
  }
}
