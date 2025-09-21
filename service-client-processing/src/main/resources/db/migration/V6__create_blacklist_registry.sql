CREATE TABLE blacklist_registry
(
    id                        BIGSERIAL PRIMARY KEY,
    document_type             VARCHAR(50)  NOT NULL,
    document_id               VARCHAR(100) NOT NULL,
    blacklisted_at            TIMESTAMP    DEFAULT now(),
    reason                    VARCHAR(500),
    blacklist_expiration_date TIMESTAMP,
    created_at                TIMESTAMP    DEFAULT now()
);

CREATE INDEX idx_blacklist_doc ON blacklist_registry (document_type, document_id);