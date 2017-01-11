<?php

namespace App\Http\Controllers;

use Laravel\Lumen\Routing\Controller as BaseController;

class Controller extends BaseController
{
  public function getHomeUrl() {
    $pathInfo = pathinfo($_SERVER['PHP_SELF']);
    $hostName = $_SERVER['HTTP_HOST'];
    $protocol = strtolower(substr($_SERVER["SERVER_PROTOCOL"],0,5))=='https://'?'https://':'http://';
    return $protocol.$hostName.$pathInfo['dirname']."/";
  }

  public function addFiles(&$musique){
    // on ajout le lien + l'Image
    if(file_exists(getcwd().'/upload/'.$musique->id.'.mp3')){
      $musique->url = $this->getHomeUrl().'/upload/'.$musique->id.'.mp3';
    }
    if(file_exists(getcwd().'/upload/'.$musique->id.'.jpg')){
      $musique->image = $this->getHomeUrl().'/upload/'.$musique->id.'.jpg';
    }
  }
}
