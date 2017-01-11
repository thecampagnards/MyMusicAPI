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

$app->get('profile', [
    'middleware' => 'auth',
    'uses' => 'UserController@showProfile'
]);

$app->group(['prefix' => 'musiques'], function () use ($app) {
  $app
  ->get('/', ['uses' => 'MusiqueController@show'])
  ->get('/{data}', ['uses' =>'MusiqueController@show'])
  ->post('/', ['uses' =>'MusiqueController@add'])
  ->put('/{id}', ['uses' =>'MusiqueController@edit'])
  ->delete('/{id}', ['uses' =>'MusiqueController@delete']);
});

$app->group(['prefix' => 'playlists'], function () use ($app) {
  $app
  ->get('/', ['uses' =>'PlaylistController@show'])
  ->get('/{data}', ['uses' =>'PlaylistController@show'])
  ->post('/', ['uses' =>'PlaylistController@add'])
  ->put('/{id}', ['uses' =>'PlaylistController@edit'])
  ->delete('/{id}', ['uses' =>'PlaylistController@delete']);
});

$app->post('/auth/login', 'AuthenticationController@authenticate');

$app->group(['middleware' => 'auth'], function($app)
{
    $app->get('/test', function() {
        return response()->json([
            'message' => 'Hello World!',
        ]);
    });
});
