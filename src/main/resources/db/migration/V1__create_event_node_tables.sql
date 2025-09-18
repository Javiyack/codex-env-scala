CREATE TABLE IF NOT EXISTS event_nodes (
    event_node_id UUID PRIMARY KEY,
    site_display_label TEXT NOT NULL,
    event_node_start_dttm_utc TIMESTAMPTZ NOT NULL,
    event_node_end_dttm_utc TIMESTAMPTZ NOT NULL,
    expected_capacity_value NUMERIC(18,4) NOT NULL,
    deleted BOOLEAN NOT NULL,
    organization_name TEXT NOT NULL,
    requested_targets JSONB NOT NULL,
    created_dttm TIMESTAMPTZ NOT NULL,
    last_updated_dttm TIMESTAMPTZ NOT NULL
);
