CREATE DATABASE IF NOT EXISTS USER_APP;

USE USER_APP;

CREATE TABLE card_type (
  id INT AUTO_INCREMENT PRIMARY KEY,
  card_name VARCHAR(50)
);

CREATE TABLE card (
  id INT AUTO_INCREMENT PRIMARY KEY,
  type INT NOT NULL,
  FOREIGN KEY (type) REFERENCES card_type(id)
);

CREATE TABLE deck (
  id INT AUTO_INCREMENT PRIMARY KEY,
  card INT NOT NULL,
  FOREIGN KEY (card) REFERENCES card(id)
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
  game_token INT UNIQUE,
  game_status VARCHAR(20),
  current_player INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  center_deck INT NOT NULL,
  host INT NOT NULL,
  players INT NOT NULL,
  FOREIGN KEY (center_deck) REFERENCES deck(id),
  FOREIGN KEY (host) REFERENCES users(id)
);

CREATE TABLE player (
  id INT AUTO_INCREMENT PRIMARY KEY,
  player_token INT UNIQUE,
  game_id INT NOT NULL,
  user_id INT NOT NULL,
  hand_cards INT NOT NULL,
  displayName VARCHAR(50),
  FOREIGN KEY (game_id) REFERENCES game(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (hand_cards) REFERENCES deck(id)
);

CREATE TABLE gameHistory (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

ALTER TABLE game ADD CONSTRAINT fk_players FOREIGN KEY (players) REFERENCES player(id);

-- Damit currentPlayer (in game) auf player verweist
ALTER TABLE game ADD CONSTRAINT fk_current_player FOREIGN KEY (current_player) REFERENCES player(id);