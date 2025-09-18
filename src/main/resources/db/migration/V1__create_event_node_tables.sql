CREATE TABLE IF NOT EXISTS event_nodes (
    event_node_id UUID PRIMARY KEY,
    site_display_label TEXT NOT NULL,
    event_node_start_dttm_utc TIMESTAMPTZ NOT NULL,
    event_node_end_dttm_utc TIMESTAMPTZ NOT NULL,
    expected_capacity_value NUMERIC(18,4) NOT NULL,
    deleted BOOLEAN NOT NULL,
    organization_name TEXT NOT NULL,
    created_dttm TIMESTAMPTZ NOT NULL,
    last_updated_dttm TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS requested_targets (
    event_node_id UUID NOT NULL REFERENCES event_nodes(event_node_id) ON DELETE CASCADE,
    start_dttm TIMESTAMPTZ NOT NULL,
    end_dttm TIMESTAMPTZ NOT NULL,
    target_value NUMERIC(18,4) NOT NULL,
    PRIMARY KEY (event_node_id, start_dttm)
);

CREATE INDEX IF NOT EXISTS idx_requested_targets_event_node ON requested_targets(event_node_id);
