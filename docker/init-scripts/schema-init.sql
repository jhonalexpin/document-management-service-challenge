CREATE SCHEMA IF NOT EXISTS document_schema;

CREATE TABLE IF NOT EXISTS document_schema.documents (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    document_name VARCHAR(255) NOT NULL,
    tags TEXT[],
    minio_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP
);

-- ✅ Index for filtering by user
CREATE INDEX IF NOT EXISTS idx_documents_user_id
    ON document_schema.documents (user_id);

-- ✅ Index for searching by document name
CREATE INDEX IF NOT EXISTS idx_documents_document_name
    ON document_schema.documents (document_name);

-- ✅ GIN index for array search (tags)
CREATE INDEX IF NOT EXISTS idx_documents_tags
    ON document_schema.documents
    USING GIN (tags);