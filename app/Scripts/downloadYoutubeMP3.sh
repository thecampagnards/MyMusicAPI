#!/bin/bash

url=$1
filename=$2
dossier=$3

TMP_FILE="youtube-mp3-$RANDOM.tmp"
#recuperation des donnees
youtube-dl --ignore-errors --get-title --get-url --get-filename --get-thumbnail --get-duration "$url" > $TMP_FILE 2> "/tmp/$TMP_FILE.err"

#recupération des valeurs
exec 42< $TMP_FILE
read video_title <&42
read video_url <&42
read video_url_with_thumbnail <&42
read video_thumbnail_url <&42
read video_filename <&42
read video_duration <&42

#dl de la jaquette + mp3
wget -O "/tmp/$video_filename" "$video_url_with_thumbnail"
wget -O "$dossier/$filename.jpg" "$video_thumbnail_url"

#on converti le mp3
ffmpeg -i "/tmp/$video_filename" "/tmp/$filename.wav"
lame -b 320 "/tmp/$filename.wav" "$dossier/$filename.mp3"

#suppr des temps
rm -f "/tmp/$video_filename" "/tmp/$filename.wav"
exec 42<&-
rm -f $TMP_FILE

echo -e "{\"title\":\""$video_title"\", \"length\":\""$video_duration"\"}"
