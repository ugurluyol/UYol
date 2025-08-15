CREATE TABLE ride (
    id CHAR(36) NOT NULL,
    driver_id CHAR(36) NOT NULL,
    owner_id CHAR(36) NOT NULL,
    from_location_desc VARCHAR(64) NOT NULL,
    from_latitude DECIMAL(9,6) NOT NULL,
    from_longitude DECIMAL(9,6) NOT NULL,
    to_location_desc VARCHAR(64) NOT NULL,
    to_latitude DECIMAL(9,6) NOT NULL,
    to_longitude DECIMAL(9,6) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    price NUMERIC NOT NULL,
    seats TEXT NOT NULL,
    status VARCHAR(18) NOT NULL CHECK ( status IN ('PENDING', 'ON_THE_ROAD', 'CANCELED', 'ENDED_SUCCESSFULLY')),
    description VARCHAR(128) NOT NULL,
    rules TEXT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_driver_ride FOREIGN KEY (driver_id) REFERENCES driver(id),
    CONSTRAINT fk_owner_ride FOREIGN KEY (owner_id) REFERENCES owner(id)
);

CREATE INDEX idx_ride_status ON ride(status);

CREATE INDEX idx_ride_start_time ON ride(start_time);