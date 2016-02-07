DELETE FROM users;
DELETE FROM token_history;
ALTER SEQUENCE seq_user_id RESTART;

INSERT INTO users (email, password) VALUES ('email_1@mail.ru', 'pass1');
INSERT INTO users (email, password) VALUES ('email_3@mail.ru', 'pass3');
INSERT INTO users (email, password) VALUES ('email_4@mail.ru', 'pass4');
