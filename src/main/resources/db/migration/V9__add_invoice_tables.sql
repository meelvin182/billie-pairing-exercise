CREATE TABLE IF NOT EXISTS organisations_schema.invoices
(
    id          UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    organisation_id     UUID NOT NULL,
    creation_date       DATE DEFAULT NOW() NOT NULL,
    due_date            DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS organisations_schema.invoice_items
(
    id     UUID DEFAULT UUID_GENERATE_V4() PRIMARY KEY,
    invoice_id UUID REFERENCES organisations_schema.invoices (id ) NOT NULL,
    quantity       NUMERIC NOT NULL,
    price_per_item DECIMAL  NOT NULL
);