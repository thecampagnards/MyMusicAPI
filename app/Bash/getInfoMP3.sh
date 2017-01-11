#!/bin/bash

# http://eyed3.nicfit.net/

if [ $# -ne 2 ]
  then
    echo "Il manque l'id du mp3 et le dossier"
fi

# on recupere le cover et les infos
retour=$(eyeD3 --write-images $2 $2/$1.mp3)
j=0
for i in $retour
do
  #if [ $j -eq 26 -o $j -eq 24 -o $j -eq 7 ]
    #then
      #echo "$i"
  #fi
  (( ++j ))
done

# on renome le cover
mv $2/FRONT_COVER.jpg $2/$1.jpg

echo -e "{\"title\":\""$1"\", \"artist\":\""$1"\", \"length\":\""$1"\"}"
