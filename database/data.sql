CREATE DATABASE IF NOT EXISTS USER_APP;

USE USER_APP;

CREATE TABLE card_type (
  id INT AUTO_INCREMENT PRIMARY KEY,
  card_name VARCHAR(50),
  card_value INT,
  card_event VARCHAR(50)
);

CREATE TABLE deck (
  id INT AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE card (
  id INT AUTO_INCREMENT PRIMARY KEY,
  card_type INT NOT NULL,
  deck_id INT,
  deck_position INT,
  FOREIGN KEY (card_type) REFERENCES card_type(id),
  FOREIGN KEY (deck_id) REFERENCES deck(id)
);

CREATE TABLE users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  user_password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE game (
  id INT AUTO_INCREMENT PRIMARY KEY,
  game_code CHAR(6) UNIQUE,
  game_status VARCHAR(20),
  current_player_id INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  center_deck INT NOT NULL,
  discard_pile INT NOT NULL,
  host_id INT NOT NULL,
  FOREIGN KEY (center_deck) REFERENCES deck(id),
  FOREIGN KEY (discard_pile) REFERENCES deck(id)
);

CREATE TABLE player (
  id INT AUTO_INCREMENT PRIMARY KEY,
  game_id INT,
  user_id INT NOT NULL,
  hand_cards INT NOT NULL,
  display_name VARCHAR(50),
  turn_indicator INT,
  web_socket_id VARCHAR(50),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (hand_cards) REFERENCES deck(id)
);

CREATE TABLE gameHistory (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Damit currentPlayer (in game) auf player verweist
ALTER TABLE game ADD CONSTRAINT fk_current_player_id FOREIGN KEY (current_player_id) REFERENCES player(id);
ALTER TABLE game ADD CONSTRAINT fk_host_id FOREIGN KEY (host_id) REFERENCES player(id);

INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 1, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 2, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 3, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 4, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 5, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 6, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 7, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 8, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Rock", 9, "NONE");

INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 1, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 2, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 3, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 4, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 5, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 6, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 7, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 8, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Paper", 9, "NONE");

INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 1, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 2, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 3, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 4, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 5, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 6, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 7, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 8, "NONE");
INSERT INTO card_type (card_name, card_value, card_event) VALUES ("Scissors", 9, "NONE");