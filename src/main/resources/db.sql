CREATE TABLE UTILISATEUR (
  id int(11) NOT NULL AUTO_INCREMENT,
  pseudo varchar(100) DEFAULT NULL,
  motdepasse varchar(100) DEFAULT NULL,
  email varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE MUSIQUE (
  id int(11) NOT NULL AUTO_INCREMENT,
  title varchar(100) DEFAULT NULL,
  length int(11) DEFAULT NULL,
  artist varchar(65) DEFAULT NULL,
  id_utilisateur int(11) DEFAULT NULL,

  PRIMARY KEY (id),
  FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE PLAYLIST (
  id int(11) NOT NULL AUTO_INCREMENT,
  title varchar(100) DEFAULT NULL,
  statut bit DEFAULT NULL,
  id_utilisateur int(11) DEFAULT NULL,

  PRIMARY KEY (id),
  FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE PLAYLIST_MUSIQUE (
  id_playlist int(11) NOT NULL,
  id_musique int(11) NOT NULL,

  PRIMARY KEY (id_playlist,id_musique),
  FOREIGN KEY (id_playlist) REFERENCES PLAYLIST(id),
  FOREIGN KEY (id_musique) REFERENCES MUSIQUE(id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;