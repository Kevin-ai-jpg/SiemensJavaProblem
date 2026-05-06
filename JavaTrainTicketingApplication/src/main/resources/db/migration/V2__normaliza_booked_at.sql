-- Convert numeric epoch-millis to parseable timestamp strings
UPDATE booking
SET booked_at = strftime('%Y-%m-%d %H:%M:%f', CAST(booked_at AS INTEGER) / 1000.0, 'unixepoch')
WHERE booked_at GLOB '[0-9]*';
