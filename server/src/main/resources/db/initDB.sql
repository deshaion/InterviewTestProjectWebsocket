DROP TABLE IF EXISTS token_history;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS seq_user_id;

/*
its needed to install extension for postgresql (version must be from 9.4)
$ sudo apt-get install postgresql-contrib-9.4

function gen_random_uuid()
*/
CREATE EXTENSION pgcrypto;

CREATE SEQUENCE seq_user_id;

CREATE TABLE users
(
  id         INTEGER PRIMARY KEY DEFAULT nextval('seq_user_id'),
  email      VARCHAR NOT NULL,
  password   VARCHAR NOT NULL,
  token      UUID,
  token_expiration_date TIMESTAMP
);
CREATE UNIQUE INDEX unique_email ON USERS (email);

CREATE TABLE token_history
(
  user_id   INTEGER NOT NULL,
  token     UUID,
  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);