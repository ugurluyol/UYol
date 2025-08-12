CREATE TABLE car (
  id CHAR(36) NOT NULL,
  owner CHAR(36) NOT NULL,
  license_plate VARCHAR(12) NOT NULL,
  car_brand VARCHAR(64) NOT NULL,
  car_model VARCHAR(64) NOT NULL,
  car_color VARCHAR(64) NOT NULL,
  car_year SMALLINT NOT NULL,
  seat_count SMALLINT NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY(id),
  CONSTRAINT fk_car_user FOREIGN KEY (owner) REFERENCES user_account(id) 
);

CREATE UNIQUE INDEX car_owner_idx ON car (owner);

CREATE UNIQUE INDEX license_plate ON car (license_plate);
