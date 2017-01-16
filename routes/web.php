<?php

/*
|--------------------------------------------------------------------------
| Application Routes
|--------------------------------------------------------------------------
|
| Here is where you can register all of the routes for an application.
| It is a breeze. Simply tell Lumen the URIs it should respond to
| and give it the Closure to call when that URI is requested.
|
*/

$app->get('/', function () use ($app) {
    return $app->version();
});

// route d'info des musiques
$app->group(['prefix' => 'musiques'], function () use ($app) {
  $app
  ->get('/', ['uses' => 'MusiqueController@show'])
  ->get('/{data}', ['uses' =>'MusiqueController@show']);
});

// route d'info des playlists
$app->group(['prefix' => 'playlists'], function () use ($app) {
  $app
  ->get('/', ['uses' =>'PlaylistController@show'])
  ->get('/{data}', ['uses' =>'PlaylistController@show']);
});

// route de creation de compte
$app->group(['prefix' => 'utilisateur'], function () use ($app) {
  $app
  ->post('/', ['uses' =>'UtilisateurController@add']);
});

// route de connexion
$app->post('/login', 'AuthenticationController@authenticate');

// route de modification de musiques playlists (besoin d'auth)
$app->group(['middleware' => 'auth'], function($app)
{
  $app->group(['prefix' => 'playlists'], function () use ($app) {
    $app
    ->post('/', ['uses' =>'PlaylistController@add'])
    ->put('/{id}', ['uses' =>'PlaylistController@edit'])
    ->delete('/{id}', ['uses' =>'PlaylistController@delete']);
  });
  $app->group(['prefix' => 'musiques'], function () use ($app) {
    $app
    ->post('/', ['uses' =>'MusiqueController@add'])
    ->put('/{id}', ['uses' =>'MusiqueController@edit'])
    ->delete('/{id}', ['uses' =>'MusiqueController@delete']);
  });
  $app->group(['prefix' => 'utilisateur'], function () use ($app) {
    $app
    ->get('/', ['uses' =>'UtilisateurController@show'])
    ->put('/{id}', ['uses' =>'UtilisateurController@edit']);
  });
});
