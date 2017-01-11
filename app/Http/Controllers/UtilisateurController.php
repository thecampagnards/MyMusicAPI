<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use App\User;

class UtilisateurController extends Controller
{
    public function create(){
      User::create(['email' => 'test@test.test', 'password' => app('hash')->make('s3curit3')]);
    }
}
