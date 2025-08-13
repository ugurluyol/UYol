CREATE TABLE owner (
  id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  voen VARCHAR(10) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT fk_owner_user FOREIGN KEY(user_id) REFERENCES user_account(id)
);

CREATE UNIQUE INDEX user_owner_idx ON owner (user_id);

CREATE UNIQUE INDEX user_voen ON owner (voen)
