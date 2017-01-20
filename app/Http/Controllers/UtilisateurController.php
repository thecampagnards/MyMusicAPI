<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Tymon\JWTAuth\JWTAuth;
use Illuminate\Support\Facades\DB;
use App\User;

class UtilisateurController extends Controller
{

    const DB_STRUCTURE = array('id', 'pseudo', 'password', 'email', 'created_at', 'updated_at');
    protected $jwt;

    public function __construct(JWTAuth $jwt)
    {
        $this->jwt = $jwt;
    }

    public function show(){
      try{
        return response()->json(Auth::user());
      } catch (Exception $e){
        return response()->json($e);
      }
    }

    public function historique(){
      try{
        $musiques = DB::table('UTILISATEUR_HISTORIQUE')
        ->join('MUSIQUE', 'MUSIQUE.id', '=', 'UTILISATEUR_HISTORIQUE.id_musique')
        ->where('UTILISATEUR_HISTORIQUE.id_utilisateur', Auth::id())
        ->orderBy('UTILISATEUR_HISTORIQUE.created_at', 'DESC')
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
        }
        return response()->json($musiques);
      } catch (Exception $e){
        return response()->json($e);
      }
    }

    public function add(Request $request){
      try{
        if(User::where('email', $request->All()['email'])->first()){
          throw new \Exception('L\'email existe déjà.');
        }
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

    private function cleanForQuery($utilisateur){
      return array_filter(
        (array) $utilisateur,
        function ($key) {
          return in_array($key, self::DB_STRUCTURE);
        },
        ARRAY_FILTER_USE_KEY
      );
    }

    private function builder(Array $utilisateur){

      $utilisateur = (object)$utilisateur;

      // on insert en db + recuperation de l'id
      if(empty($utilisateur->id)){
        // on hash le password
        $utilisateur->password = app('hash')->make($utilisateur->password);
        $utilisateur = User::create($this->cleanForQuery($utilisateur));
        // on recupere le token de connexion
        $utilisateur->token = Auth::login($utilisateur);
      }elseif($utilisateur->id == Auth::id()){
        // @TODO UPDATE du USER
        DB::update($this->cleanForQuery($utilisateur));
      }
      return $utilisateur;
    }
}
