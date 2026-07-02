CREATE TABLE IF NOT EXISTS settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    setting_key TEXT NOT NULL UNIQUE,
    setting_value TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS calendar_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    external_id TEXT,
    title TEXT NOT NULL,
    location TEXT,
    starts_at TEXT NOT NULL,
    ends_at TEXT NOT NULL,
    provider TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cycling_rides (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    external_id TEXT,
    name TEXT NOT NULL,
    started_at TEXT NOT NULL,
    distance_miles REAL NOT NULL,
    moving_time_seconds INTEGER NOT NULL,
    elapsed_time_seconds INTEGER NOT NULL,
    elevation_feet REAL NOT NULL,
    average_speed_mph REAL NOT NULL,
    average_power_watts REAL,
    normalized_power_watts REAL,
    average_heart_rate_bpm INTEGER,
    average_cadence_rpm INTEGER,
    calories INTEGER,
    tss REAL,
    notes TEXT,
    source TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

