-- Merchant verification documents.
-- The blueprint shipped the entity/protos but no migration, so the table is new.
CREATE TABLE IF NOT EXISTS "merchant_documents" (
    "document_id" SERIAL PRIMARY KEY,
    "merchant_id" INT NOT NULL REFERENCES "merchants" ("merchant_id") ON DELETE CASCADE,
    "document_type" VARCHAR(100) NOT NULL,
    "document_url" TEXT NOT NULL,
    "status" VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    "note" TEXT,
    "created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    "deleted_at" TIMESTAMP DEFAULT NULL
);

CREATE SEQUENCE IF NOT EXISTS merchant_documents_seq START 1;

CREATE INDEX IF NOT EXISTS "idx_merchant_documents_merchant_id" ON "merchant_documents" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_merchant_documents_status" ON "merchant_documents" ("status");
