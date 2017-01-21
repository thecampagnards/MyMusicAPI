
CREATE TABLE UTILISATEUR (
  id int(11) NOT NULL AUTO_INCREMENT,
  pseudo varchar(100) DEFAULT NULL,
  password varchar(120) DEFAULT NULL,
  email varchar(100) DEFAULT NULL,
  created_at timestamp NULL DEFAULT NULL,
  updated_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE MUSIQUE (
  id int(11) NOT NULL AUTO_INCREMENT,
  state bit DEFAULT NULL,
  title varchar(100) DEFAULT NULL,
  length int(11) DEFAULT NULL,
  artist varchar(65) DEFAULT NULL,
  listen int(11) DEFAULT 0,
  id_utilisateur int(11) DEFAULT NULL,

  PRIMARY KEY (id),
  FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE PLAYLIST (
  id int(11) NOT NULL AUTO_INCREMENT,
  title varchar(100) DEFAULT NULL,
  state bit DEFAULT NULL,
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

CREATE TABLE UTILISATEUR_HISTORIQUE (
  id_utilisateur int(11) NOT NULL,
  id_musique int(11) NOT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id_utilisateur,id_musique,created_at),
  FOREIGN KEY (id_utilisateur) REFERENCES UTILISATEUR(id),
  FOREIGN KEY (id_musique) REFERENCES MUSIQUE(id)

) ENGINE=InnoDB DEFAULT CHARSET=latin1;
