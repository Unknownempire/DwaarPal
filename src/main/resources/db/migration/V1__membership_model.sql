-- Create the base schema for a fresh database. Existing installations retain their data
-- and are migrated below from users.society_id/flat_id and vehicles.user_id/society_id.
CREATE TABLE IF NOT EXISTS societies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    pincode VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS buildings (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    society_id BIGINT NOT NULL REFERENCES societies(id)
);

CREATE TABLE IF NOT EXISTS flats (
    id BIGSERIAL PRIMARY KEY,
    flat_number VARCHAR(255) NOT NULL,
    building_id BIGINT NOT NULL REFERENCES buildings(id)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS gates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    society_id BIGINT NOT NULL REFERENCES societies(id)
);

CREATE TABLE IF NOT EXISTS memberships (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    society_id BIGINT NOT NULL,
    flat_id BIGINT,
    status VARCHAR(32) NOT NULL,
    applied_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    ended_at TIMESTAMP,
    reviewer_user_id BIGINT
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGSERIAL PRIMARY KEY,
    registration_number VARCHAR(255) NOT NULL UNIQUE,
    image_url VARCHAR(255),
    type VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    model VARCHAR(255) NOT NULL,
    location_status VARCHAR(255),
    membership_id BIGINT
);

CREATE TABLE IF NOT EXISTS entry_exit_records (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    guard_id BIGINT NOT NULL REFERENCES users(id),
    gate_id BIGINT NOT NULL REFERENCES gates(id),
    type VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL
);

-- Add the new association on databases created with the previous entity model.
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS society_id BIGINT;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS flat_id BIGINT;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS status VARCHAR(32);
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS applied_at TIMESTAMP;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS reviewed_at TIMESTAMP;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS ended_at TIMESTAMP;
ALTER TABLE memberships ADD COLUMN IF NOT EXISTS reviewer_user_id BIGINT;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'society_id') THEN
        EXECUTE $q$
            INSERT INTO memberships(user_id, society_id, flat_id, status, applied_at)
            SELECT u.id, u.society_id, u.flat_id,
                   CASE
                     WHEN u.role IN ('ADMIN', 'GUARD') THEN 'ACTIVE'
                     WHEN u.status = 'APPROVED' THEN 'ACTIVE'
                     WHEN u.status = 'REJECTED' THEN 'REJECTED'
                     WHEN u.status = 'INACTIVE' THEN 'INACTIVE'
                     ELSE 'PENDING'
                   END,
                   CURRENT_TIMESTAMP
              FROM users u
             WHERE u.society_id IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM memberships m WHERE m.user_id = u.id AND m.society_id = u.society_id)
        $q$;
        UPDATE users SET status = CASE WHEN status = 'INACTIVE' THEN 'INACTIVE' ELSE 'ACTIVE' END;
        ALTER TABLE users DROP COLUMN society_id;
        ALTER TABLE users DROP COLUMN IF EXISTS flat_id;
    END IF;
END $$;

ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS membership_id BIGINT;
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'vehicles' AND column_name = 'user_id') THEN
        EXECUTE $q$
            INSERT INTO memberships(user_id, society_id, status, applied_at)
            SELECT DISTINCT v.user_id, v.society_id,
                   CASE
                     WHEN u.role IN ('ADMIN', 'GUARD') THEN 'ACTIVE'
                     WHEN u.status = 'APPROVED' THEN 'ACTIVE'
                     WHEN u.status = 'REJECTED' THEN 'REJECTED'
                     WHEN u.status = 'INACTIVE' THEN 'INACTIVE'
                     ELSE 'PENDING'
                   END,
                   CURRENT_TIMESTAMP
              FROM vehicles v
              JOIN users u ON u.id = v.user_id
             WHERE v.user_id IS NOT NULL AND v.society_id IS NOT NULL
               AND NOT EXISTS (SELECT 1 FROM memberships m WHERE m.user_id = v.user_id AND m.society_id = v.society_id)
        $q$;
        EXECUTE $q$
            UPDATE vehicles v
               SET membership_id = m.id
              FROM memberships m
             WHERE m.user_id = v.user_id AND m.society_id = v.society_id
               AND v.membership_id IS NULL
        $q$;
        ALTER TABLE vehicles DROP COLUMN user_id;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'vehicles' AND column_name = 'society_id') THEN
        ALTER TABLE vehicles DROP COLUMN society_id;
    END IF;
END $$;

ALTER TABLE vehicles ALTER COLUMN membership_id SET NOT NULL;
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_vehicles_membership') THEN
        ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_membership FOREIGN KEY (membership_id) REFERENCES memberships(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_memberships_user') THEN
        ALTER TABLE memberships ADD CONSTRAINT fk_memberships_user FOREIGN KEY (user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_memberships_society') THEN
        ALTER TABLE memberships ADD CONSTRAINT fk_memberships_society FOREIGN KEY (society_id) REFERENCES societies(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_memberships_flat') THEN
        ALTER TABLE memberships ADD CONSTRAINT fk_memberships_flat FOREIGN KEY (flat_id) REFERENCES flats(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_memberships_reviewer') THEN
        ALTER TABLE memberships ADD CONSTRAINT fk_memberships_reviewer FOREIGN KEY (reviewer_user_id) REFERENCES users(id);
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_memberships_one_active_per_user_society
    ON memberships(user_id, society_id) WHERE status = 'ACTIVE';

