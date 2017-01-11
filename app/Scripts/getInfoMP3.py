#!/usr/bin/env python
import sys
import eyed3
import json

# on recupere les parametres
name = sys.argv[1]
directory = sys.argv[2]

# on lit le mp3
audiofile = eyed3.load(directory + '/' + name + '.mp3')

# on recupere nos donnees
data = {}
data['artist'] = audiofile.tag.artist
data['title'] = audiofile.tag.title
data['length'] = audiofile.info.time_secs

# on enregistre l image
f = open(directory + '/' + name + '.jpg', 'w')
f.write(audiofile.tag.images[0].image_data)
f.close()

# on affiche nos donnees
print json.dumps(data)
