CREATE TABLE message (
    id CHAR(36) NOT NULL,
    conversation_id CHAR(36) NOT NULL,
    sender_id CHAR(36) NOT NULL,
    content VARCHAR(256) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    last_updated TIMESTAMP NOT NULL,
    edited BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (conversation_id) REFERENCES conversation(id),
    CONSTRAINT FOREIGN KEY (sender_id) REFERENCES user_account(id)
);