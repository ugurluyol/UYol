CREATE TABLE ride_contract (
    id CHAR(36) NOT NULL,
    ride_id CHAR(36) NOT NULL,
    price_per_seat NUMERIC NOT NULL,
    booked_seats TEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ride_contract FOREIGN KEY (ride_id) REFERENCES ride(id)
);

CREATE INDEX ride_contract_idx ON ride_contract(ride_id);