#!/usr/bin/env python
import sys
import eyed3
import json

# on recupere les parametres
name = sys.argv[1]
directory = sys.argv[2]
title = sys.argv[3]
artist = sys.argv[4]
image = sys.argv[5]

# on lit le mp3
audiofile = eyed3.load(directory + '/' + name + '.mp3')

# on insert nos donnees
audiofile.tag.artist = artist
audiofile.tag.title = title

# on recupere l image
imagedata = open(image,'rb').read()

# ajout de l image au tags
audiofile.tag.images.set(3,imagedata,'image/jpeg','description')

# on enregistre les changements
audiofile.tag.save()
