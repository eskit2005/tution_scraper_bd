CREATE TABLE refresh_tokens (
                                token VARCHAR(255) PRIMARY KEY,
                                issued_at DATETIME NOT NULL,
                                expiry DATETIME NOT NULL,
                                revoked BOOLEAN NOT NULL,
                                user_id INT NOT NULL references User(id) on delete cascade
)