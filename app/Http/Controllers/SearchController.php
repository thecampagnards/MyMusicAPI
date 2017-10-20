<?php

namespace App\Http\Controllers;
use Illuminate\Support\Facades\DB;
use GuzzleHttp\Client;
use Illuminate\Http\Request;

class SearchController extends Controller {

  public function show(Request $request){

    try{

      // encodage pour les requetes sur YouTube ou SoundCloud
      $searchEncode   = urlencode($request['q']);
      $musiques = new \StdClass();
      // client de requete
      $client = new Client();

      // SoundCloud
      if(isset($request['soundcloud']) ? $request['soundcloud'] === false : true){
        $promiseSoundCloud = $client->requestAsync('GET', 'http://api.soundcloud.com/tracks?q='.$searchEncode.'&client_id='.env('SOUND_CLOUD_CLIENT_ID'));
        $promiseSoundCloud->then(function($response) use (&$musiques){
          $datas = json_decode($response->getBody());
          foreach ($datas as $data){
            // on rempli notre objet musique
            $musique = new \StdClass();
            $musique->title         = $data->title;
            $musique->artist        = $data->user->username;
            $musique->length        = round($data->duration / 1000);
            $musique->url           = $data->stream_url.'?client_id='.env('SOUND_CLOUD_CLIENT_ID');
            $musique->permalink_url = $data->permalink_url;
            $musique->image         = $data->artwork_url;
            $musiques->SoundCloud[] = $musique;
          }
        });
      }

      // YouTube
      if(isset($request['youtube']) ? $request['youtube'] === false : true){
        $promiseYouTube = $client->requestAsync('GET', 'https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=8&type=video&q='.$searchEncode.'&key='.env('YOUTUBE_API_KEY'));
        $promiseYouTube->then(function($response) use (&$musiques){
          $datas = json_decode($response->getBody());
          foreach ($datas->items as $data){
            // on rempli notre objet musiques
            $musique = new \StdClass();
            $musique->videoId    = $data->id->videoId;
            $musique->title      = $data->snippet->title;
            $musique->artist     = $data->snippet->channelTitle;
            $musique->image      = $data->snippet->thumbnails->default->url;
            $musiques->YouTube[] = $musique;
          }
        });
      }

      if(isset($request['mymusic']) ? $request['mymusic'] === false : true){
        // MyMusic
        $musiques->MyMusic = DB::table('MUSIQUE')
          ->where('artist', 'like', '%'.$request['q'].'%')
          ->orWhere('title', 'like', '%'.$request['q'].'%')
          ->orderBy('id', 'DESC')
          ->get();

        // on les parcours
        foreach ($musiques->MyMusic as &$musique){
            // On ajoute les fichiers mp3 et cover
            $this->addFiles($musique);
        }
      }

      // On attend la fin du traitement des requetes
      (isset($promiseSoundCloud) ? $promiseSoundCloud->wait() : null)
      && (isset($promiseYouTube) ? $promiseYouTube->wait() : null);
      return response()->json($musiques);
    }catch (Exception $e){
      return response()->json($e);
    }
  }
}
