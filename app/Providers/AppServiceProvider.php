<?php

namespace App\Providers;

use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    /**
     * Register any application services.
     *
     * @return void
     */
    public function addFiles(&$musique)
    {
      // on ajout le lien + l'Image
      if(file_exists(getcwd().'/upload/'.$musique->id.'.mp3')){
        $musique->link = 'http://'.$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI'].'/upload/'.$musique->id.'.mp3';
      }
      if(file_exists(getcwd().'/upload/'.$musique->id.'.jpg')){
        $musique->image = 'http://'.$_SERVER['HTTP_HOST'].$_SERVER['REQUEST_URI'].'/upload/'.$musique->id.'.jpg';
      }
      return $musique;
    }
}
