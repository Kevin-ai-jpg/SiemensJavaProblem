CREATE TABLE station (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         code TEXT NOT NULL UNIQUE,
                         name TEXT
);

CREATE TABLE train (
                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                       train_code TEXT NOT NULL UNIQUE,
                       name TEXT,
                       capacity INTEGER NOT NULL
);

CREATE TABLE stop_time (
                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                           train_id INTEGER NOT NULL,
                           station_id INTEGER NOT NULL,
                           stop_index INTEGER NOT NULL,
                           arrival TEXT,
                           departure TEXT,
                           CONSTRAINT fk_stop_time_train FOREIGN KEY (train_id) REFERENCES train(id),
                           CONSTRAINT fk_stop_time_station FOREIGN KEY (station_id) REFERENCES station(id),
                           CONSTRAINT uk_stop_time_train_index UNIQUE (train_id, stop_index)
);

CREATE INDEX idx_stop_time_train_id ON stop_time(train_id);
CREATE INDEX idx_stop_time_station_id ON stop_time(station_id);

CREATE TABLE booking (
                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                         train_id INTEGER NOT NULL,
                         passenger_name TEXT,
                         passenger_email TEXT,
                         from_index INTEGER NOT NULL,
                         to_index INTEGER NOT NULL,
                         seats INTEGER NOT NULL,
                         booked_at TEXT,
                         CONSTRAINT fk_booking_train FOREIGN KEY (train_id) REFERENCES train(id)
);

CREATE INDEX idx_booking_train_id ON booking(train_id);
