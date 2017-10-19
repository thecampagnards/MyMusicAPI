<?php

namespace App\Http\Controllers;
use Illuminate\Support\Facades\DB;
use GuzzleHttp\Client;

class SearchController extends Controller
{
  public function show($search){
    try{

      // encodage pour les requetes sur YouTube ou SoundCloud
      $searchEncode   = urlencode($search);
      $musiques = new \StdClass();
      // client de requete
      $client = new Client();

      // SoundCloud
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
          $musique->cover         = $data->artwork_url;
          $musiques->SoundCloud[] = $musique;
        }
      });

      // YouTube
      $promiseYouTube = $client->requestAsync('GET', 'https://www.googleapis.com/youtube/v3/search?part=snippet&maxResults=25&q='.$searchEncode.'&key='.env('YOUTUBE_API_KEY'));
      $promiseYouTube->then(function($response) use (&$musiques){
        $datas = json_decode($response->getBody());
        foreach ($datas->items as $data){
          if($data->id->kind === 'youtube#video'){
            // on rempli notre objet musiques
            $musique = new \StdClass();
            $musique->videoId    = $data->id->videoId;
            $musique->title      = $data->snippet->title;
            $musique->artist     = $data->snippet->channelTitle;
            $musique->cover      = $data->snippet->thumbnails->default->url;
            $musiques->YouTube[] = $musique;
          }
        }
      });

      // MyMusic
      $musiques->MyMusic = DB::table('MUSIQUE')
        ->where('artist', 'like', '%'.$search.'%')
        ->orWhere('title', 'like', '%'.$search.'%')
        ->orderBy('id', 'DESC')
        ->get();

      // on les parcours
      foreach ($musiques->MyMusic as &$musique){
          // On ajoute les fichiers mp3 et cover
          $this->addFiles($musique);
      }

      // On attend la fin du traitement des requetes
      $promiseSoundCloud->wait() && $promiseYouTube->wait();
      return response()->json($musiques);
    }catch (Exception $e){
      return response()->json($e);
    }
  }
}
