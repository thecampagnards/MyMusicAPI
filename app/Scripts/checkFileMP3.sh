#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo -e "Utilisation : ./checkFileMP3.sh fichier dossier \nExemples :\n ./checkFile.sh monfichier.mp3 mondossier"
  exit 1
fi

dossier=$2
mon_fichier=$1
mon_fichier_extension="${mon_fichier##*.}"
mon_fichier_nom=$(basename -s .$mon_fichier_extension $mon_fichier)

# parcours des fichiers
for fichier in $dossier/*.$mon_fichier_extension; do
  fichier_nom=$(basename -s .$mon_fichier_extension $fichier)
  # check le nom du fichier (si on a le même fichier dans le dossier) + la diff binaire des fichiers
  if [ $mon_fichier_nom != $fichier_nom ] && diff $mon_fichier $fichier >/dev/null; then
    # on affiche le nom du fichier identique pour nous l'id de la musique
    echo -e $fichier_nom
    exit 1
  fi
done

exit 0
