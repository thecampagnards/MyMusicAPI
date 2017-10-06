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

# recupération des datas de la musique
data=$(curl -G -s "http://musicbrainz.org/ws/2/release-group/?fmt=json&" --data-urlencode "query=artistname:$video_title")
score=$(jq -r '."release-groups"[0].score' <<< $data)
if [ $score -eq 100 ];then
  video_title=$(jq -r '."release-groups"[0].title' <<< $data)
  video_artist=$(jq -r '."release-groups"[0]."artist-credit"[0].artist.name' <<< $data)
  tmp=$(jq -r '."release-groups"[0]."artist-credit"[0].joinphrase' <<< $data)
  [ "$tmp"  != "null" ] && video_artist+=$tmp
  tmp=$(jq -r '."release-groups"[0]."artist-credit"[1].artist.name' <<< $data)
  [ "$tmp" != "null" ] && video_artist+=$tmp
  video_mid=$(jq -r '."release-groups"[0].releases[0].id' <<< $data)

  data=$(curl -s -L "http://coverartarchive.org/release/$video_mid")
  video_thumbnail_url=$(jq -r '.images[0].thumbnails.small' <<< $data)
fi

#calcul de la duree en seconde
IFS=: read minutes secondes <<< $video_duration
video_duration=$(($minutes*60 + $secondes))

#dl de la jaquette + mp3
wget -O "/tmp/$video_filename" "$video_url_with_thumbnail" &> /dev/null
wget -O "$dossier/$filename.jpg" "$video_thumbnail_url" &> /dev/null

#on converti le mp3
ffmpeg -i "/tmp/$video_filename" "/tmp/$filename.wav" &> /dev/null
lame -b 320 "/tmp/$filename.wav" "$dossier/$filename.mp3" &> /dev/null

#suppr des temps
rm -f "/tmp/$video_filename" "/tmp/$filename.wav"
exec 42<&-
rm -f $TMP_FILE

echo -e "{\"mid\":\""$video_mid"\", \"title\":\""$video_title"\",\"artist\":\""$video_artist"\", \"length\":\""$video_duration"\"}"

exit 0
