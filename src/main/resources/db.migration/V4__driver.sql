CREATE TABLE driver (
  id CHAR(36) NOT NULL,
  user_id CHAR(36) NOT NULL,
  driver_license VARCHAR(12) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  last_updated TIMESTAMP NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT fk_driver_user FOREIGN KEY(user_id) REFERENCES tableName(attribute)
);

CREATE UNIQUE INDEX unique_user_driver ON driver (user_id); 
