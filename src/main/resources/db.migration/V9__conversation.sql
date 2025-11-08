CREATE TABLE conversation (
    id CHAR(36) NOT NULL,
    ride_id CHAR(36) NOT NULL,
    participant CHAR(36) NOT NULL,
    driver_id CHAR(36) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE','BLOCKED','CLOSED')),
    PRIMARY KEY (id),
    CONSTRAINT fk_conversation_ride FOREIGN KEY (ride_id) REFERENCES ride(id),
    CONSTRAINT fk_conversation_driver FOREIGN KEY (driver_id) REFERENCES driver(id)
);

CREATE OR REPLACE FUNCTION check_participant_exists()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM user_account WHERE id = NEW.participant)
       AND NOT EXISTS (SELECT 1 FROM driver WHERE id = NEW.participant) THEN
        RAISE EXCEPTION 'Participant % not found in user_account or driver', NEW.participant;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_participant
    BEFORE INSERT OR UPDATE ON conversation
        FOR EACH ROW
        EXECUTE FUNCTION check_participant_exists();