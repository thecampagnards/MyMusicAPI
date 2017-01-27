<?php

namespace App\Http\Controllers;

use Illuminate\Support\Facades\DB;
use Illuminate\Http\Request;
use GuzzleHttp\Client;
use Illuminate\Support\Facades\Auth;

class MusiqueController extends Controller
{

  const DB_STRUCTURE = array('id', 'title', 'length', 'artist', 'id_utilisateur', 'listen', 'state');

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

  public function listen($idMusique){
    try{
      // incrémenter dans la bdd
      DB::table('MUSIQUE')->whereId($idMusique)->increment('listen');

      // check si utilisateur
      if (Auth::id()) {
        DB::table('UTILISATEUR_HISTORIQUE')->insert(['id_utilisateur' => Auth::id(), 'id_musique' => $idMusique]);
      }

      return response()->json('ok');
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function add(Request $request){
    try{
      return response()->json($this->builder($request));
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function edit(Request $request, $id){
    try{
      return response()->json($this->builder($request));
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function delete($id){
    try{
      $this->remove($id);
      return response()->json('ok');
    } catch (Exception $e){
      return response()->json($e);
    }
  }

  public function remove($id){
    $musique = new \StdClass();
    $musique->id = $id;
    $this->addFiles($musique);
    if(!empty($musique->link)){
      unlink(getcwd().'/upload/'.$musique->id.'.mp3');
    }
    if(!empty($musique->image)){
      unlink(getcwd().'/upload/'.$musique->id.'.jpg');
    }
    DB::table('MUSIQUE')->whereId($musique->id)->delete();
  }

  private function cleanForQuery($musique){
    if(!empty($musique->utilisateur)){
      $musique->id_utilisateur = Auth::id();
    }

    return array_filter(
      (array) $musique,
      function ($key) {
        return in_array($key, self::DB_STRUCTURE);
      },
      ARRAY_FILTER_USE_KEY
    );
  }

  private function builder(Request $request){

    $musique = json_decode($request->get('musique'));
    // check si lien local
    if(!empty($musique->url) && $request->server('HTTP_HOST') === parse_url($musique->url, PHP_URL_HOST)){
      unset($musique->url);
    }

    // si on a une url
    if(!empty($musique->url)){
      //check si youtube
      if (preg_match("/(http:|https:)?\\/\\/(www\\.)?(youtube.com|youtu.be)\\/(watch)?(\\?v=)?(\\S+)?/", $musique->url) === 1){
        $client = new Client();
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
      // dossier d'upload et des scripts
      $downloadDir = getcwd().'/upload/';
      $scriptDir = getcwd().'/../app/Scripts/';
      // on télécharge le mp3 avec un delay de 15s
      do{
        // on supprime si le fichier existe
        if(file_exists(getcwd().'/upload/'.$musique->id.'.mp3')){
          unlink(getcwd().'/upload/'.$musique->id.'.mp3');
        }
        $response = $client->request('GET', $musique->url, ['sink' => $downloadDir.$musique->id.'.mp3', 'connect_timeout' => 15, 'http_errors' => false]);
      }while($response->getStatusCode() !== 200);
      // on recupere les infos du mp3 + ajout du cover
      $newData = json_decode(exec(escapeshellcmd('python '.$scriptDir.'/getInfoMP3.py '.$musique->id.' '.$downloadDir)));
      // on merge les données
      $musique = (object)array_merge((array)$newData, (array)$musique);
    } elseif ($request->hasFile('file') && $request->file('file')->isValid()) {
      // upload du file
      $request->file('file')->move(getcwd().'/upload/', $musique->id.'.mp3');
      $newData = json_decode(exec(escapeshellcmd('python '.$scriptDir.'/getInfoMP3.py '.$musique->id.' '.$downloadDir)));
      $musique = (object)array_merge((array)$newData, (array)$musique);
    }

    if ($request->hasFile('cover') && $request->file('cover')->isValid()) {
      // upload du cover
      $request->file('cover')->move($downloadDir, $musique->id.'.jpg');
    }

    // check du fichier si il n'existe pas déja
    if(!empty($musique->url) || ($request->hasFile('cover') && $request->file('cover')->isValid())){
      $idExist = exec(escapeshellcmd('/'.$scriptDir.'/checkFileMP3.sh '.$downloadDir.'/'.$musique->id.'.mp3 '.$downloadDir));
      if(!empty($idExist)){
        // on la supprime
        $this->remove($musique->id);
        // on recupère le titre de la chanson
        $musiqueExist = DB::table('MUSIQUE')->whereId($idExist)->first();
        throw new \Exception('La musique existe déjà ('.json_encode($musiqueExist).').');
      }
    }

    // on l'update
    DB::table('MUSIQUE')->where('id', $musique->id)->update($this->cleanForQuery($musique));

    // on ajoute les fichiers
    $this->addFiles($musique);
    return $musique;
  }
}
